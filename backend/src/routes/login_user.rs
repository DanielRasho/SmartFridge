use std::{
    fmt::Display,
    sync::{atomic::AtomicUsize, Arc},
};

use axum::{response::IntoResponse, Json};
use chrono::{Duration, Utc};
use hyper::StatusCode;
use serde::{Deserialize, Serialize};
use tokio_postgres::Client;
use uuid::Uuid;

use crate::{generate_jwt, models::JWT_Token, responses::ResponseError, APP_SECRET};

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

    // TODO Check if pasword is correct...

    // TODO Generate session in DB...

    tracing::debug!("{} Generating JWT...", tracing_prefix);
    let user_id = Uuid::new_v4().to_string();
    let expired_date = Utc::now() + Duration::days(7);
    let token = JWT_Token {
        user_id,
        expired_date,
        username,
    };

    let token = match generate_jwt(APP_SECRET, token) {
        Ok(t) => t,
        Err(err) => {
            tracing::error!(
                "{} An error ocurred while generating the JWT {:?}",
                tracing_prefix,
                err
            );
            let error: ResponseError<_> = (
                StatusCode::INTERNAL_SERVER_ERROR,
                LoginUserErrors::CouldntGenerateJWT(err),
            )
                .into();
            Err(error)?
        }
    };
    tracing::debug!("{} JWT generated successfully!", tracing_prefix);

    tracing::debug!("{} DONE", tracing_prefix);
    Ok(Json(LoginUserResponse { token }))
}
