use std::{
    fmt::Display,
    sync::{atomic::AtomicUsize, Arc},
};

use axum::{response::IntoResponse, Json};
use hyper::StatusCode;
use serde::Deserialize;
use tokio_postgres::Client;

use crate::{encrypt_password, responses::ResponseError};

#[derive(Debug)]
pub enum RegisterUserErrors {
    NoDBConnection,
    InvalidPayload { payload: String },
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

    // TODO Check if username exists...

    tracing::debug!("{} Encrypting password...", tracing_prefix);
    let encrypted = encrypt_password(password);
    tracing::debug!("{} Password encrypted successfully!", tracing_prefix);

    // TODO Insert into DB...

    tracing::debug!("{} DONE", tracing_prefix);
    Ok((StatusCode::OK, ""))
}
