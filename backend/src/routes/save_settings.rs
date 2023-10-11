use std::{
    fmt::Display,
    sync::{atomic::AtomicUsize, Arc},
};

use axum::{response::IntoResponse, Json};
use hyper::StatusCode;
use serde::Deserialize;
use tokio_postgres::Client;

use crate::{extract_jwt, models::UserSettings, responses::ResponseError, APP_SECRET};

#[derive(Debug)]
pub enum SaveSettingsErrors {
    InvalidPayload { payload: String },
    NoDBConnection,
    InvalidJWT,
}

impl Display for SaveSettingsErrors {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        write!(f, "{:?}", self)
    }
}

#[derive(Debug, Deserialize)]
struct SaveSettingsPayload {
    token: String,
    settings: UserSettings,
}

static ID: AtomicUsize = AtomicUsize::new(0);

pub async fn save_settings(
    payload: Json<serde_json::Value>,
    client: Arc<Option<Client>>,
) -> Result<impl IntoResponse, ResponseError<SaveSettingsErrors>> {
    let id = ID.fetch_add(1, std::sync::atomic::Ordering::AcqRel);
    let tracing_prefix = format!("/SAVE_SETTINGS - {}:", id);

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

    // TODO Check if token is valid...

    // TODO Save settings in DB...

    tracing::debug!("{} DONE", tracing_prefix);
    Ok(StatusCode::OK)
}
