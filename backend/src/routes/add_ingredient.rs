use std::fmt::Display;

use axum::{response::IntoResponse, Json};
use hyper::StatusCode;
use serde::{Deserialize, Serialize};

use crate::{extract_jwt, models::Ingredient, responses::ResponseError, APP_SECRET};

#[derive(Debug, Serialize)]
pub enum AddIngredientErrors {
    InvalidPayload { payload: String },
    InvalidJWT,
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

pub async fn add_ingredient(
    payload: Json<serde_json::Value>,
) -> Result<impl IntoResponse, ResponseError<AddIngredientErrors>> {
    let AddIngredientPayload { token, ingredient } = match serde_json::from_value(payload.0.clone())
    {
        Ok(p) => p,
        Err(err) => {
            tracing::error!(
                "An error `{:?}` occurred parsing payload `{}`",
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

    let token_info = match extract_jwt(APP_SECRET, &token) {
        Ok(t) => t,
        Err(err) => {
            tracing::error!(
                "An error `{:?}` occurred while extracting the JWT `{}`",
                err,
                token
            );
            let error: ResponseError<_> =
                (StatusCode::BAD_REQUEST, AddIngredientErrors::InvalidJWT).into();
            Err(error)?
        }
    };

    // TODO Check if token is valid...

    // TODO Add ingredient to DB...

    Ok(StatusCode::OK)
}
