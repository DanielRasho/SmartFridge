use std::fmt::Display;

use axum::{response::IntoResponse, Json};
use hyper::StatusCode;
use serde::Deserialize;

use crate::{models::UserSettings, responses::ResponseError};

#[derive(Debug)]
pub enum LoginUserErrors {
    InvalidPayload { payload: String },
    NoDBConnection,
}

impl Display for LoginUserErrors {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        write!(f, "{:?}", self)
    }
}

#[derive(Debug, Deserialize)]
struct SaveSettingsPayload {
    token: String,
    settings: UserSettings,
}

pub async fn save_settings(
    payload: Json<serde_json::Value>,
) -> Result<impl IntoResponse, ResponseError<LoginUserErrors>> {
    let SaveSettingsPayload { token, settings } = match serde_json::from_value(payload.0.clone()) {
        Ok(p) => p,
        Err(err) => {
            tracing::error!(
                "An error `{:?}` occurred while parsing payload {}",
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

    // TODO Check if token is valid...

    // TODO Save settings in DB...

    Ok(StatusCode::OK)
}
