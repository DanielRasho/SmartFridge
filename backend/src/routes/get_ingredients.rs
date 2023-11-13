use std::{
    fmt::Display,
    sync::{atomic::AtomicUsize, Arc},
};

use axum::{response::IntoResponse, Json};
use chrono::{Duration, Utc};
use hyper::StatusCode;
use serde::Deserialize;
use tokio_postgres::{Client, Row};
use uuid::Uuid;

use crate::{
    extract_jwt, from_db_to_value, is_session_valid, models::Ingredient, parse_db_ingredient,
    responses::ResponseError, APP_SECRET,
};

#[derive(Debug)]
pub enum GetIngredientsErrors {
    InvalidPayloadFormat { payload: String },
    InvalidJWT,
    NoDBConnection,
    InvalidSession,
    ErrorCheckingIfSessionIsValid,
    JWTExpired,
    CouldntRetrieveRecipesFromDB,
    InvalidIngredientFormatFromDB,
}

impl Display for GetIngredientsErrors {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        write!(f, "{:?}", self)
    }
}

#[derive(Debug, Deserialize)]
struct GetIngredientsPayload {
    token: String,
    user_id: Uuid,
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

    tracing::debug!("{} Checking if session is valid...", tracing_prefix);
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
                    current_date: _,
                    db_expire_date: _,
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
    tracing::debug!("{} Session is valid!", tracing_prefix);

    tracing::debug!("{} Getting ingredients from DB...", tracing_prefix);
    let db_result = conn
        .query(
            "SELECT * FROM sf_ingredient WHERE user_id=$1",
            &[&user_id.to_string()],
        )
        .await
        .map_err(|err| {
            tracing::error!(
                "{} An error `{:?}` while trying to get ingredients for user `{}`",
                tracing_prefix,
                err,
                user_id
            );
            let error: ResponseError<_> = (
                StatusCode::INTERNAL_SERVER_ERROR,
                GetIngredientsErrors::CouldntRetrieveRecipesFromDB,
            )
                .into();
            error
        })?;
    tracing::debug!("{} Got ingredients from user!", tracing_prefix);

    tracing::debug!("{} Parsing ingredients from db...", tracing_prefix);
    let ingredients = db_result
        .iter()
        .map(|row| {
            parse_db_ingredient(row, &tracing_prefix).ok_or_else(|| {
                let error: ResponseError<_> = (
                    StatusCode::INTERNAL_SERVER_ERROR,
                    GetIngredientsErrors::InvalidIngredientFormatFromDB,
                )
                    .into();
                error
            })
        })
        .collect::<Result<Vec<Ingredient>, ResponseError<GetIngredientsErrors>>>()?;
    tracing::debug!("{} Ingredients parsed!", tracing_prefix);

    tracing::debug!("{} DONE", tracing_prefix);
    Ok(Json(ingredients))
}
