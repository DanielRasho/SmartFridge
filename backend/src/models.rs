use chrono::{DateTime, Utc};
use serde::{Deserialize, Serialize};
use std::str::FromStr;
use strum::EnumString;
use uuid::Uuid;

/// Represents the theme of the app the user selected.
#[derive(Debug, Serialize, Deserialize, Default, EnumString)]
pub enum AppThemes {
    #[default]
    Light,
    Dark,
    Foxy,
    DarkOcean,
}

/// Represents the settings the user has for the client app.
#[derive(Debug, Serialize, Deserialize)]
pub struct UserSettings {
    #[serde(rename = "SettingsId")]
    settings_id: Uuid,

    #[serde(rename = "UserId")]
    user_id: Uuid,

    #[serde(rename = "Theme")]
    theme: AppThemes,
}

#[derive(Debug)]
pub enum FromTokioRowToUserSettingsErrors {
    FailedParsingSettingsId,
    FailedParsingUserId,
    FailedParsingTheme,
}

impl TryFrom<&tokio_postgres::Row> for UserSettings {
    type Error = FromTokioRowToUserSettingsErrors;

    fn try_from(value: &tokio_postgres::Row) -> Result<Self, Self::Error> {
        let settings_id: String = value.get("settings_id");
        let user_id: String = value.get("user_id");
        let theme: String = value.get("theme");

        let settings_id = settings_id
            .parse()
            .map_err(|_| FromTokioRowToUserSettingsErrors::FailedParsingSettingsId)?;
        let user_id = user_id
            .parse()
            .map_err(|_| FromTokioRowToUserSettingsErrors::FailedParsingUserId)?;
        let theme = AppThemes::from_str(&theme)
            .map_err(|_| FromTokioRowToUserSettingsErrors::FailedParsingTheme)?;

        Ok(UserSettings {
            settings_id,
            user_id,
            theme,
        })
    }
}

/// Represents an ingredient that the user needs.
#[derive(Debug, Serialize, Deserialize)]
pub struct Ingredient {
    #[serde(rename = "IngredientId")]
    pub ingredient_id: Uuid,

    #[serde(rename = "UserId")]
    pub user_id: Uuid,

    #[serde(rename = "ExpireDate")]
    pub expire_date: DateTime<Utc>,

    #[serde(rename = "Name")]
    pub name: String,

    #[serde(rename = "Category")]
    pub category: String,

    #[serde(rename = "Quantity")]
    pub quantity: f32,

    #[serde(rename = "Unit")]
    pub unit: String,
}

/// Represents a Food Recipe in the app.
#[derive(Debug, Serialize, Deserialize)]
pub struct Recipe {
    /// The tracking Id of the recipe.
    #[serde(rename = "RecipeId")]
    pub recipe_id: String,

    #[serde(rename = "Recipe")]
    pub title: String,

    #[serde(rename = "Banner")]
    pub banner: String,

    #[serde(rename = "Tags")]
    pub tags: Vec<String>,

    #[serde(rename = "Ingredients")]
    pub ingredients: Vec<RecipeIngredient>,

    #[serde(rename = "Source")]
    pub source: String,
}

#[derive(Debug, Serialize, Deserialize)]
pub struct RecipeIngredient {
    #[serde(rename = "Name")]
    pub name: String,

    #[serde(rename = "Display")]
    pub display: String,
}

#[derive(Debug, Serialize, Deserialize)]
pub struct JWT_Token {
    pub user_id: String,
    pub session_id: String,
    pub expire_date: DateTime<Utc>,
    pub username: String,
}
