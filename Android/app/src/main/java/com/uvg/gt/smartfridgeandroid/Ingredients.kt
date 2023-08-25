package com.uvg.gt.smartfridgeandroid

import java.time.LocalDate

data class FoodQuantity(val qualifier: String, val quantity: Float)
data class Ingredient(val date: LocalDate, val name: String, val category: String, val quantity: FoodQuantity)
data class IngredientHolder(val category: String, val values: MutableList<Ingredient>)