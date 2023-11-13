use std::{
    fmt::Display,
    sync::{atomic::AtomicUsize, Arc},
};

use axum::{response::IntoResponse, Json};
use hyper::StatusCode;
use serde::{Deserialize, Serialize};
use tokio_postgres::Client;
use uuid::Uuid;

use crate::{extract_jwt, is_session_valid, responses::ResponseError, APP_SECRET};

#[derive(Debug, Serialize)]
pub enum AddIngredientErrors {
    InvalidPayload { payload: String },
    InvalidJWT,
    ErrorCheckingIfSessionIsValid,
    JWTExpired,
    DBConnectionNotFound,
    ErrorInsertingIngredientIntoDB,
    NoIngredientInserted,
}

impl Display for AddIngredientErrors {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        write!(f, "{:?}", self)
    }
}

#[derive(Debug, Deserialize)]
pub struct AddIngredientPayload {
    token: String,
    ingredient: IngredientPayload,
}

/// Represents an ingredient that will be created.
///
/// Creating the ingredient id shouldn't be a responsibility of the client.
/// That's why this object doesn't have that.
#[derive(Debug, Serialize, Deserialize)]
pub struct IngredientPayload {
    #[serde(rename = "UserId")]
    pub user_id: Uuid,

    #[serde(rename = "ExpireDate")]
    pub expire_date: chrono::DateTime<chrono::Utc>,

    #[serde(rename = "Name")]
    pub name: String,

    #[serde(rename = "Category")]
    pub category: String,

    #[serde(rename = "Quantity")]
    pub quantity: u16,

    #[serde(rename = "Unit")]
    pub unit: String,
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

    let conn = client.as_ref().as_ref().ok_or_else(|| {
        tracing::error!(
            "{} DB Connection not found! Couldn't add ingredient `{:?}` into DB!",
            tracing_prefix,
            ingredient
        );
        let error: ResponseError<_> = (
            StatusCode::INTERNAL_SERVER_ERROR,
            AddIngredientErrors::DBConnectionNotFound,
        )
            .into();
        error
    })?;

    tracing::debug!("{} DB Connection found!", tracing_prefix);

    match is_session_valid(token_info, conn).await {
        Ok(_) => {}
        Err(err) => {
            tracing::error!(
                "{} An error occurred `{:?}` while checkinf if session is valid!",
                tracing_prefix,
                err
            );
            let error: ResponseError<_> = match err {
                crate::IsSessionValidErrors::InternalDBError(_) => (
                    StatusCode::INTERNAL_SERVER_ERROR,
                    AddIngredientErrors::ErrorCheckingIfSessionIsValid,
                ),
                crate::IsSessionValidErrors::InvalidSessionData {
                    current_date: _,
                    db_expire_date: _,
                } => (StatusCode::UNAUTHORIZED, AddIngredientErrors::JWTExpired),
                _ => (
                    StatusCode::BAD_REQUEST,
                    AddIngredientErrors::ErrorCheckingIfSessionIsValid,
                ),
            }
            .into();

            Err(error)?
        }
    }

    tracing::debug!("{} Inserting ingredient `{:?}`", tracing_prefix, ingredient);
    let ingredient_id = Uuid::new_v4().to_string();
    match conn
        .execute(
            "INSERT INTO sf_ingredient (ingredient_id, user_id, name, expire_date, category, quantity, unit) VALUES ($1, $2, $3, $4, $5, $6, $7)",
            &[
                &ingredient_id,
                &ingredient.user_id.to_string(),
                &ingredient.name,
                &ingredient.expire_date,
                &ingredient.category,
                &(ingredient.quantity as i16),
                &ingredient.unit,
            ],
        )
        .await
    {
        Ok(rows_modified) => {
            if rows_modified == 0 {
                tracing::error!(
                    "{} No ingredient inserted into DB! Reason: Unknown",
                    tracing_prefix
                );
                let error: ResponseError<_> = (
                    StatusCode::INTERNAL_SERVER_ERROR,
                    AddIngredientErrors::NoIngredientInserted,
                )
                    .into();
                Err(error)?
            } else {
                tracing::debug!(
                    "{} Ingredient with ID `{}` inserted into DB!",
                    tracing_prefix,
                    ingredient_id
                );
            }
        }
        Err(err) => {
            tracing::error!(
                "{} An error occurred `{:?}` while trying to insert an ingredient `{:?}`!",
                tracing_prefix,
                err,
                ingredient
            );
            let error: ResponseError<_> = (
                StatusCode::INTERNAL_SERVER_ERROR,
                AddIngredientErrors::ErrorInsertingIngredientIntoDB,
            )
                .into();
            Err(error)?
        }
    }

    tracing::debug!("{} DONE!", tracing_prefix);
    Ok(StatusCode::OK)
}
