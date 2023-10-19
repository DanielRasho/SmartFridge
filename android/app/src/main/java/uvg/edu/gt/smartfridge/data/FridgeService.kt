package uvg.edu.gt.smartfridge.data

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.request
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpMethod
import kotlinx.serialization.json.Json
import org.json.JSONArray
import uvg.edu.gt.smartfridge.models.Ingredient

class FridgeService(client: HttpClient) : Service(client) {

    //const val GET_INGREDIENTS = "/get_ingredients"
    //const val SEARCH_INGREDIENTS = "/search_ingredients"

    // TODO
    suspend fun getIngredients(JWT_TOKEN : String) : Result<List<Ingredient>>{

        return handleHttpRequest {
            // Making Request
            val response : HttpResponse = client.request(HttpRoutes.GET_INGREDIENTS){
                method = HttpMethod.Get }

            // Translating JSON response
            val data : JSONArray = JSONArray(response.body() as String)

            val tempIngredients = ArrayList<Ingredient>()
            // Checking response state
            for (index in 0 until data.length()){
                tempIngredients.add(Json.decodeFromString<Ingredient>(data.getString(index)))
            }
            tempIngredients
        }
    }

    // TODO
    suspend fun searchIngredients() : List<Ingredient>{
        return emptyList()
    }
}