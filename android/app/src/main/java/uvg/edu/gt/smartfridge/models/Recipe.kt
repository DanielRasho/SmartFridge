package uvg.edu.gt.smartfridge.models

import kotlinx.serialization.Serializable

@Serializable
data class Recipe(
    val RecipeId : String,
    val Recipe: String,
    val Banner: String,
    val Tags: List<String>,
    val Ingredients: List<Ingredient>,
    val Source: String
)
