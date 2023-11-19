package uvg.edu.gt.smartfridge.data

import android.util.Log
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.headers
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.json.JSONArray
import org.json.JSONObject
import uvg.edu.gt.smartfridge.models.Recipe

class HomeService (client: HttpClient) : Service(client) {
    suspend fun getRecipes( JWT_TOKEN : String) : Result<List<Recipe>>{
        return handleHttpRequest {
            println("JWT: $JWT_TOKEN")
            // Creating Request body
            val requestBody = buildJsonObject {
                put("token", JWT_TOKEN)
            }.toString()

            // Making Request
            val response : HttpResponse = client.request(
                HttpRoutes.BASE_URL + HttpRoutes.GET_RECIPES
            ){
                method = HttpMethod.Post
                headers {
                    append(HttpHeaders.ContentType, "application/json")
                }
                setBody( requestBody )
            }

            if (response.status == HttpStatusCode.Unauthorized) {
                throw ResponseException(response.status.value, response.body<String>().toString())
            }

            println(response.body() as String)

            // Translating JSON response
            val data : JSONArray = JSONArray(response.body() as String)

            val tempRecipes = ArrayList<Recipe>()

            // Fetching ingredients.
            for (index in 0 until data.length()){
                tempRecipes.add(Json.decodeFromString(data.getString(index)))
            }
            tempRecipes
        }
    }

    suspend fun searchRecipes( JWT_TOKEN : String, query : String) : Result<List<Recipe>>{
        return handleHttpRequest {
            // Creating Request body
            val requestBody = buildJsonObject {
                put("token", JWT_TOKEN)
                put("query", query)
            }.toString()

            // Making Request
            val response : HttpResponse = client.request(
                HttpRoutes.BASE_URL + HttpRoutes.SEARCH_RECIPES
            ){
                method = HttpMethod.Post
                headers {
                    append(HttpHeaders.ContentType, "application/json")
                }
                setBody( requestBody )
            }

            if (response.status == HttpStatusCode.Unauthorized) {
                throw ResponseException(response.status.value, response.body<String>().toString())
            }

            // Translating JSON response
            val data : JSONArray = JSONArray(response.body() as String)

            val tempRecipes = ArrayList<Recipe>()

            // Fetching ingredients.
            for (index in 0 until data.length()){
                var tempRecipe = data.getJSONArray(index)
                tempRecipes.add(
                    Json.decodeFromString(tempRecipe.toString())
                )
            }
            tempRecipes
        }
    }

    suspend fun getRecipeDetails( JWT_TOKEN: String, recipeID : String) : Result<Recipe> {
        return handleHttpRequest {
            // Creating Request body
            val requestBody = buildJsonObject {
                put("token", JWT_TOKEN)
                put("recipeID", recipeID)
            }.toString()

            // Making Request
            val response : HttpResponse = client.request(
                HttpRoutes.BASE_URL + HttpRoutes.SEARCH_RECIPES
            ){
                method = HttpMethod.Post
                headers {
                    append(HttpHeaders.ContentType, "application/json")
                }
                setBody( requestBody )
            }

            if (response.status.value == HttpStatusCode.Unauthorized.value) {
                Log.e("HomeService", "Error, invalid JWT: $response")
                throw ResponseException(response.status.value, response.body<String>().toString())
            }

            // Translating JSON response
            val data : JSONObject = JSONObject(response.body() as String)

            // Returning recipe.
            Json.decodeFromString<Recipe>(data.toString())
        }
    }
}