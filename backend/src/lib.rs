#![recursion_limit = "512"]
use std::{
    fmt::{Debug, Display},
    str::FromStr,
};

use base64::{engine::general_purpose, Engine};
use chrono::{DateTime, Datelike, Timelike, Utc};
use hmac::{digest::KeyInit, Hmac};
use hyper::StatusCode;
use jwt::{SignWithKey, VerifyWithKey};
use models::{Ingredient, JWT_Token};
use rand::{thread_rng, Rng};
use responses::ResponseError;
use sha2::{Digest, Sha256};
use tokio_postgres::{Client, Row};
use uuid::Uuid;

mod models;
mod responses;
pub mod routes;

pub const APP_SECRET: &[u8] = b"super-secret-key";

#[derive(Debug)]
pub enum GenerateJWTErrors {
    ErrorGeneratingHmacKey,
    ErrorSigningWithKey,
}

/// Creates a JWT.
/// This operation may fail as the `token_info` object signing with the key may fail.
pub fn generate_jwt(secret: &[u8], token_info: JWT_Token) -> Result<String, GenerateJWTErrors> {
    let secret_key: Hmac<Sha256> =
        Hmac::new_from_slice(secret).map_err(|_| GenerateJWTErrors::ErrorGeneratingHmacKey)?;
    token_info
        .sign_with_key(&secret_key)
        .map_err(|_| GenerateJWTErrors::ErrorSigningWithKey)
}

#[derive(Debug)]
pub enum ExtractJWTErrors {
    ErrorGeneratingHmacKey,
    ErrorExtractingWithKey,
}

/// Extracts the JWT.
/// This operation my fail as the JWT may be invalid.
pub fn extract_jwt(secret: &[u8], token: &str) -> Result<JWT_Token, ExtractJWTErrors> {
    let secret_key: Hmac<Sha256> =
        Hmac::new_from_slice(secret).map_err(|_| ExtractJWTErrors::ErrorGeneratingHmacKey)?;
    token
        .verify_with_key(&secret_key)
        .map_err(|_| ExtractJWTErrors::ErrorExtractingWithKey)
}

/// Encrypts the given password with a random 16 bytes salt.
fn encrypt_password(password: &str) -> String {
    let mut rand = thread_rng();
    let salt: [u8; 16] = rand.gen();
    encrypt_password_with_salt(password, &salt)
}

/// Encrypts the password with the given salt.
fn encrypt_password_with_salt(password: &str, salt: &[u8]) -> String {
    let salt = salt.iter();
    let password_bytes = password.bytes();

    let mut hasher = Sha256::new();
    hasher.update(password_bytes.collect::<Vec<u8>>());
    let password_bytes = hasher.finalize().to_vec();
    let password_bytes: Vec<u8> = salt.chain(password_bytes.iter()).copied().collect();

    general_purpose::STANDARD_NO_PAD.encode(password_bytes)
}

/// Obtains the salt from the given password.
fn obtain_salt(db_password: &str) -> Result<Vec<u8>, base64::DecodeError> {
    let decoded_bytes = general_purpose::STANDARD_NO_PAD.decode(db_password.as_bytes())?;
    Ok(decoded_bytes[0..16].to_vec())
}

#[derive(Debug)]
enum IsSessionValidErrors {
    InternalDBError(tokio_postgres::Error),
    NoSessionWithId(String),
    UserIdDoesntMatchDBRecord {
        db_user_id: String,
        user_id: String,
    },
    ExpireDateDoesntMatchDBRecord {
        db_expire_date: chrono::DateTime<Utc>,
        expire_date: chrono::DateTime<Utc>,
    },
    InvalidSessionData {
        current_date: DateTime<Utc>,
        db_expire_date: DateTime<Utc>,
    },
}

/// Checks if the given JWT represents a valid session with the given connection.
///
/// The method returns an error if the session is invalid, or nothing if it's ok.
async fn is_session_valid(
    JWT_Token {
        user_id,
        session_id,
        expire_date,
        username: _,
    }: JWT_Token,
    conn: &Client,
) -> Result<(), IsSessionValidErrors> {
    let current_date = Utc::now();
    let rows = conn
        .query(
            "SELECT user_id, expire_date FROM sf_session WHERE session_id=$1",
            &[&session_id],
        )
        .await
        .map_err(|err| IsSessionValidErrors::InternalDBError(err))?;
    if rows.is_empty() {
        return Err(IsSessionValidErrors::NoSessionWithId(session_id));
    }

    let row = rows.get(0).unwrap();
    let db_user_id = row.get::<usize, String>(0);
    let db_expire_date = row.get::<usize, DateTime<Utc>>(1);

    if db_user_id != user_id {
        return Err(IsSessionValidErrors::UserIdDoesntMatchDBRecord {
            db_user_id,
            user_id,
        });
    }

    if (db_expire_date - expire_date) > chrono::Duration::minutes(1) {
        return Err(IsSessionValidErrors::ExpireDateDoesntMatchDBRecord {
            db_expire_date,
            expire_date,
        });
    }

    if current_date > db_expire_date {
        return Err(IsSessionValidErrors::InvalidSessionData {
            current_date,
            db_expire_date,
        });
    }

    Ok(())
}

/// Converts a value in the given index from a DB row into a value of type T.
fn from_db_to_value<T>(row: &Row, index: &str, tracing_prefix: &str) -> Option<T>
where
    T: FromStr,
    <T as FromStr>::Err: Debug,
{
    let value = match row.try_get::<&str, &str>(index) {
        Ok(v) => v,
        Err(err) => {
            tracing::error!(
                "{} An error `{:?}` occurred while parsing row field `{}`",
                tracing_prefix,
                err,
                index
            );
            None?
        }
    };

    match value.parse() {
        Ok(v) => Some(v),
        Err(err) => {
            tracing::error!(
                "{} An error `{:?}` occurred while parsing row field `{}`",
                tracing_prefix,
                err,
                index
            );
            None?
        }
    }
}

/// Parses an Ingredient from a DB Row.
fn parse_db_ingredient(
    row: &Row,
    tracing_prefix: &str,
) -> Option<Ingredient>
{
    let ingredient_id = from_db_to_value(row, "ingredient_id", &tracing_prefix)?;

    let user_id = from_db_to_value(row, "user_id", &tracing_prefix)?;

    let name = from_db_to_value(row, "name", &tracing_prefix)?;

    let expire_date = from_db_to_value(row, "expire_date", &tracing_prefix)?;

    let category = from_db_to_value(row, "category", &tracing_prefix)?;

    let quantity = from_db_to_value(row, "quantity", &tracing_prefix)?;

    let unit = from_db_to_value(row, "unit", &tracing_prefix)?;

    Some(Ingredient {
        ingredient_id,
        user_id,
        expire_date,
        name,
        category,
        quantity,
        unit,
    })
}
