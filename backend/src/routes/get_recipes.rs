use std::{
    fmt::Display,
    sync::{atomic::AtomicUsize, Arc},
};

use axum::{response::IntoResponse, Json};

use hyper::StatusCode;
use serde::Deserialize;
use serde_json::{Map, Value};
use tokio_postgres::Client;

use crate::{
    extract_jwt, is_session_valid,
    models::{Recipe, RecipeIngredient},
    parse_api_recipe_from_value,
    responses::ResponseError,
    Params, APP_SECRET,
};

#[derive(Debug)]
pub enum GetRecipesErrors {
    InvalidPayloadFormat { payload: String },
    InvalidJWT,
    CouldntRetrieveRecipesFromAPI,
    NoDBConnectionFound,
    ErrorCheckingIfSessionIsValid,
    JWTExpired,
}

impl Display for GetRecipesErrors {
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
    params: Arc<Params>,
) -> Result<impl IntoResponse, ResponseError<GetRecipesErrors>> {
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
                GetRecipesErrors::InvalidPayloadFormat {
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
        Err(err) => {
            tracing::error!(
                "{} An error `{:?}` occurred while extracting the JWT",
                tracing_prefix,
                err,
            );
            let error: ResponseError<_> =
                (StatusCode::BAD_REQUEST, GetRecipesErrors::InvalidJWT).into();
            Err(error)?
        }
    };
    tracing::debug!(
        "{} JWT parsed successfully! `{:?}`",
        tracing_prefix,
        token_info
    );

    tracing::debug!("{} Checking if DB connection exists...", tracing_prefix);
    let conn = client.as_ref().as_ref().ok_or_else(|| {
        tracing::error!("{} No DB connection found!", tracing_prefix);
        let error: ResponseError<_> = (
            StatusCode::INTERNAL_SERVER_ERROR,
            GetRecipesErrors::NoDBConnectionFound,
        )
            .into();
        error
    })?;
    tracing::debug!("{} DB Connection found!", tracing_prefix);

    tracing::debug!("{} Checking if session is valid...", tracing_prefix);
    if let Err(err) = is_session_valid(token_info, conn).await {
        tracing::error!(
            "{} An error `{:?}` occurred while checkinf if session is valid!",
            tracing_prefix,
            err
        );
        let error: ResponseError<_> = match err {
            crate::IsSessionValidErrors::InternalDBError(_) => (
                StatusCode::INTERNAL_SERVER_ERROR,
                GetRecipesErrors::ErrorCheckingIfSessionIsValid,
            ),
            crate::IsSessionValidErrors::InvalidSessionData {
                current_date: _,
                db_expire_date: _,
            } => (StatusCode::UNAUTHORIZED, GetRecipesErrors::JWTExpired),
            _ => (
                StatusCode::BAD_REQUEST,
                GetRecipesErrors::ErrorCheckingIfSessionIsValid,
            ),
        }
        .into();
        Err(error)?
    }
    tracing::debug!("{} Session is valid!", tracing_prefix);

    tracing::debug!("{} Getting recipes from API...", tracing_prefix);
    let recipes = get_recipes_from_api(&params.rapid_api_key, &params.rapid_api_host)
        .await
        .map_err(|err| {
            tracing::error!(
                "{} An error `{:?}` occurred while getting recipes from API!",
                tracing_prefix,
                err
            );
            let error: ResponseError<_> = (
                StatusCode::INTERNAL_SERVER_ERROR,
                GetRecipesErrors::CouldntRetrieveRecipesFromAPI,
            )
                .into();
            error
        })?;

    tracing::debug!("{} DONE", tracing_prefix);
    Ok(Json(recipes))
}

#[derive(Debug)]
enum GetRecipesFromAPIErrors {
    APIFormatHaschanged {
        reason: String,
        api_response: String,
    },
    APIError {
        error: reqwest::Error,
    },
    ResponseWasntJSON {
        error: serde_json::Error,
        response: String,
    },
}

async fn get_recipes_from_api(
    api_key: &str,
    api_host: &str,
) -> Result<Vec<Recipe>, GetRecipesFromAPIErrors> {
    let client = reqwest::Client::new();
    let response = client
        .get("https://worldwide-recipes1.p.rapidapi.com/api/explore")
        .header("X-RapidAPI-Key", api_key)
        .header("X-RapidAPI-Host", api_host)
        .send()
        .await
        .map_err(|err| GetRecipesFromAPIErrors::APIError { error: err })?
        .text()
        .await
        .map_err(|err| GetRecipesFromAPIErrors::APIError { error: err })?;

    let api_response: Value = serde_json::from_str(&response).map_err(|err| {
        GetRecipesFromAPIErrors::ResponseWasntJSON {
            error: err,
            response,
        }
    })?;
    let json_recipes = match api_response {
        serde_json::Value::Object(obj) => obj,
        _ => Err(GetRecipesFromAPIErrors::APIFormatHaschanged {
            reason: "Response was not an object!".to_owned(),
            api_response: "".to_owned(),
        })?,
    };
    let json_recipes = match json_recipes.get("results") {
        Some(serde_json::Value::Object(obj)) => obj,
        _ => Err(GetRecipesFromAPIErrors::APIFormatHaschanged {
            reason: "results object not found!".to_owned(),
            api_response: "".to_owned(),
        })?,
    };
    let json_recipes = match json_recipes.get("feed") {
        Some(serde_json::Value::Array(arr)) => arr,
        _ => Err(GetRecipesFromAPIErrors::APIFormatHaschanged {
            reason: "feed array not found!".to_owned(),
            api_response: "".to_owned(),
        })?,
    };
    let recipes: Vec<Recipe> = json_recipes
        .iter()
        .filter_map(|v| {
            if let Value::Object(a) = v {
                parse_api_recipe_from_value(a)
            } else {
                None
            }
        })
        .collect();

    Ok(recipes)
}
