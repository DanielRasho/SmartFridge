use std::{
    fmt::Display,
    sync::{atomic::AtomicUsize, Arc},
};

use axum::{response::IntoResponse, Json};
use hyper::StatusCode;
use serde::{Deserialize, Serialize};
use tokio_postgres::Client;
use uuid::Uuid;

use crate::{
    extract_jwt, is_session_valid,
    models::{AppThemes, UserSettings},
    responses::ResponseError,
    APP_SECRET,
};

#[derive(Debug)]
pub enum SaveSettingsErrors {
    InvalidPayload { payload: String },
    NoDBConnection,
    InvalidJWT,
    ErrorCheckingIfSessionIsValid,
    JWTExpired,
    ErrorSavingSettings,
}

impl Display for SaveSettingsErrors {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        write!(f, "{:?}", self)
    }
}

#[derive(Debug, Deserialize)]
struct SaveSettingsPayload {
    token: String,
    settings: UserSettingsPayload,
}

/// Contains the theme id and all the options the user is allowed to change.
#[derive(Debug, Serialize, Deserialize)]
struct UserSettingsPayload {
    #[serde(rename = "SettingsId")]
    settings_id: Uuid,

    #[serde(rename = "Theme")]
    theme: AppThemes,
}

static ID: AtomicUsize = AtomicUsize::new(0);

pub async fn save_settings(
    payload: Json<serde_json::Value>,
    client: Arc<Option<Client>>,
) -> Result<impl IntoResponse, ResponseError<SaveSettingsErrors>> {
    let id = ID.fetch_add(1, std::sync::atomic::Ordering::AcqRel);
    let tracing_prefix = format!("/settings/save - {}:", id);

    tracing::debug!("{} START", tracing_prefix);

    tracing::debug!("{} Parsing payload...", tracing_prefix);
    let SaveSettingsPayload { token, settings } = match serde_json::from_value(payload.0.clone()) {
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
                SaveSettingsErrors::InvalidPayload {
                    payload: payload.0.to_string(),
                },
            )
                .into();
            Err(error)?
        }
    };
    tracing::debug!("{} Payload parsed successfully!", tracing_prefix);

    tracing::debug!("{} Extracting JWT...", tracing_prefix);
    let token_info = match extract_jwt(APP_SECRET, &token) {
        Ok(t) => t,
        Err(_) => {
            tracing::error!(
                "{} An error occurred while extracting the JWT `{}`",
                tracing_prefix,
                token
            );
            let error: ResponseError<_> =
                (StatusCode::BAD_REQUEST, SaveSettingsErrors::InvalidJWT).into();
            Err(error)?
        }
    };
    tracing::debug!("{} JWT extracted successfully!", tracing_prefix);

    tracing::debug!("{} Checking DB connection...", tracing_prefix);
    let conn = client.as_ref().as_ref().ok_or_else(|| {
        tracing::error!("{} No DB connection found!", tracing_prefix);
        let error: ResponseError<_> = (
            StatusCode::INTERNAL_SERVER_ERROR,
            SaveSettingsErrors::NoDBConnection,
        )
            .into();
        error
    })?;
    tracing::debug!("{} DB connection found!", tracing_prefix);

    tracing::debug!("{} Checking if session is valid...", tracing_prefix);
    if let Err(err) = is_session_valid(token_info, conn).await {
        tracing::error!(
            "{} An error `{:?}` occurred while checking if session is valid!",
            tracing_prefix,
            err
        );
        let error: ResponseError<_> = match err {
            crate::IsSessionValidErrors::InternalDBError(_) => (
                StatusCode::INTERNAL_SERVER_ERROR,
                SaveSettingsErrors::ErrorCheckingIfSessionIsValid,
            ),
            crate::IsSessionValidErrors::InvalidSessionData {
                current_date,
                db_expire_date,
            } => (StatusCode::UNAUTHORIZED, SaveSettingsErrors::JWTExpired),
            _ => (
                StatusCode::BAD_REQUEST,
                SaveSettingsErrors::ErrorCheckingIfSessionIsValid,
            ),
        }
        .into();
        Err(error)?
    };
    tracing::debug!("{} Session is valid!", tracing_prefix);

    tracing::debug!("{} Saving settings in DB...", tracing_prefix);
    let theme = format!("{:?}", settings.theme);
    if let Err(err) = conn
        .execute(
            "UPDATE sf_settings WHERE settings_id=$1 SET theme=$2",
            &[&settings.settings_id.to_string(), &theme],
        )
        .await
    {
        tracing::error!(
            "{} An error `{:?}` occurred while updating settings `{}`",
            tracing_prefix,
            err,
            settings.settings_id
        );
        let error: ResponseError<_> = (
            StatusCode::INTERNAL_SERVER_ERROR,
            SaveSettingsErrors::ErrorSavingSettings,
        )
            .into();
        Err(error)?
    }

    tracing::debug!("{} DONE", tracing_prefix);
    Ok(StatusCode::OK)
}
