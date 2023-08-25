package com.uvg.gt.smartfridgeandroid.placeholder

import com.uvg.gt.smartfridgeandroid.FoodQuantity
import com.uvg.gt.smartfridgeandroid.Ingredient
import com.uvg.gt.smartfridgeandroid.IngredientHolder
import com.uvg.gt.smartfridgeandroid.Recipe
import java.time.LocalDate
import kotlin.random.Random

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 *
 * TODO: Replace all uses of this class before publishing your app.
 */
object PlaceholderContent {

    /**
     * An array of sample (placeholder) items.
     */
    val RECIPES: MutableList<Recipe> = ArrayList()
    val INGREDIENTS_HOLDERS: MutableList<IngredientHolder> = ArrayList()


    private const val COUNT = 25

    init {
        // Add some sample items.
        for (i in 1..COUNT) {
            RECIPES.add(createPlaceholderItem(i))
        }

        for (i in 1..(COUNT / 5)) {
            INGREDIENTS_HOLDERS.add(createPlaceholderHolder())
        }
    }

    private fun createPlaceholderHolder(): IngredientHolder {
        val values = listOf(
            IngredientHolder(
                "Seasonings",
                mutableListOf(
                    Ingredient(
                        LocalDate.now(),
                        "Peanut Butter",
                        "Seasonings",
                        FoodQuantity("Bottles", 2f)
                    ),
                    Ingredient(
                        LocalDate.now(),
                        "Water",
                        "Seasonings",
                        FoodQuantity("Cups", 1f)
                    )
                )
            ),
            IngredientHolder(
                "Meat",
                mutableListOf(
                    Ingredient(
                        LocalDate.now(),
                        "Chicken",
                        "Meat",
                        FoodQuantity("Lbs", 5f)
                    ),
                    Ingredient(
                        LocalDate.now(),
                        "Shrimps",
                        "Meat",
                        FoodQuantity("Lbs", 2f)
                    )
                )
            )
        )
        return values[Random.nextInt(0, values.size)]
    }

    private fun createPlaceholderItem(position: Int): Recipe {
        return Recipe(
            position.toString(),
            "Recipe #$position", "@tools:sample/backgrounds/scenic[0]", listOf("Lunch", "Breakfast")
        )
    }
}