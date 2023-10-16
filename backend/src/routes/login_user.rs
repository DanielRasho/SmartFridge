use std::{
    fmt::Display,
    sync::{atomic::AtomicUsize, Arc},
};

use axum::{response::IntoResponse, Json};
use base64::{engine::general_purpose, Engine};
use chrono::{Duration, Utc};
use hyper::StatusCode;
use serde::{Deserialize, Serialize};
use tokio_postgres::Client;
use uuid::Uuid;

use crate::{
    encrypt_password_with_salt, generate_jwt, models::JWT_Token, obtain_salt,
    responses::ResponseError, APP_SECRET,
};

#[derive(Debug)]
pub enum LoginUserErrors {
    InvalidPayload { payload: String },
    NoDBConnection,
    CouldntGenerateJWT,
    ErrorObtainingPassword,
    UsernameDoesntExists,
    PasswordsDontMatch,
    ErrorCreatingSession,
    ErrorDecodingSalt,
}

impl Display for LoginUserErrors {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        write!(f, "{:?}", self)
    }
}

#[derive(Debug, Serialize)]
pub struct LoginUserResponse {
    /// The JWT token that was evaluated
    pub token: String,
}

#[derive(Debug, Deserialize)]
struct LoginUserPayload {
    username: String,
    password: String,
}

static ID: AtomicUsize = AtomicUsize::new(0);

pub async fn login_user(
    payload: Json<serde_json::Value>,
    client: Arc<Option<Client>>,
) -> Result<impl IntoResponse, ResponseError<LoginUserErrors>> {
    let id = ID.fetch_add(1, std::sync::atomic::Ordering::AcqRel);
    let tracing_prefix = format!("/user/login - {}:", id);

    tracing::debug!("{} START", tracing_prefix);

    tracing::debug!("{} Parsing payload...", tracing_prefix);
    let LoginUserPayload { username, password } = match serde_json::from_value(payload.0.clone()) {
        Ok(p) => p,
        Err(err) => {
            tracing::error!(
                "{} An error `{:?}` occurred while parsing payload {}",
                tracing_prefix,
                err,
                payload.0
            );
            let error: ResponseError<_> = (
                StatusCode::BAD_REQUEST,
                LoginUserErrors::InvalidPayload {
                    payload: payload.0.to_string(),
                },
            )
                .into();
            Err(error)?
        }
    };
    tracing::debug!("{} Payload parsed successfully!", tracing_prefix);

    // TODO Check if password is correct...

    tracing::debug!("{} Checking if we have a DB connection...", tracing_prefix);
    if let None = client.as_ref() {
        let error: ResponseError<_> = (
            StatusCode::INTERNAL_SERVER_ERROR,
            LoginUserErrors::NoDBConnection,
        )
            .into();
        Err(error)?
    }

    let conn = client.as_ref().as_ref().unwrap();

    tracing::debug!("{} Retrieving user `{}` salt...", tracing_prefix, username);
    let (db_password, user_id): (String, String) = match conn
        .query(
            "SELECT password,user_id FROM sf_user WHERE username=$1",
            &[&username],
        )
        .await
    {
        Ok(r) => {
            if r.is_empty() {
                tracing::error!(
                    "{} No username found that matches `{}`!",
                    tracing_prefix,
                    username
                );
                let error: ResponseError<_> = (
                    StatusCode::BAD_REQUEST,
                    LoginUserErrors::UsernameDoesntExists,
                )
                    .into();
                Err(error)?
            }
            (r.get(0).unwrap().get(0), r.get(0).unwrap().get(1))
        }
        Err(err) => {
            tracing::error!(
                "{} An error `{}` occurred while retrieving password for user `{}`!",
                tracing_prefix,
                err,
                username
            );
            let error: ResponseError<_> = (
                StatusCode::INTERNAL_SERVER_ERROR,
                LoginUserErrors::ErrorObtainingPassword,
            )
                .into();
            Err(error)?
        }
    };
    tracing::debug!("{} Username found! Extracting salt...", tracing_prefix);
    let salt = match obtain_salt(&db_password) {
        Ok(r) => r,
        Err(err) => {
            tracing::error!(
                "{} An error `{:?}` occurred while decoding salt bytes!",
                tracing_prefix,
                err
            );
            let error: ResponseError<_> = (
                StatusCode::INTERNAL_SERVER_ERROR,
                LoginUserErrors::ErrorDecodingSalt,
            )
                .into();
            Err(error)?
        }
    };
    tracing::debug!("{} Salt extracted!", tracing_prefix);

    tracing::debug!("{} Checking if passwords match...", tracing_prefix);
    let encripted = encrypt_password_with_salt(&password, &salt);

    if encripted != db_password {
        tracing::error!("{} Passwords don't match!", tracing_prefix);
        let err: ResponseError<_> =
            (StatusCode::BAD_REQUEST, LoginUserErrors::PasswordsDontMatch).into();
        Err(err)?
    }

    tracing::debug!("{} Passwords match! Generating session...", tracing_prefix);

    // TODO Generate session in DB...
    let session_id = Uuid::new_v4().to_string();
    let expire_date = Utc::now() + Duration::days(7);
    if let Err(err) = conn
        .execute(
            "INSERT INTO sf_session(session_id, user_id, expire_date) VALUES ($1, $2, $3)",
            &[&session_id, &user_id, &expire_date],
        )
        .await
    {
        tracing::error!(
            "{} An error `{}` occurred while trying to create session for user `{}`!",
            tracing_prefix,
            err,
            user_id
        );
        let err: ResponseError<_> = (
            StatusCode::INTERNAL_SERVER_ERROR,
            LoginUserErrors::ErrorCreatingSession,
        )
            .into();
        Err(err)?
    }
    tracing::debug!(
        "{} Session generated for user `{}`!",
        tracing_prefix,
        user_id
    );

    tracing::debug!("{} Generating JWT...", tracing_prefix);
    let user_id = Uuid::new_v4().to_string();
    let token = JWT_Token {
        user_id,
        expire_date,
        username,
    };

    let token = match generate_jwt(APP_SECRET, token) {
        Ok(t) => t,
        Err(err) => {
            tracing::error!(
                "{} An error `{:?}` occurred while generating the JWT",
                tracing_prefix,
                err
            );
            let error: ResponseError<_> = (
                StatusCode::INTERNAL_SERVER_ERROR,
                LoginUserErrors::CouldntGenerateJWT,
            )
                .into();
            Err(error)?
        }
    };
    tracing::debug!("{} JWT generated successfully!", tracing_prefix);

    tracing::debug!("{} DONE", tracing_prefix);
    Ok(Json(LoginUserResponse { token }))
}
