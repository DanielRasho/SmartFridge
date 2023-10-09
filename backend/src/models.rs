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
    pub expire_date: DateTime<Utc>,
    pub name: String,
    pub category: String,
    pub quantity: u32,
    pub unit: String,
}

/// Represents a Food Recipe in the app.
#[derive(Debug, Serialize, Deserialize)]
pub struct Recipe {
    pub title: String,
    pub banner: String,
    pub tags: Vec<String>,
    pub ingredients: Vec<Ingredient>,
    pub source: String,
}

#[derive(Debug, Serialize, Deserialize)]
pub struct JWT_Token {
    pub user_id: String,
    pub expired_date: DateTime<Utc>,
    pub username: String,
}
