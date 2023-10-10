use std::{fmt::Display, sync::atomic::AtomicUsize};

use axum::{response::IntoResponse, Json};
use hyper::StatusCode;
use serde::Deserialize;

use crate::responses::ResponseError;

#[derive(Debug)]
pub enum RegisterUserErrors {
    NoDBConnection,
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
) -> Result<impl IntoResponse, ResponseError<RegisterUserErrors>> {
    let id = ID.fetch_add(1, std::sync::atomic::Ordering::AcqRel);
    let tracing_prefix = format!("/REGISTER_USER {}:", id);

    tracing::debug!("{} START", tracing_prefix);

    // TODO Check if username exists...

    // TODO Encrypt password...
    
    // TODO Insert into DB...

    tracing::debug!("{} DONE", tracing_prefix);
    Ok((StatusCode::OK, ""))
}
