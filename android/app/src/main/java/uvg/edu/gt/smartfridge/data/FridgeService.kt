package uvg.edu.gt.smartfridge.data

import io.ktor.client.HttpClient
import uvg.edu.gt.smartfridge.models.Ingredient

class FridgeService (private val client : HttpClient){

    //const val GET_INGREDIENTS = "/get_ingredients"
    //const val SEARCH_INGREDIENTS = "/search_ingredients"

    // TODO
    suspend fun getIngredients() : List<Ingredient>{
        return emptyList()
    }

    // TODO
    suspend fun searchIngredients() : List<Ingredient>{
        return emptyList()
    }
}