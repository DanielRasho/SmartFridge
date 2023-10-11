use std::{fmt::Display, sync::{atomic::AtomicUsize, Arc}};

use axum::{response::IntoResponse, Json};
use chrono::{Duration, Utc};
use hyper::StatusCode;
use serde::Deserialize;
use tokio_postgres::Client;

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

static ID: AtomicUsize = AtomicUsize::new(0);

pub async fn get_ingredients(
    payload: Json<serde_json::Value>,
    client: Arc<Option<Client>>,
) -> Result<impl IntoResponse, ResponseError<GetIngredientsErrors>> {
    let id = ID.fetch_add(1, std::sync::atomic::Ordering::AcqRel);
    let tracing_prefix = format!("/GET_INGREDIENT - {}:", id);

    tracing::debug!("{} START", tracing_prefix);

    tracing::debug!("{} Parsing payload...", tracing_prefix);
    let GetIngredientsPayload { token } = match serde_json::from_value(payload.0.clone()) {
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
                GetIngredientsErrors::InvalidPayloadFormat {
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
        Err(_) => {
            tracing::error!(
                "{} An error occurred while extracting the JWT `{}`",
                tracing_prefix,
                token
            );
            let error: ResponseError<_> =
                (StatusCode::BAD_REQUEST, GetIngredientsErrors::InvalidJWT).into();
            Err(error)?
        }
    };
    tracing::debug!("{} JWT extracted successfully!", tracing_prefix);

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

    tracing::debug!("{} DONE", tracing_prefix);
    Ok(Json(recipes))
}
