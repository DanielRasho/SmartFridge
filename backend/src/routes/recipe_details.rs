use std::{
    fmt::Display,
    sync::{atomic::AtomicUsize, Arc},
};

use axum::{response::IntoResponse, Json};
use chrono::Utc;
use hyper::StatusCode;
use serde::Deserialize;
use tokio_postgres::Client;
use uuid::Uuid;

use crate::{
    extract_jwt,
    models::{Ingredient, Recipe, RecipeIngredient},
    responses::ResponseError,
    APP_SECRET,
};

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

static ID: AtomicUsize = AtomicUsize::new(0);

pub async fn recipe_details(
    payload: Json<serde_json::Value>,
    client: Arc<Option<Client>>,
) -> Result<impl IntoResponse, ResponseError<RecipeDetailsErrors>> {
    let id = ID.fetch_add(1, std::sync::atomic::Ordering::AcqRel);
    let tracing_prefix = format!("/recipes/details - {}:", id);

    tracing::debug!("{} START", tracing_prefix);

    tracing::debug!("{} Parsing payload...", tracing_prefix);
    let RecipeDetailsPayload { token, recipe_id } = match serde_json::from_value(payload.0.clone())
    {
        Ok(p) => p,
        Err(err) => {
            tracing::error!(
                "{} An error {:?} occurred while parsing the payload `{}`",
                tracing_prefix,
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
    tracing::debug!("{} Payload parsed!", tracing_prefix);

    tracing::debug!("{} Extracting JWT...", tracing_prefix);
    let token_info = match extract_jwt(APP_SECRET, &token) {
        Ok(t) => t,
        Err(_) => {
            tracing::error!(
                "{} An error occurred while extracting the JWT `{}`",
                tracing_prefix,
                token
            );
            let error: ResponseError<_> =
                (StatusCode::BAD_REQUEST, RecipeDetailsErrors::InvalidJWT).into();
            Err(error)?
        }
    };
    tracing::debug!("{} JWT extracted successfully!", tracing_prefix);

    // TODO Validate token with DB

    // TODO Get recipe from API

    let recipe = Recipe {
            recipe_id: "recipe:Plant-Based-Breakfast-Bowl-9118197,recipe,list.recipe.trending".to_owned(),
            title: "Test Recipe #3".to_string(),
            banner: "https://lh3.googleusercontent.com/efGuFTcoR-Atb8-OgBL8PMCVbPwRQANTX0ZVgllhlzBkVc92d0G9LkapW1TiNmTL4iZJNlPIkyGKS1ODOUNCOxM".to_string(),
            tags: vec!["Breakfast".to_string(), "Egg".to_string()],
            ingredients: vec![RecipeIngredient { display: "½ cup riced sweet potato".to_owned(), name: "Lime Juice".to_owned() }],
            source: "http://www.yummly.com/recipe/Plant-Based-Breakfast-Bowl-9118197".to_string(),
        };

    tracing::debug!("{} DONE", tracing_prefix);
    Ok(Json(recipe))
}
