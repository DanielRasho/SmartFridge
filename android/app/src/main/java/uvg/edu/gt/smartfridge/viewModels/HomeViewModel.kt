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
import uvg.edu.gt.smartfridge.data.HomeService
import uvg.edu.gt.smartfridge.models.Ingredient
import uvg.edu.gt.smartfridge.models.Recipe

class HomeViewModel : ViewModel()  {

    private val _httpClient = HttpClient(CIO) {
        install (Logging) {
            logger = Logger.DEFAULT
            filter { request ->  request.url.host.contains("ktor.io")  }
        }
    }

    private val homeService = HomeService(_httpClient)

    private var recipes by mutableStateOf(emptyList<Recipe>())

    fun getRecipesList() : List<Recipe>{
        return recipes
    }

    suspend fun fetchRecipesList(JWT_TOKEN : String) : Result<List<Recipe>> {
        val result = homeService.getRecipes(JWT_TOKEN)
        if (result.isSuccess)
            recipes = result.getOrNull()!!
        return homeService.getRecipes(JWT_TOKEN)
    }

    suspend fun searchRecipes(JWT_TOKEN: String, query : String) : Result<List<Recipe>> {
        return homeService.searchRecipes(JWT_TOKEN, query.trim())
    }

}