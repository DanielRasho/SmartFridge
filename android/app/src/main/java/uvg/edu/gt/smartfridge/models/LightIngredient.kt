package uvg.edu.gt.smartfridge.models

import kotlinx.serialization.Serializable

@Serializable
data class LightIngredient(
    val Name: String,
    val Display : String
)
