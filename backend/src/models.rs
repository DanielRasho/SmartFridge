use chrono::{DateTime, Utc};
use serde::{Deserialize, Serialize};

/// Represents the theme of the app the user selected.
#[derive(Debug, Serialize, Deserialize)]
pub enum AppThemes {
    Light,
    Dark,
    Foxy,
    DarkOcean,
}

/// Represents the settings the user has for the client app.
#[derive(Debug, Serialize, Deserialize)]
pub struct UserSettings {
    theme: AppThemes,
}

/// Represents an ingredient that the user needs.
#[derive(Debug, Serialize, Deserialize)]
pub struct Ingredient {
    expire_date: DateTime<Utc>,
    name: String,
    category: String,
    quantity: u32,
    unit: String,
}

/// Represents a Food Recipe in the app.
#[derive(Debug, Serialize, Deserialize)]
pub struct Recipe {
    title: String,
    banner: String,
    tags: Vec<String>,
    ingredients: Vec<Ingredient>,
    source: String,
}

#[derive(Debug, Serialize, Deserialize)]
pub struct JWT_Token {
    pub user_id: String,
    pub expired_date: DateTime<Utc>,
    pub username: String,
}
