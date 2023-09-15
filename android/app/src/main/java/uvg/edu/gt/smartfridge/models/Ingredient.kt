package uvg.edu.gt.smartfridge.models

data class Ingredient(val name: String, val category: String, val quantity: Float, val unit: String, val expireDate: String? = null)
