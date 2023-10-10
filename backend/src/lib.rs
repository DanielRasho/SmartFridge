use base64::{engine::general_purpose, Engine};
use hmac::{digest::KeyInit, Hmac};
use jwt::{SignWithKey, VerifyWithKey};
use models::JWT_Token;
use rand::{thread_rng, Rng};
use sha2::{Digest, Sha256};

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
fn encrypt_password(password: String) -> String {
    let mut rand = thread_rng();
    let salt: [u8; 16] = rand.gen();
    encrypt_password_with_salt(password, &salt)
}

/// Encrypts the password with the given salt.
fn encrypt_password_with_salt(password: String, salt: &[u8]) -> String {
    let salt = salt.iter();
    let password_bytes = password.into_bytes();

    let mut hasher = Sha256::new();
    hasher.update(password_bytes);
    let password_bytes = hasher.finalize().to_vec();
    let password_bytes: Vec<u8> = salt.chain(password_bytes.iter()).copied().collect();

    general_purpose::STANDARD_NO_PAD.encode(password_bytes)
}
