use std::{
    fmt::Display,
    sync::{atomic::AtomicUsize, Arc},
};

use axum::{response::IntoResponse, Json};
use hyper::StatusCode;
use serde::{Deserialize, Serialize};
use tokio_postgres::Client;
use uuid::Uuid;

use crate::{
    extract_jwt, is_session_valid, responses::ResponseError, APP_SECRET,
};

#[derive(Debug, Serialize)]
pub enum RemoveIngredientErrors {
    InvalidPayload { payload: String },
    InvalidJWT,
    NoDBConnectionFound,
    ErrorCheckingIfSessionIsValid,
    JWTExpired,
    ErrorRemovingIngredient,
}

impl Display for RemoveIngredientErrors {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        write!(f, "{:?}", self)
    }
}

#[derive(Debug, Deserialize)]
pub struct RemoveIngredientPayload {
    token: String,
    ingredient_id: Uuid,
}

static ID: AtomicUsize = AtomicUsize::new(0);

pub async fn remove_ingredient(
    payload: Json<serde_json::Value>,
    client: Arc<Option<Client>>,
) -> Result<impl IntoResponse, ResponseError<RemoveIngredientErrors>> {
    let id = ID.fetch_add(1, std::sync::atomic::Ordering::AcqRel);
    let tracing_prefix = format!("/ingredients/remove - {}:", id);

    tracing::debug!("{} START", tracing_prefix);

    tracing::debug!("{} Parsing payload...", tracing_prefix);
    let RemoveIngredientPayload {
        token,
        ingredient_id,
    } = match serde_json::from_value(payload.0.clone()) {
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
                RemoveIngredientErrors::InvalidPayload {
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
                (StatusCode::BAD_REQUEST, RemoveIngredientErrors::InvalidJWT).into();
            Err(error)?
        }
    };
    tracing::debug!("{} JWT extracted successfully!", tracing_prefix);

    let conn = client.as_ref().as_ref().ok_or_else(|| {
        tracing::error!("{} DB connection not found!", tracing_prefix);

        let error: ResponseError<_> = (
            StatusCode::INTERNAL_SERVER_ERROR,
            RemoveIngredientErrors::NoDBConnectionFound,
        )
            .into();
        error
    })?;
    tracing::debug!("{} DB Connection found!", tracing_prefix);

    tracing::debug!("{} Checking if connection is valid...", tracing_prefix);
    if let Err(err) = is_session_valid(token_info, conn).await {
        tracing::error!(
            "{} An error `{:?}` occurred while checking if session is valid!",
            tracing_prefix,
            err
        );
        let error: ResponseError<_> = match err {
            crate::IsSessionValidErrors::InternalDBError(_) => (
                StatusCode::INTERNAL_SERVER_ERROR,
                RemoveIngredientErrors::ErrorCheckingIfSessionIsValid,
            ),
            crate::IsSessionValidErrors::InvalidSessionData {
                current_date: _,
                db_expire_date: _,
            } => (StatusCode::UNAUTHORIZED, RemoveIngredientErrors::JWTExpired),
            _ => (
                StatusCode::BAD_REQUEST,
                RemoveIngredientErrors::ErrorCheckingIfSessionIsValid,
            ),
        }
        .into();

        Err(error)?
    }
    tracing::debug!("{} Session is valid!", tracing_prefix);

    tracing::debug!("{} Removing ingredient...", tracing_prefix);
    if let Err(err) = conn
        .execute(
            "REMOVE FROM sf_ingredient WHERE ingredient_id=$1",
            &[&ingredient_id.to_string()],
        )
        .await
    {
        tracing::error!(
            "{} An error `{:?}` occurred while deleting ingredient from DB!",
            tracing_prefix,
            err
        );
        let error: ResponseError<_> = (
            StatusCode::INTERNAL_SERVER_ERROR,
            RemoveIngredientErrors::ErrorRemovingIngredient,
        )
            .into();
        Err(error)?
    }
    tracing::debug!(
        "{} Ingredient with id `{}` removed",
        tracing_prefix,
        ingredient_id
    );

    tracing::debug!("{} DONE", tracing_prefix);
    Ok(StatusCode::OK)
}
