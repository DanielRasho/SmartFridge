package com.uvg.gt.smartfridgeandroid

import java.util.Date

data class FoodQuantity(val qualifier: String, val quantity: Float)
data class Ingredient(val date: Date, val name: String, val category: String, val quantity: FoodQuantity)
data class IngredientHolder(val category: String, val values: MutableList<Ingredient>)