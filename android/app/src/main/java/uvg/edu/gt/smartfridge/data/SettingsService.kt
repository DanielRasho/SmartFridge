package uvg.edu.gt.smartfridge.data

import io.ktor.client.HttpClient
import io.ktor.client.request.headers
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import uvg.edu.gt.smartfridge.models.Settings

class SettingsService(client: HttpClient) : Service(client) {

    suspend fun logout( JWT_TOKEN : String) : Result<String>{

        return handleHttpRequest {
            // Creating Request body
            val requestBody = buildJsonObject {
                put("token", JWT_TOKEN)
            }.toString()

            // Making Request
            val response : HttpResponse = client.request(
                HttpRoutes.BASE_URL + HttpRoutes.LOGOUT
            ){
                method = HttpMethod.Post
                headers {
                    append(HttpHeaders.ContentType, "application/json")
                }
                setBody( requestBody )
            }
            "Session Closed"
        }
    }

    suspend fun saveSettings( JWT_TOKEN : String, settings : Settings) : Result<String>{

        return handleHttpRequest {
            // Creating Request body
            val requestBody = buildJsonObject {
                put("token", JWT_TOKEN)
                put("settings", Json.encodeToString(settings))
            }.toString()

            // Making Request
            val response : HttpResponse = client.request(
                HttpRoutes.BASE_URL + HttpRoutes.SAVE_SETTINGS
            ){
                method = HttpMethod.Post
                headers {
                    append(HttpHeaders.ContentType, "application/json")
                }
                setBody( requestBody )
            }
            "Settings saved"
        }
    }
}