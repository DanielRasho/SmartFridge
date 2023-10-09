use std::fmt::Display;

use axum::{debug_handler, response::IntoResponse, Json};
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

pub async fn register_user(
    payload: Json<serde_json::Value>,
) -> Result<impl IntoResponse, ResponseError<RegisterUserErrors>> {
    Ok((StatusCode::OK, ""))
}
