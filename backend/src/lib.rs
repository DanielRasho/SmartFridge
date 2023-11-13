#![recursion_limit = "512"]
use std::{fmt::Debug, io, net::SocketAddr, str::FromStr};

use base64::{engine::general_purpose, Engine};
use chrono::{DateTime, Utc};
use clap::Parser;
use hmac::{digest::KeyInit, Hmac};

use jwt::{SignWithKey, VerifyWithKey};
use models::{Ingredient, JWT_Token, Recipe, RecipeIngredient};
use rand::{thread_rng, Rng};

use serde_json::{Map, Value};
use sha2::{Digest, Sha256};
use tokio_postgres::{Client, Row};

mod models;
mod responses;
pub mod routes;

pub const APP_SECRET: &[u8] = b"super-secret-key";

#[derive(Debug, Parser)]
#[command(author, version, about, long_about = None)]
pub struct Params {
    /// The host to start the server on. Check the port is free before starting the server.
    #[arg(short, long, env, default_value = "127.0.0.1:3000", value_parser = resolve_host)]
    pub server_host: SocketAddr,

    /// The connection string to connect to the database.
    #[arg(
        short,
        long,
        env,
        default_value = "host=localhost port=5432 user=postgres dbname=smart_fridge connect_timeout=10"
    )]
    pub db_connection: String,

    /// The WorldWide Recipes API Key, can be obtained from:
    /// https://rapidapi.com/ptwebsolution/api/worldwide-recipes1.
    #[arg(
        long,
        env,
        default_value = "f365167e3emsh42cd1be186db2d8p1b9c87jsn49c9b494e5ac"
    )]
    pub rapid_api_key: String,

    /// The WorldWide Recipes API Host, can be obtained from:
    /// https://rapidapi.com/ptwebsolution/api/worldwide-recipes1.
    #[arg(long, env, default_value = "worldwide-recipes1.p.rapidapi.com")]
    pub rapid_api_host: String,
}

fn resolve_host(host: &str) -> io::Result<SocketAddr> {
    let host: SocketAddr = host.parse().map_err(|_| {
        io::Error::new(
            io::ErrorKind::AddrNotAvailable,
            format!("Couldn't find destination {host}"),
        )
    })?;
    Ok(host)
}

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
        .map_err(IsSessionValidErrors::InternalDBError)?;
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
fn parse_db_ingredient(row: &Row, tracing_prefix: &str) -> Option<Ingredient> {
    let ingredient_id = from_db_to_value(row, "ingredient_id", tracing_prefix)?;

    let user_id = from_db_to_value(row, "user_id", tracing_prefix)?;

    let name = from_db_to_value(row, "name", tracing_prefix)?;

    let expire_date = from_db_to_value(row, "expire_date", tracing_prefix)?;

    let category = from_db_to_value(row, "category", tracing_prefix)?;

    let quantity = from_db_to_value(row, "quantity", tracing_prefix)?;

    let unit = from_db_to_value(row, "unit", tracing_prefix)?;

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

/// Parse a recipe from the response of WorldWide Recipes of RapidAPI
fn parse_api_recipe_from_value(value: &Map<String, Value>) -> Option<Recipe> {
    if let Some(serde_json::Value::Object(_)) = value.get("seo") {
        let recipe_id = if let serde_json::Value::String(a) = value.get("tracking-id")? {
            a.to_string()
        } else {
            None?
        };

        let content = if let serde_json::Value::Object(a) = value.get("content")? {
            a
        } else {
            None?
        };

        let details = if let Value::Object(a) = content.get("details")? {
            a
        } else {
            None?
        };

        let tags: Vec<String> = if let Value::Array(a) = details.get("keywords")? {
            a
        } else {
            None?
        }
        .iter()
        .filter_map(|v| {
            if let Value::String(b) = v {
                Some(b.to_string())
            } else {
                None
            }
        })
        .take(2)
        .collect();

        let title = if let Value::String(a) = details.get("name")? {
            a.to_string()
        } else {
            None?
        };

        let images = if let Value::Array(a) = details.get("images")? {
            a
        } else {
            None?
        };

        let banner = if let Value::Object(a) = images.get(0)? {
            a
        } else {
            None?
        };
        let banner = if let Value::String(a) = banner.get("hostedLargeUrl")? {
            a.to_string()
        } else {
            None?
        };

        let source = if let Value::String(a) = details.get("directionsUrl")? {
            a.to_string()
        } else {
            None?
        };

        let ingredients: Vec<RecipeIngredient> =
            if let Value::Array(a) = content.get("ingredientLines")? {
                a
            } else {
                None?
            }
            .iter()
            .filter_map(|json_ingredient| {
                let display = if let Value::String(a) = json_ingredient.get("wholeLine")? {
                    a.to_string()
                } else {
                    None?
                };

                let name = if let Value::String(a) = json_ingredient.get("ingredient")? {
                    a.to_string()
                } else {
                    None?
                };

                Some(RecipeIngredient { name, display })
            })
            .collect();

        Some(Recipe {
            recipe_id,
            title,
            banner,
            tags,
            ingredients,
            source,
        })
    } else {
        let content = if let Value::Object(a) = value.get("content")? {
            a
        } else {
            None?
        };
        let matches = if let Value::Object(a) = content.get("matches")? {
            a
        } else {
            None?
        };

        parse_api_recipe_from_value(matches)
    }
}
