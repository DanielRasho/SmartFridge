package uvg.edu.gt.smartfridge.data

import io.ktor.client.HttpClient
import io.ktor.client.request.headers
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class RegisterService (client: HttpClient) : Service(client) {

    suspend fun register(username: String, password: String) : Result<String> {
        return handleHttpRequest {

            // Creating Request body
            val requestBody = buildJsonObject {
                put("username", username)
                put("password", password)
            }.toString()

            // Making Request
            val response : HttpResponse = client.request(
                HttpRoutes.BASE_URL + HttpRoutes.LOGIN){
                method = HttpMethod.Post
                headers {
                    append(HttpHeaders.ContentType, "application/json")
                }
                setBody( requestBody )
            }

            "Register Successful"
        }
    }

}