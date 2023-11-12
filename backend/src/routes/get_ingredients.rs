use std::{
    fmt::Display,
    sync::{atomic::AtomicUsize, Arc},
};

use axum::{response::IntoResponse, Json};
use chrono::{Duration, Utc};
use hyper::StatusCode;
use serde::Deserialize;
use tokio_postgres::Client;
use uuid::Uuid;

use crate::{
    extract_jwt, is_session_valid, models::Ingredient, responses::ResponseError, APP_SECRET,
};

#[derive(Debug)]
pub enum GetIngredientsErrors {
    InvalidPayloadFormat { payload: String },
    InvalidJWT,
    NoDBConnection,
    InvalidSession,
    ErrorCheckingIfSessionIsValid,
    JWTExpired,
}

impl Display for GetIngredientsErrors {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        write!(f, "{:?}", self)
    }
}

#[derive(Debug, Deserialize)]
struct GetIngredientsPayload {
    token: String,
    user_id: String,
}

static ID: AtomicUsize = AtomicUsize::new(0);

pub async fn get_ingredients(
    payload: Json<serde_json::Value>,
    client: Arc<Option<Client>>,
) -> Result<impl IntoResponse, ResponseError<GetIngredientsErrors>> {
    let id = ID.fetch_add(1, std::sync::atomic::Ordering::AcqRel);
    let tracing_prefix = format!("/ingredients - {}:", id);

    tracing::debug!("{} START", tracing_prefix);

    tracing::debug!("{} Parsing payload...", tracing_prefix);
    let GetIngredientsPayload { token, user_id } = match serde_json::from_value(payload.0.clone()) {
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

    let conn = client.as_ref().as_ref().ok_or_else(|| {
        tracing::error!("{} No DB connection found!", tracing_prefix);
        let error: ResponseError<_> = (
            StatusCode::INTERNAL_SERVER_ERROR,
            GetIngredientsErrors::NoDBConnection,
        )
            .into();
        error
    })?;

    tracing::debug!("{} DB Connection found!", tracing_prefix);

    match is_session_valid(token_info, conn).await {
        Ok(_) => {}
        Err(err) => {
            tracing::error!(
                "{} An error `{:?}` occurred while trying to check if session is valid!",
                tracing_prefix,
                err
            );

            let error: ResponseError<_> = match err {
                crate::IsSessionValidErrors::InternalDBError(_) => (
                    StatusCode::INTERNAL_SERVER_ERROR,
                    GetIngredientsErrors::ErrorCheckingIfSessionIsValid,
                ),
                crate::IsSessionValidErrors::InvalidSessionData {
                    current_date,
                    db_expire_date,
                } => (
                    StatusCode::UNAUTHORIZED,
                    GetIngredientsErrors::InvalidSession,
                ),
                _ => (
                    StatusCode::BAD_REQUEST,
                    GetIngredientsErrors::ErrorCheckingIfSessionIsValid,
                ),
            }
            .into();

            Err(error)?
        }
    }

    // TODO Get Ingredients from DB

    let ingredients = vec![
        Ingredient {
            expire_date: Utc::now() + Duration::days(10),
            name: "Eggs".to_string(),
            category: "Dairy".to_string(),
            quantity: 10,
            unit: "Units".to_string(),
            ingredient_id: Uuid::new_v4(),
            user_id: Uuid::new_v4(),
        },
        Ingredient {
            expire_date: Utc::now() + Duration::days(10),
            name: "Milk".to_string(),
            category: "Dairy".to_string(),
            quantity: 10,
            unit: "L".to_string(),
            ingredient_id: Uuid::new_v4(),
            user_id: Uuid::new_v4(),
        },
        Ingredient {
            expire_date: Utc::now() + Duration::days(10),
            name: "Steak".to_string(),
            category: "Meat".to_string(),
            quantity: 3,
            unit: "Lb".to_string(),
            ingredient_id: Uuid::new_v4(),
            user_id: Uuid::new_v4(),
        },
        Ingredient {
            expire_date: Utc::now() + Duration::days(10),
            name: "Chicken".to_string(),
            category: "Meat".to_string(),
            quantity: 5,
            unit: "Lb".to_string(),
            ingredient_id: Uuid::new_v4(),
            user_id: Uuid::new_v4(),
        },
        Ingredient {
            expire_date: Utc::now() + Duration::days(10),
            name: "Oranges".to_string(),
            category: "Fruit".to_string(),
            quantity: 15,
            unit: "Units".to_string(),
            ingredient_id: Uuid::new_v4(),
            user_id: Uuid::new_v4(),
        },
    ];

    tracing::debug!("{} DONE", tracing_prefix);
    Ok(Json(ingredients))
}
