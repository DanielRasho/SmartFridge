package uvg.edu.gt.smartfridge.viewModels

import androidx.lifecycle.ViewModel
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import kotlinx.serialization.Serializable
import uvg.edu.gt.smartfridge.data.FridgeService
import uvg.edu.gt.smartfridge.models.Ingredient

class EditIngredientViewModel : ViewModel() {
    private val _httpClient = HttpClient(CIO) {
        install(Logging) {
            logger = Logger.DEFAULT
            filter { request -> request.url.host.contains("ktor.io") }
        }
    }

    private val fridgeService = FridgeService(_httpClient)

    suspend fun editIngredient(JWT_TOKEN: String, ingredient: Ingredient): Result<String> {
        val payload = EditIngredientRequestPayload(
            ingredient.IngredientId,
            ingredient.Name,
            ingredient.Category,
            ingredient.Quantity,
            ingredient.Unit,
            ingredient.ExpireDate
        )
        return fridgeService.editIngredient(JWT_TOKEN, payload)
    }

    suspend fun deleteIngredient(JWT_TOKEN : String, IngredientId: String) : Result<String> {
        return fridgeService.deleteIngredient(JWT_TOKEN, IngredientId)
    }
}

@Serializable
class EditIngredientRequestPayload(
    val IngredientId : String,
    val Name: String,
    val Category: String,
    val Quantity: Float,
    val Unit: String,
    val ExpireDate: String = ""
)