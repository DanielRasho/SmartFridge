use std::fmt::Display;

use axum::{response::IntoResponse, Json};
use chrono::{Duration, Utc};
use hyper::StatusCode;
use serde::{Deserialize, Serialize};
use uuid::Uuid;

use crate::{generate_jwt, models::JWT_Token, responses::ResponseError};

#[derive(Debug)]
pub enum LoginUserErrors {
    InvalidPayload { payload: String },
    NoDBConnection,
    CouldntGenerateJWT(crate::GenerateJWTErrors),
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

pub const APP_SECRET: &[u8] = b"super-secret-key";

pub async fn login_user(
    payload: Json<serde_json::Value>,
) -> Result<impl IntoResponse, ResponseError<LoginUserErrors>> {
    let LoginUserPayload { username, password } = match serde_json::from_value(payload.0.clone()) {
        Ok(p) => p,
        Err(err) => {
            tracing::error!("An error `{:?}` occurred while parsing payload {}", err, payload.0);
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
    //TODO Assume DB check passed...

    let user_id = Uuid::new_v4().to_string();
    let expired_date = Utc::now() + Duration::minutes(30);
    let token = JWT_Token {
        user_id,
        expired_date,
        username,
    };

    let token = match generate_jwt(APP_SECRET, token) {
        Ok(t) => t,
        Err(err) => {
            tracing::error!("An error ocurred while generating the JWT {:?}", err);
            let error: ResponseError<_> = (
                StatusCode::INTERNAL_SERVER_ERROR,
                LoginUserErrors::CouldntGenerateJWT(err),
            )
                .into();
            Err(error)?
        }
    };

    Ok(Json(LoginUserResponse { token }))
}
