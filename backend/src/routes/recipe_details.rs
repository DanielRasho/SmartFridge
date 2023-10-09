use std::fmt::Display;

use axum::{response::IntoResponse, Json};
use chrono::Utc;
use hyper::StatusCode;
use serde::Deserialize;

use crate::{
    extract_jwt,
    models::{Ingredient, Recipe},
    responses::ResponseError,
};

use super::login_user::APP_SECRET;

#[derive(Debug)]
pub enum RecipeDetailsErrors {
    InvalidPayloadFormat { payload: String },
    InvalidJWT,
}

impl Display for RecipeDetailsErrors {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        write!(f, "{:?}", self)
    }
}

#[derive(Debug, Deserialize)]
struct RecipeDetailsPayload {
    token: String,
    #[serde(rename(deserialize = "recipeId"))]
    recipe_id: String,
}

pub async fn recipe_details(
    payload: Json<serde_json::Value>,
) -> Result<impl IntoResponse, ResponseError<RecipeDetailsErrors>> {
    let RecipeDetailsPayload { token, recipe_id } = match serde_json::from_value(payload.0.clone())
    {
        Ok(p) => p,
        Err(err) => {
            tracing::error!(
                "An error {:?} occurred while parsing the payload `{}`",
                err,
                payload.0
            );
            let error: ResponseError<_> = (
                StatusCode::BAD_REQUEST,
                RecipeDetailsErrors::InvalidPayloadFormat {
                    payload: payload.0.to_string(),
                },
            )
                .into();
            Err(error)?
        }
    };

    let token_info = match extract_jwt(APP_SECRET, &token) {
        Ok(t) => t,
        Err(_) => {
            tracing::error!("An error occurred while extracting the JWT `{}`", token);
            let error: ResponseError<_> =
                (StatusCode::BAD_REQUEST, RecipeDetailsErrors::InvalidJWT).into();
            Err(error)?
        }
    };

    // TODO Validate token with DB

    // TODO Get recipe from API

    let recipe = Recipe {
            title: "Test Recipe #3".to_string(),
            banner: "https://lh3.googleusercontent.com/efGuFTcoR-Atb8-OgBL8PMCVbPwRQANTX0ZVgllhlzBkVc92d0G9LkapW1TiNmTL4iZJNlPIkyGKS1ODOUNCOxM".to_string(),
            tags: vec!["Breakfast".to_string(), "Egg".to_string()],
            ingredients: vec![Ingredient { expire_date: Utc::now(), name: "Eggs".to_string(), category: "Dairy".to_string(), quantity: 2, unit: "Eggs".to_string() }],
            source: "http://www.yummly.com/recipe/Plant-Based-Breakfast-Bowl-9118197".to_string(),
        };

    Ok(Json(recipe))
}
