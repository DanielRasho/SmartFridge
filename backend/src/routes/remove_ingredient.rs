use std::{
    fmt::Display,
    sync::{atomic::AtomicUsize, Arc},
};

use axum::{response::IntoResponse, Json};
use hyper::StatusCode;
use serde::{Deserialize, Serialize};
use tokio_postgres::Client;

use crate::{extract_jwt, models::Ingredient, responses::ResponseError, APP_SECRET};

#[derive(Debug, Serialize)]
pub enum RemoveIngredientErrors {
    InvalidPayload { payload: String },
    InvalidJWT,
}

impl Display for RemoveIngredientErrors {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        write!(f, "{:?}", self)
    }
}

#[derive(Debug, Deserialize)]
pub struct RemoveIngredientPayload {
    token: String,
    ingredient_id: String,
}

static ID: AtomicUsize = AtomicUsize::new(0);

pub async fn remove_ingredient(
    payload: Json<serde_json::Value>,
    client: Arc<Option<Client>>,
) -> Result<impl IntoResponse, ResponseError<RemoveIngredientErrors>> {
    let id = ID.fetch_add(1, std::sync::atomic::Ordering::AcqRel);
    let tracing_prefix = format!("/ingredients/remove - {}:", id);

    tracing::debug!("{} START", tracing_prefix);

    tracing::debug!("{} Parsing payload...", tracing_prefix);
    let RemoveIngredientPayload { token, ingredient_id } =
        match serde_json::from_value(payload.0.clone()) {
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
                    RemoveIngredientErrors::InvalidPayload {
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
                (StatusCode::BAD_REQUEST, RemoveIngredientErrors::InvalidJWT).into();
            Err(error)?
        }
    };
    tracing::debug!("{} JWT extracted successfully!", tracing_prefix);

    // TODO Check if token is valid...

    // TODO Remove ingredient from DB...

    tracing::debug!("{} DONE", tracing_prefix);
    Ok(StatusCode::OK)
}