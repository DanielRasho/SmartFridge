package uvg.edu.gt.smartfridge.models

import kotlinx.serialization.Serializable

@Serializable
data class Recipe(
    val title: String,
    val banner: String,
    val tags: Sequence<String>,
    val ingredients: Sequence<Ingredient>,
    val source: String
)
