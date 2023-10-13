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
    models::{Ingredient, Recipe},
    responses::ResponseError,
    APP_SECRET,
};

#[derive(Debug)]
pub enum GetRecipesError {
    InvalidPayloadFormat { payload: String },
    InvalidJWT,
}

impl Display for GetRecipesError {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        write!(f, "{:?}", self)
    }
}

#[derive(Debug, Deserialize)]
struct GetRecipesPayload {
    token: String,
}

static ID: AtomicUsize = AtomicUsize::new(0);

pub async fn get_recipes(
    payload: Json<serde_json::Value>,
    client: Arc<Option<Client>>,
) -> Result<impl IntoResponse, ResponseError<GetRecipesError>> {
    let id = ID.fetch_add(1, std::sync::atomic::Ordering::AcqRel);
    let tracing_prefix = format!("/recipes - {}:", id);

    tracing::debug!("{} START", tracing_prefix);

    tracing::debug!("{} Parsing payload...", tracing_prefix);
    let GetRecipesPayload { token } = match serde_json::from_value(payload.0.clone()) {
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
                GetRecipesError::InvalidPayloadFormat {
                    payload: payload.0.to_string(),
                },
            )
                .into();
            Err(error)?
        }
    };
    tracing::debug!("{} Payload parsed successfully!", tracing_prefix);

    tracing::debug!("{} Parsing JWT...", tracing_prefix);
    let token_info = match extract_jwt(APP_SECRET, &token) {
        Ok(t) => t,
        Err(_) => {
            tracing::error!(
                "{} An error occurred while extracting the JWT `{}`",
                tracing_prefix,
                token
            );
            let error: ResponseError<_> =
                (StatusCode::BAD_REQUEST, GetRecipesError::InvalidJWT).into();
            Err(error)?
        }
    };
    tracing::debug!("{} JWT parsed successfully!", tracing_prefix);

    // TODO Validate token with DB

    // TODO Get recipes from API

    let recipes = vec![
        Recipe {
            recipe_id: Uuid::new_v4(),
            title: "Test Recipe #1".to_string(),
            banner: "https://lh3.googleusercontent.com/uSzqXtfkILNLvbaIhzU8LK-iKCOG6w60AXCXEhWNzY-UrUaLypLSpH10MoloHfa96NPONh19gIz0ebK-XL7v".to_string(),
            tags: vec!["Breakfast".to_string(), "Egg".to_string()],
            ingredients: vec![Ingredient { expire_date: Utc::now(), name: "Eggs".to_string(), category: "Dairy".to_string(), quantity: 2, unit: "Eggs".to_string(), ingredient_id: Uuid::new_v4() }],
            source: "http://www.yummly.com/recipe/Plant-Based-Breakfast-Bowl-9118197".to_string(),
        },
        Recipe {
            recipe_id: Uuid::new_v4(),
            title: "Test Recipe #2".to_string(),
            banner: "https://lh3.googleusercontent.com/uSzqXtfkILNLvbaIhzU8LK-iKCOG6w60AXCXEhWNzY-UrUaLypLSpH10MoloHfa96NPONh19gIz0ebK-XL7v".to_string(),
            tags: vec!["Breakfast".to_string(), "Egg".to_string()],
            ingredients: vec![Ingredient { expire_date: Utc::now(), name: "Eggs".to_string(), category: "Dairy".to_string(), quantity: 2, unit: "Eggs".to_string(), ingredient_id: Uuid::new_v4() }],
            source: "http://www.yummly.com/recipe/Plant-Based-Breakfast-Bowl-9118197".to_string(),
        },
        Recipe {
            recipe_id: Uuid::new_v4(),
            title: "Test Recipe #3".to_string(),
            banner: "https://lh3.googleusercontent.com/efGuFTcoR-Atb8-OgBL8PMCVbPwRQANTX0ZVgllhlzBkVc92d0G9LkapW1TiNmTL4iZJNlPIkyGKS1ODOUNCOxM".to_string(),
            tags: vec!["Breakfast".to_string(), "Egg".to_string()],
            ingredients: vec![Ingredient { expire_date: Utc::now(), name: "Eggs".to_string(), category: "Dairy".to_string(), quantity: 2, unit: "Eggs".to_string(), ingredient_id: Uuid::new_v4() }],
            source: "http://www.yummly.com/recipe/Plant-Based-Breakfast-Bowl-9118197".to_string(),
        },
        Recipe {
            recipe_id: Uuid::new_v4(),
            title: "Test Recipe #4".to_string(),
            banner: "https://lh3.googleusercontent.com/JumnqUM5mRUraff-j2tx7Oy1c9oXbGJP8ba4fDcF3OqYC2W_2R_Tug1AVJhbwtZcJqaVf5MpVGAfHP1VtHIKzw".to_string(),
            tags: vec!["Breakfast".to_string(), "Egg".to_string()],
            ingredients: vec![Ingredient { expire_date: Utc::now(), name: "Eggs".to_string(), category: "Dairy".to_string(), quantity: 2, unit: "Eggs".to_string(), ingredient_id: Uuid::new_v4()}],
            source: "http://www.yummly.com/recipe/Plant-Based-Breakfast-Bowl-9118197".to_string(),
        },
    ];

    tracing::debug!("{} DONE", tracing_prefix);
    Ok(Json(recipes))
}
