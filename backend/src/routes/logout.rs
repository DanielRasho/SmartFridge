use std::{
    fmt::Display,
    sync::{atomic::AtomicUsize, Arc},
};

use axum::{response::IntoResponse, Json};
use chrono::{Duration, Utc};
use hyper::StatusCode;
use serde::Deserialize;
use tokio_postgres::Client;

use crate::{extract_jwt, models::JWT_Token, responses::ResponseError};

#[derive(Debug)]
pub enum LogoutUserErrors {
    InvalidPayload,
    NoDBConnection,
    ErrorUpdatingSessionDate,
}

impl Display for LogoutUserErrors {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        write!(f, "{:?}", self)
    }
}

#[derive(Debug, Deserialize)]
struct LogoutUserPayload {
    token: String,
}

static ID: AtomicUsize = AtomicUsize::new(0);

pub async fn logout(
    payload: Json<serde_json::Value>,
    client: Arc<Option<Client>>,
) -> Result<impl IntoResponse, ResponseError<LogoutUserErrors>> {
    let id = ID.fetch_add(1, std::sync::atomic::Ordering::AcqRel);
    let tracing_prefix = format!("/user/logout - {}:", id);

    tracing::debug!("{} START", tracing_prefix);

    tracing::debug!("{} Parsing payload...", tracing_prefix);
    let LogoutUserPayload { token } = match serde_json::from_value(payload.0.clone()) {
        Ok(p) => p,
        Err(err) => {
            tracing::error!(
                "{} An error `{:?}` occurred while parsing payload {}",
                tracing_prefix,
                err,
                payload.0
            );
            let error: ResponseError<_> =
                (StatusCode::BAD_REQUEST, LogoutUserErrors::InvalidPayload).into();
            Err(error)?
        }
    };
    tracing::debug!("{} Payload parsed successfully!", tracing_prefix);

    tracing::debug!("{} Extracting JWT...", tracing_prefix);
    let JWT_Token {
        user_id: _,
        session_id,
        expire_date: _,
        username: _,
    } = match extract_jwt(crate::APP_SECRET, &token) {
        Ok(r) => r,
        Err(err) => {
            tracing::error!(
                "{} An error `{:?}` occurred while trying to extract the JWT!",
                tracing_prefix,
                err
            );
            let error: ResponseError<_> =
                (StatusCode::BAD_REQUEST, LogoutUserErrors::InvalidPayload).into();
            Err(error)?
        }
    };
    tracing::debug!("{} JWT extracted!", tracing_prefix);

    if client.as_ref().is_none() {
        tracing::error!("{} No DB connection found!", tracing_prefix);
        let err: ResponseError<_> = (
            StatusCode::INTERNAL_SERVER_ERROR,
            LogoutUserErrors::NoDBConnection,
        )
            .into();
        Err(err)?
    }

    tracing::debug!(
        "{} DB connection found! Updating expire date...",
        tracing_prefix
    );
    let conn = client.as_ref().as_ref().unwrap();
    let new_expire_date = Utc::now() - Duration::seconds(3);

    if let Err(err) = conn
        .execute(
            "UPDATE sf_session SET expire_date=$1 WHERE session_id=$2",
            &[&new_expire_date, &session_id],
        )
        .await
    {
        tracing::error!(
            "{} An error `{:?}` occurred while updating session with id `{}`",
            tracing_prefix,
            err,
            session_id
        );
        let error: ResponseError<_> = (
            StatusCode::INTERNAL_SERVER_ERROR,
            LogoutUserErrors::ErrorUpdatingSessionDate,
        )
            .into();
        Err(error)?
    }
    tracing::debug!("{} Session updated successfully!", tracing_prefix);

    tracing::debug!("{} DONE", tracing_prefix);
    Ok(StatusCode::OK)
}
