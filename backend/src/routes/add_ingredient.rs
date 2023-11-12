use std::{
    fmt::Display,
    sync::{atomic::AtomicUsize, Arc},
};

use axum::{response::IntoResponse, Json};
use hyper::StatusCode;
use serde::{Deserialize, Serialize};
use tokio_postgres::Client;

use crate::{extract_jwt, models::Ingredient, responses::ResponseError, APP_SECRET, is_session_valid};

#[derive(Debug, Serialize)]
pub enum AddIngredientErrors {
    InvalidPayload { payload: String },
    InvalidJWT,
    ErrorCheckingIfSessionIsValid,
    JWTExpired,
}

impl Display for AddIngredientErrors {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        write!(f, "{:?}", self)
    }
}

#[derive(Debug, Deserialize)]
pub struct AddIngredientPayload {
    token: String,
    ingredient: Ingredient,
}

static ID: AtomicUsize = AtomicUsize::new(0);

pub async fn add_ingredient(
    payload: Json<serde_json::Value>,
    client: Arc<Option<Client>>,
) -> Result<impl IntoResponse, ResponseError<AddIngredientErrors>> {
    let id = ID.fetch_add(1, std::sync::atomic::Ordering::AcqRel);
    let tracing_prefix = format!("/ingredients/add - {}:", id);

    tracing::debug!("{} START", tracing_prefix);

    tracing::debug!("{} Parsing payload...", tracing_prefix);
    let AddIngredientPayload { token, ingredient } = match serde_json::from_value(payload.0.clone())
    {
        Ok(p) => p,
        Err(err) => {
            tracing::error!(
                "{} An error `{:?}` occurred parsing payload `{}`",
                tracing_prefix,
                err,
                payload.0
            );
            let error: ResponseError<_> = (
                StatusCode::BAD_REQUEST,
                AddIngredientErrors::InvalidPayload {
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
        Err(err) => {
            tracing::error!(
                "{} An error `{:?}` occurred while extracting the JWT `{}`",
                tracing_prefix,
                err,
                token
            );
            let error: ResponseError<_> =
                (StatusCode::BAD_REQUEST, AddIngredientErrors::InvalidJWT).into();
            Err(error)?
        }
    };
    tracing::debug!("{} JWT extracted successfully!", tracing_prefix);

    match is_session_valid(token_info, client.as_ref().as_ref().unwrap()).await {
        Ok(true) => {}
        Ok(false) => {
            tracing::error!("{} The JWT already expired!", tracing_prefix);
            let error: ResponseError<_> =
                (StatusCode::UNAUTHORIZED, AddIngredientErrors::JWTExpired).into();
            Err(error)?
        }
        Err(err) => {
            tracing::error!(
                "{} An error `{:?}` occurred while checking if session is valid!",
                tracing_prefix,
                err
            );
            let error: ResponseError<_> =
                if let crate::IsSessionValidErrors::InternalDBError(_) = err {
                    (
                        StatusCode::INTERNAL_SERVER_ERROR,
                        AddIngredientErrors::ErrorCheckingIfSessionIsValid,
                    )
                } else {
                    (
                        StatusCode::BAD_REQUEST,
                        AddIngredientErrors::ErrorCheckingIfSessionIsValid,
                    )
                }
                .into();
            Err(error)?
        }
    };
    // TODO Add ingredient to DB...

    tracing::debug!("{} DONE!", tracing_prefix);
    Ok(StatusCode::OK)
}
