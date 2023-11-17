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
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.put
import org.json.JSONArray
import org.json.JSONObject
import uvg.edu.gt.smartfridge.models.Ingredient
import uvg.edu.gt.smartfridge.viewModels.AddIngredientRequestPayload

class FridgeService(client: HttpClient) : Service(client) {

    // TODO
    suspend fun getIngredients(JWT_TOKEN: String): Result<List<Ingredient>> {

        return handleHttpRequest {

            println("JWT: $JWT_TOKEN")
            // Creating Request body
            val requestBody = buildJsonObject {
                put("token", JWT_TOKEN)
            }.toString()

            // Making Request
            val response: HttpResponse = client.request(
                HttpRoutes.BASE_URL + HttpRoutes.GET_INGREDIENTS
            ) {
                method = HttpMethod.Post
                headers {
                    append(HttpHeaders.ContentType, "application/json")
                }
                setBody(requestBody)
            }

            println(response.body() as String)

            // Translating JSON response
            val data: JSONArray = JSONArray(response.body() as String)

            val tempIngredients = ArrayList<Ingredient>()

            // Fetching ingredients.
            for (index in 0 until data.length()) {
                tempIngredients.add(Json{ignoreUnknownKeys = true}.decodeFromString<Ingredient>(data.getString(index)))
            }
            tempIngredients
        }
    }

    // TODO
    suspend fun searchIngredients(JWT_TOKEN: String, query: String): Result<List<Ingredient>> {
        return handleHttpRequest {

            // Creating Request body
            val requestBody = buildJsonObject {
                put("token", JWT_TOKEN)
                put("query", query)
            }.toString()

            // Making Request
            val response: HttpResponse = client.request(
                HttpRoutes.BASE_URL + HttpRoutes.SEARCH_INGREDIENTS
            ) {
                method = HttpMethod.Post
                headers {
                    append(HttpHeaders.ContentType, "application/json")
                }
                setBody(requestBody)
            }

            // Translating JSON response
            val data: JSONArray = JSONArray(response.body() as String)

            val tempIngredients = ArrayList<Ingredient>()

            // Fetching ingredients.
            for (index in 0 until data.length()) {
                tempIngredients.add(Json.decodeFromString<Ingredient>(data.getString(index)))
            }
            tempIngredients
        }
    }

    suspend fun addIngredient(JWT_TOKEN: String, ingredient: AddIngredientRequestPayload): Result<String> {

        return handleHttpRequest {
            val reqIngredient = Json.encodeToJsonElement(ingredient)

            val requestBody = buildJsonObject {
                put("token", JWT_TOKEN)
                put("ingredient", reqIngredient)
            }.toString()

            println(requestBody)

            // Making Request
            val response: HttpResponse = client.request(
                HttpRoutes.BASE_URL + HttpRoutes.ADD_INGREDIENT
            ) {
                method = HttpMethod.Post
                headers {
                    append(HttpHeaders.ContentType, "application/json")
                }
                setBody(requestBody)
            }

            val body = response.body() as String
            println("The response body is: " + body.toString())
            println(HttpRoutes.BASE_URL + HttpRoutes.ADD_INGREDIENT + ": Sending data: " + requestBody)

            "Ingredient ${ingredient.Name} added!"
        }
    }


    suspend fun editIngredient(JWT_TOKEN: String, ingredient: Ingredient): Result<String> {

        return handleHttpRequest {
            // Creating Request body
            val requestBody = buildJsonObject {
                put("token", JWT_TOKEN)
                put("ingredient", Json.encodeToString(ingredient))
            }.toString()

            // Making Request
            val response: HttpResponse = client.request(
                HttpRoutes.BASE_URL + HttpRoutes.EDIT_INGREDIENT
            ) {
                method = HttpMethod.Post
                headers {
                    append(HttpHeaders.ContentType, "application/json")
                }
                setBody(requestBody)
            }

            "Ingredient edited"
        }
    }

    suspend fun deleteIngredient(JWT_TOKEN: String, ingredientID: String): Result<String> {

        return handleHttpRequest {
            // Creating Request body
            val requestBody = buildJsonObject {
                put("token", JWT_TOKEN)
                put("ingredient_id", ingredientID)
            }.toString()

            // Making Request
            val response: HttpResponse = client.request(
                HttpRoutes.BASE_URL + HttpRoutes.DELETE_INGREDIENT
            ) {
                method = HttpMethod.Post
                headers {
                    append(HttpHeaders.ContentType, "application/json")
                }
                setBody(requestBody)
            }
            "Ingredient removed"
        }
    }
}