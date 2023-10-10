use std::fmt::Display;

use axum::{response::IntoResponse, Json};
use chrono::{Duration, Utc};
use hyper::StatusCode;
use serde::Deserialize;

use crate::{extract_jwt, models::Ingredient, responses::ResponseError, APP_SECRET};

#[derive(Debug)]
pub enum GetIngredientsErrors {
    InvalidPayloadFormat { payload: String },
    InvalidJWT,
}

impl Display for GetIngredientsErrors {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        write!(f, "{:?}", self)
    }
}

#[derive(Debug, Deserialize)]
struct GetIngredientsPayload {
    token: String,
}

pub async fn get_ingredients(
    payload: Json<serde_json::Value>,
) -> Result<impl IntoResponse, ResponseError<GetIngredientsErrors>> {
    let GetIngredientsPayload { token } = match serde_json::from_value(payload.0.clone()) {
        Ok(p) => p,
        Err(err) => {
            tracing::error!(
                "An error {:?} occurred while parsing the payload `{}`",
                err,
                payload.0
            );
            let error: ResponseError<_> = (
                StatusCode::BAD_REQUEST,
                GetIngredientsErrors::InvalidPayloadFormat {
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
                (StatusCode::BAD_REQUEST, GetIngredientsErrors::InvalidJWT).into();
            Err(error)?
        }
    };

    // TODO Validate token with DB

    // TODO Get recipes from API

    let recipes = vec![
        Ingredient {
            expire_date: Utc::now() + Duration::days(10),
            name: "Eggs".to_string(),
            category: "Dairy".to_string(),
            quantity: 10,
            unit: "Units".to_string(),
        },
        Ingredient {
            expire_date: Utc::now() + Duration::days(10),
            name: "Milk".to_string(),
            category: "Dairy".to_string(),
            quantity: 10,
            unit: "L".to_string(),
        },
        Ingredient {
            expire_date: Utc::now() + Duration::days(10),
            name: "Steak".to_string(),
            category: "Meat".to_string(),
            quantity: 3,
            unit: "Lb".to_string(),
        },
        Ingredient {
            expire_date: Utc::now() + Duration::days(10),
            name: "Chicken".to_string(),
            category: "Meat".to_string(),
            quantity: 5,
            unit: "Lb".to_string(),
        },
        Ingredient {
            expire_date: Utc::now() + Duration::days(10),
            name: "Oranges".to_string(),
            category: "Fruit".to_string(),
            quantity: 15,
            unit: "Units".to_string(),
        },
    ];

    Ok(Json(recipes))
}
