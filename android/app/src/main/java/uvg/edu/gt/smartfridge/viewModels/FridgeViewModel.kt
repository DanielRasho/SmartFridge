package uvg.edu.gt.smartfridge.viewModels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import uvg.edu.gt.smartfridge.data.FridgeService
import uvg.edu.gt.smartfridge.models.Ingredient

class FridgeViewModel : ViewModel() {

    private val _httpClient = HttpClient(CIO) {
        install (Logging) {
            logger = Logger.DEFAULT
            filter { request ->  request.url.host.contains("ktor.io")  }
        }
    }

    private val fridgeService = FridgeService(_httpClient)

    private var userIngredients by mutableStateOf(emptyList<Ingredient>())

    fun getIngredients() : List<Ingredient> {
        println("ingredients = $userIngredients")
        return userIngredients
    }

    suspend fun fetchUserIngredients( JWT_TOKEN: String) : Result<List<Ingredient>> {
        val result = fridgeService.getIngredients(JWT_TOKEN)
        if (result.isSuccess) {
            println("Ingredients fetched")
            userIngredients = result.getOrNull()!!
            println("Hello ingredients = $userIngredients")
        }
        return result
    }
}