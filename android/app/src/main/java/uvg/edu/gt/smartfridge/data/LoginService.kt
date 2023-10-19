package uvg.edu.gt.smartfridge.data

import io.ktor.client.HttpClient

class LoginService (client: HttpClient) : Service(client) {

    suspend fun login(username: String, password: String) : String {

        return ""
    }
}