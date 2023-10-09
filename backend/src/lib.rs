use hmac::{Hmac, digest::KeyInit};
use jwt::SignWithKey;
use models::JWT_Token;
use sha2::Sha256;

mod models;
mod routes;
mod responses;

#[derive(Debug)]
pub enum GenerateJWTErrors {
    ErrorGeneratingHmacKey,
    ErrorSigningWithKey,
}

pub fn generate_jwt(secret: &[u8], token_info: JWT_Token) -> Result<String, GenerateJWTErrors> {
    let secret_key: Hmac<Sha256> = Hmac::new_from_slice(secret).map_err(|_| GenerateJWTErrors::ErrorGeneratingHmacKey)?;
    token_info.sign_with_key(&secret_key).map_err(|_| GenerateJWTErrors::ErrorSigningWithKey)
}
