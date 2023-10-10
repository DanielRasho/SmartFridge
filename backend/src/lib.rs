use hmac::{digest::KeyInit, Hmac};
use jwt::{SignWithKey, VerifyWithKey};
use models::JWT_Token;
use sha2::Sha256;

mod models;
mod responses;
pub mod routes;

pub const APP_SECRET: &[u8] = b"super-secret-key";

#[derive(Debug)]
pub enum GenerateJWTErrors {
    ErrorGeneratingHmacKey,
    ErrorSigningWithKey,
}

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

pub fn extract_jwt(secret: &[u8], token: &str) -> Result<JWT_Token, ExtractJWTErrors> {
    let secret_key: Hmac<Sha256> =
        Hmac::new_from_slice(secret).map_err(|_| ExtractJWTErrors::ErrorGeneratingHmacKey)?;
    token
        .verify_with_key(&secret_key)
        .map_err(|_| ExtractJWTErrors::ErrorExtractingWithKey)
}
