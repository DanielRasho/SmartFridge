use std::fmt::Display;

use axum::{response::IntoResponse, Json};
use hyper::StatusCode;
use serde::Deserialize;

use crate::responses::ResponseError;

#[derive(Debug)]
pub enum LogoutUserErrors {
    InvalidPayload { payload: String },
    NoDBConnection,
}

impl Display for LogoutUserErrors {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        write!(f, "{:?}", self)
    }
}

#[derive(Debug, Deserialize)]
struct LogoutUserPayload {
    token: String,
}

pub async fn logout(
    payload: Json<serde_json::Value>,
) -> Result<impl IntoResponse, ResponseError<LogoutUserErrors>> {
    let LogoutUserPayload { token } = match serde_json::from_value(payload.0.clone()) {
        Ok(p) => p,
        Err(err) => {
            tracing::error!(
                "An error `{:?}` occurred while parsing payload {}",
                err,
                payload.0
            );
            let error: ResponseError<_> = (
                StatusCode::BAD_REQUEST,
                LogoutUserErrors::InvalidPayload {
                    payload: payload.0.to_string(),
                },
            )
                .into();
            Err(error)?
        }
    };

    // TODO Check token with DB...

    // TODO Invalidate token...

    // TODO Close DB Session...

    Ok(StatusCode::OK)
}
