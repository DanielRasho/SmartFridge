package uvg.edu.gt.smartfridge.data

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.headers
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.URLBuilder
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.json.JSONObject
import uvg.edu.gt.smartfridge.models.UserSettings

class LoginService (client: HttpClient) : Service(client) {

    suspend fun login(username: String, password: String) : Result<Pair<String, UserSettings>> {
        return handleHttpRequest {

            // Creating Request body
            val requestBody = buildJsonObject {
                put("username", username)
                put("password", password)
            }.toString()

            // JUST TYPE THE CURREN PRIVATE IP ADDRESS WITH "ip address"
            val requestUrl = URLBuilder( host = "192.168.0.2", port = 3000,
                pathSegments = listOf("user", "login")
            )

            println("HELLO!")
            println(requestUrl.toString())

            // Making Request
            val response : HttpResponse = client.request(
                HttpRoutes.BASE_URL + HttpRoutes.LOGIN){
                method = HttpMethod.Post
                headers {
                    append(HttpHeaders.ContentType, "application/json")
                }
                setBody( requestBody )
            }

            // Translating JSON response
            val data : JSONObject = JSONObject(response.body() as String)

            println(data.toString())
            // Fetching Data
            val JWToken : String = data.getString("token")
            println("AFTER")
            val userSettings: UserSettings = Json.decodeFromString(
                data.getJSONObject("preferences").toString())

            Pair(JWToken, userSettings)
        }
    }
}