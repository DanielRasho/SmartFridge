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
pub enum EditIngredientErrors {
    InvalidPayload { payload: String },
    InvalidJWT,
    NoDBConnectionFound,
    JWTExpired,
    ErrorCheckingIfSessionIsValid,
    ErrorUpdatingIngredientInDB,
}

impl Display for EditIngredientErrors {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        write!(f, "{:?}", self)
    }
}

#[derive(Debug, Deserialize)]
pub struct EditIngredientPayload {
    token: String,
    ingredient: IngredientPayload,
}

/// Represents an ingredient that will be created.
///
/// Creating the ingredient id shouldn't be a responsibility of the client.
/// That's why this object doesn't have that.
#[derive(Debug, Serialize, Deserialize)]
pub struct IngredientPayload {
    #[serde(rename = "IngredientId")]
    pub ingredient_id: Uuid,

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

/// Route to edit the data contained inside an ingredient.
///
/// All elements from the ingredient are updated except for id's.
pub async fn edit_ingredient(
    payload: Json<serde_json::Value>,
    client: Arc<Option<Client>>,
) -> Result<impl IntoResponse, ResponseError<EditIngredientErrors>> {
    let id = ID.fetch_add(1, std::sync::atomic::Ordering::AcqRel);
    let tracing_prefix = format!("/ingredients/edit - {}:", id);

    tracing::debug!("{} START", tracing_prefix);

    tracing::debug!("{} Parsing payload...", tracing_prefix);
    let EditIngredientPayload { token, ingredient } =
        match serde_json::from_value(payload.0.clone()) {
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
                    EditIngredientErrors::InvalidPayload {
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
                (StatusCode::BAD_REQUEST, EditIngredientErrors::InvalidJWT).into();
            Err(error)?
        }
    };
    tracing::debug!("{} JWT extracted successfully!", tracing_prefix);

    tracing::debug!("{} Checking for DB connection...", tracing_prefix);
    let conn = client.as_ref().as_ref().ok_or_else(|| {
        tracing::error!("{} No DB connection found!", tracing_prefix);
        let error: ResponseError<_> = (
            StatusCode::INTERNAL_SERVER_ERROR,
            EditIngredientErrors::NoDBConnectionFound,
        )
            .into();
        error
    })?;
    tracing::debug!("{} DB Connection found!", tracing_prefix);

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
                EditIngredientErrors::ErrorCheckingIfSessionIsValid,
            ),
            crate::IsSessionValidErrors::InvalidSessionData {
                current_date: _,
                db_expire_date: _,
            } => (StatusCode::UNAUTHORIZED, EditIngredientErrors::JWTExpired),
            _ => (
                StatusCode::BAD_REQUEST,
                EditIngredientErrors::ErrorCheckingIfSessionIsValid,
            ),
        }
        .into();

        Err(error)?
    }
    tracing::debug!("{} Session is valid!", tracing_prefix);

    tracing::debug!("{} Updating ingredient in DB...", tracing_prefix);
    let IngredientPayload {
        ingredient_id,
        expire_date,
        name,
        category,
        quantity,
        unit,
    } = &ingredient;
    if let Err(err) = conn.execute("UPDATE sf_ingredient SET expire_date=$2, name=$3, category=$4, quantity=$5, unit=$6 WHERE ingredient_id=$1", &[&ingredient_id.to_string(), expire_date, name, category, &(*quantity as i16), unit]).await {
        tracing::error!("{} An error `{:?}` occurred while trying to update the ingredient `{:?}`", tracing_prefix, err, ingredient);
        let error: ResponseError<_> = (StatusCode::INTERNAL_SERVER_ERROR, EditIngredientErrors::ErrorUpdatingIngredientInDB).into();
        Err(error)?
    }
    tracing::debug!("{} Ingredient updated!", tracing_prefix);

    tracing::debug!("{} DONE", tracing_prefix);
    Ok(StatusCode::OK)
}
