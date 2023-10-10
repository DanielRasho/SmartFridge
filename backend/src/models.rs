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
    #[serde(rename = "Theme")]
    theme: AppThemes,
}

/// Represents an ingredient that the user needs.
#[derive(Debug, Serialize, Deserialize)]
pub struct Ingredient {
    #[serde(rename = "ExpireDate")]
    pub expire_date: DateTime<Utc>,

    #[serde(rename = "Name")]
    pub name: String,

    #[serde(rename = "Category")]
    pub category: String,

    #[serde(rename = "Quantity")]
    pub quantity: u32,

    #[serde(rename = "Unit")]
    pub unit: String,
}

/// Represents a Food Recipe in the app.
#[derive(Debug, Serialize, Deserialize)]
pub struct Recipe {
    #[serde(rename = "Recipe")]
    pub title: String,

    #[serde(rename = "Banner")]
    pub banner: String,

    #[serde(rename = "Tags")]
    pub tags: Vec<String>,

    #[serde(rename = "Ingredients")]
    pub ingredients: Vec<Ingredient>,

    #[serde(rename = "Source")]
    pub source: String,
}

#[derive(Debug, Serialize, Deserialize)]
pub struct JWT_Token {
    pub user_id: String,
    pub expired_date: DateTime<Utc>,
    pub username: String,
}
