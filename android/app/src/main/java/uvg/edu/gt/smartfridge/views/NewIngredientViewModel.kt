package uvg.edu.gt.smartfridge.views

import androidx.lifecycle.ViewModel
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import uvg.edu.gt.smartfridge.data.FridgeService
import uvg.edu.gt.smartfridge.models.Ingredient

class NewIngredientViewModel : ViewModel() {

    private val _httpClient = HttpClient(CIO) {
        install (Logging) {
            logger = Logger.DEFAULT
            filter { request ->  request.url.host.contains("ktor.io")  }
        }
    }

    private val fridgeService = FridgeService(_httpClient)

    suspend fun addIngredient( JWT_TOKEN : String, ingredient: Ingredient) : Result<String> {
        return fridgeService.addIngredient(JWT_TOKEN, ingredient)
    }

}