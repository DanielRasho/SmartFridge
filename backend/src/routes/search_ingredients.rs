use std::{
    fmt::{Display},
    sync::{atomic::AtomicUsize, Arc},
};

use axum::{response::IntoResponse, Json};

use hyper::StatusCode;
use serde::Deserialize;
use tokio_postgres::Client;
use uuid::Uuid;

use crate::{
    extract_jwt, is_session_valid, models::Ingredient, parse_db_ingredient,
    responses::ResponseError, APP_SECRET,
};

#[derive(Debug)]
pub enum SearchIngredientErrors {
    InvalidPayloadFormat { payload: String },
    InvalidJWT,
    NoDBConnectionFound,
    ErrorCheckingIfSessionIsValid,
    JWTExpired,
    ErrorRetrievingIngredients,
    InvalidIngredientFormatFromDB,
}

impl Display for SearchIngredientErrors {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        write!(f, "{:?}", self)
    }
}

#[derive(Debug, Deserialize)]
struct SearchIngredientsPayload {
    token: String,
    user_id: Uuid,
    query: String,
}

static ID: AtomicUsize = AtomicUsize::new(0);

pub async fn search_ingredients(
    payload: Json<serde_json::Value>,
    client: Arc<Option<Client>>,
) -> Result<impl IntoResponse, ResponseError<SearchIngredientErrors>> {
    let id = ID.fetch_add(1, std::sync::atomic::Ordering::AcqRel);
    let tracing_prefix = format!("/ingredients/search - {}:", id);

    tracing::debug!("{} START", tracing_prefix);

    tracing::debug!("{} Parsing payload...", tracing_prefix);
    let SearchIngredientsPayload {
        token,
        user_id,
        query,
    } = match serde_json::from_value(payload.0.clone()) {
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
                SearchIngredientErrors::InvalidPayloadFormat {
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
                (StatusCode::BAD_REQUEST, SearchIngredientErrors::InvalidJWT).into();
            Err(error)?
        }
    };
    tracing::debug!("{} JWT extracted successfully!", tracing_prefix);

    tracing::debug!("{} Checking DB connection...", tracing_prefix);
    let conn = client.as_ref().as_ref().ok_or_else(|| {
        tracing::error!("{} No DB connection found!", tracing_prefix);
        let error: ResponseError<_> = (
            StatusCode::INTERNAL_SERVER_ERROR,
            SearchIngredientErrors::NoDBConnectionFound,
        )
            .into();
        error
    })?;
    tracing::debug!("{} DB connection found!", tracing_prefix);

    tracing::debug!("{} Checking if session is valid...", tracing_prefix);
    if let Err(err) = is_session_valid(token_info, conn).await {
        tracing::error!(
            "{} An error `{:?}` occurred while checking if session is valid!",
            tracing_prefix,
            err
        );

        let error: ResponseError<_> = match err {
            crate::IsSessionValidErrors::InternalDBError(_) => (
                StatusCode::INTERNAL_SERVER_ERROR,
                SearchIngredientErrors::ErrorCheckingIfSessionIsValid,
            ),
            crate::IsSessionValidErrors::InvalidSessionData {
                current_date: _,
                db_expire_date: _,
            } => (StatusCode::UNAUTHORIZED, SearchIngredientErrors::JWTExpired),
            _ => (
                StatusCode::BAD_REQUEST,
                SearchIngredientErrors::ErrorCheckingIfSessionIsValid,
            ),
        }
        .into();

        Err(error)?
    }
    tracing::debug!("{} Session is valid!", tracing_prefix);

    tracing::debug!("{} Getting ingredients from API...", tracing_prefix);
    let ingredients = conn
        .query(
            "SELECT * FROM sf_ingredients WHERE user_id=$1 AND ( name % $2 OR category % $2 )",
            &[&user_id.to_string(), &query],
        )
        .await
        .map_err(|err| {
            tracing::error!(
                "{} An error `{:?}` occurred while trying to query ingredients from DB!",
                tracing_prefix,
                err
            );
            let error: ResponseError<_> = (
                StatusCode::INTERNAL_SERVER_ERROR,
                SearchIngredientErrors::ErrorRetrievingIngredients,
            )
                .into();
            error
        })?;
    tracing::debug!("{} Done getting ingredients!", tracing_prefix);

    tracing::debug!("{} Parsing ingredients...", tracing_prefix);
    let ingredients = ingredients
        .iter()
        .map(|row| {
            parse_db_ingredient(row, &tracing_prefix).ok_or_else(|| {
                let error: ResponseError<_> = (
                    StatusCode::INTERNAL_SERVER_ERROR,
                    SearchIngredientErrors::InvalidIngredientFormatFromDB,
                )
                    .into();
                error
            })
        })
        .collect::<Result<Vec<Ingredient>, ResponseError<SearchIngredientErrors>>>()?;
    tracing::debug!("{} DONE", tracing_prefix);

    Ok(Json(ingredients))
}
