use std::{
    fmt::Display,
    sync::{atomic::AtomicUsize, Arc},
};

use axum::{response::IntoResponse, Json};
use hyper::StatusCode;
use serde::Deserialize;
use tokio_postgres::Client;
use uuid::Uuid;

use crate::{
    encrypt_password,
    models::{AppThemes, UserSettings},
    responses::ResponseError,
};

#[derive(Debug)]
pub enum RegisterUserErrors {
    NoDBConnection,
    InvalidPayload { payload: String },
    ErrorCheckingIfUserIsAlreadyRegistered,
    UsernameTaken,
    ErrorInsertingUserIntoDB,
    NoUserInserted,
    ErrorInsertingSettingsIntoDB,
    NoSettingsInserted,
}

impl Display for RegisterUserErrors {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        write!(f, "{:?}", self)
    }
}

#[derive(Debug, Deserialize)]
pub struct RegisterUserPayload {
    username: String,
    password: String,
}

static ID: AtomicUsize = AtomicUsize::new(0);

pub async fn register_user(
    payload: Json<serde_json::Value>,
    client: Arc<Option<Client>>,
) -> Result<impl IntoResponse, ResponseError<RegisterUserErrors>> {
    let id = ID.fetch_add(1, std::sync::atomic::Ordering::AcqRel);
    let tracing_prefix = format!("/REGISTER_USER {}:", id);

    tracing::debug!("{} START", tracing_prefix);

    tracing::debug!("{} Parsing payload...", tracing_prefix);
    let RegisterUserPayload { username, password } = match serde_json::from_value(payload.0.clone())
    {
        Ok(p) => p,
        Err(err) => {
            tracing::error!(
                "{} An error `{:?}` occurred while parsing payload `{}`",
                tracing_prefix,
                err,
                payload.0
            );
            let error: ResponseError<_> = (
                StatusCode::BAD_REQUEST,
                RegisterUserErrors::InvalidPayload {
                    payload: payload.0.to_string(),
                },
            )
                .into();
            Err(error)?
        }
    };
    tracing::debug!("{} Payload parsed successfully!", tracing_prefix);

    tracing::debug!("{} Connecting to DB...", tracing_prefix);
    let username_exists = match client.as_ref() {
        Some(conn) => {
            match conn
                .query(
                    "SELECT user_id FROM sf_user WHERE username=$1",
                    &[&username],
                )
                .await
            {
                Ok(r) => !r.is_empty(),
                Err(err) => {
                    tracing::error!(
                        "{} An error `{:?}` occurred while checking if username `{}` exists!",
                        tracing_prefix,
                        err,
                        username
                    );
                    let error: ResponseError<_> = (
                        StatusCode::INTERNAL_SERVER_ERROR,
                        RegisterUserErrors::ErrorCheckingIfUserIsAlreadyRegistered,
                    )
                        .into();
                    Err(error)?
                }
            }
        }

        None => {
            tracing::debug!(
                "{} DB Connection not found! Couldn't check if username exists!",
                tracing_prefix
            );
            true
        }
    };

    if username_exists {
        tracing::error!(
            "{} Username `{}` is already taken!",
            tracing_prefix,
            username
        );
        let error: ResponseError<_> =
            (StatusCode::BAD_REQUEST, RegisterUserErrors::UsernameTaken).into();
        Err(error)?
    }

    tracing::debug!("{} Encrypting password...", tracing_prefix);
    let encrypted = encrypt_password(password);
    tracing::debug!("{} Password encrypted successfully!", tracing_prefix);

    tracing::debug!("{} Creating user...", tracing_prefix);
    let user_id = Uuid::new_v4().to_string();

    match client.as_ref() {
        Some(conn) => {
            tracing::debug!("{} DB Connection found!", tracing_prefix);

            tracing::debug!(
                "{} Inserting user `{}` into DB...",
                tracing_prefix,
                username
            );
            let result = conn
                .execute(
                    "INSERT INTO sf_user VALUES ($1, $2, $3)",
                    &[&user_id, &username, &encrypted],
                )
                .await;

            match result {
                Ok(rows_modified) => {
                    if rows_modified > 0 {
                        tracing::debug!(
                            "{} User `{}` with ID `{}` inserted into DB!",
                            tracing_prefix,
                            username,
                            user_id
                        );
                    } else {
                        tracing::debug!(
                            "{} No user inserted into DB! Reason: Unknown",
                            tracing_prefix
                        );

                        let error: ResponseError<_> = (
                            StatusCode::INTERNAL_SERVER_ERROR,
                            RegisterUserErrors::NoUserInserted,
                        )
                            .into();
                        Err(error)?
                    }
                }
                Err(err) => {
                    tracing::error!(
                        "{} An error `{:?}` occurred while trying to insert user `{}`!",
                        tracing_prefix,
                        err,
                        username
                    );
                    let error: ResponseError<_> = (
                        StatusCode::INTERNAL_SERVER_ERROR,
                        RegisterUserErrors::ErrorInsertingUserIntoDB,
                    )
                        .into();
                    Err(error)?
                }
            }
        }
        None => {
            tracing::error!(
                "{} DB Connection not found!, Couldn't insert user `{}` into DB!",
                tracing_prefix,
                username
            );
        }
    }

    tracing::debug!("{} Inserting user settings...", tracing_prefix);
    match client.as_ref() {
        Some(conn) => {
            tracing::debug!("{} DB Connection found!", tracing_prefix);
            let settings_id = Uuid::new_v4().to_string();
            let theme = format!("{:?}", AppThemes::default());

            tracing::debug!("{} Inserting settings...", tracing_prefix);
            match conn
                .execute(
                    "INSERT INTO sf_settings VALUES ($1, $2, $3)",
                    &[&settings_id, &user_id, &theme],
                )
                .await
            {
                Ok(rows_modified) => {
                    if rows_modified > 0 {
                        tracing::debug!(
                            "{} Settings for user `{}` inserted into DB!",
                            tracing_prefix,
                            user_id
                        );
                    } else {
                        tracing::debug!(
                            "{} No settings inserted into DB! Reason: Unknown",
                            tracing_prefix
                        );
                        let error: ResponseError<_> = (
                            StatusCode::INTERNAL_SERVER_ERROR,
                            RegisterUserErrors::NoSettingsInserted,
                        )
                            .into();
                        Err(error)?
                    }
                }
                Err(err) => {
                    tracing::error!("{} An error `{:?}` occurred while inserting settings for user `{}` into DB!", tracing_prefix, err, user_id);
                    let error: ResponseError<_> = (
                        StatusCode::INTERNAL_SERVER_ERROR,
                        RegisterUserErrors::ErrorInsertingSettingsIntoDB,
                    )
                        .into();
                    Err(error)?
                }
            }
        }
        None => {
            tracing::debug!(
                "{} DB Connection not found!. Couldn't insert user settings into DB!",
                tracing_prefix
            );
        }
    }

    tracing::debug!("{} DONE", tracing_prefix);
    Ok((StatusCode::OK, ""))
}
