package uvg.edu.gt.smartfridge.models

import kotlinx.serialization.Serializable

@Serializable
data class Ingredient(
    val name: String,
    val category: String,
    val quantity: Float,
    val unit: String,
    val expireDate: String = ""
)
