package uvg.edu.gt.smartfridge.models

import kotlinx.serialization.Serializable

@Serializable
data class Ingredient(
    val IngredientId: String,
    val Name: String,
    val Category: String,
    val Quantity: Float,
    val Unit: String,
    val ExpireDate: String = ""
)
