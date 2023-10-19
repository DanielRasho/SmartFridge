package uvg.edu.gt.smartfridge.data

import io.ktor.client.HttpClient

class RegisterService (client: HttpClient) : Service(client) {

    suspend fun register(username: String, password: String) : Result<String> {

        return Result.success("")
    }

}