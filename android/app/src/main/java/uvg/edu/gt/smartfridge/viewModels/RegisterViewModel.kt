package uvg.edu.gt.smartfridge.viewModels

import androidx.lifecycle.ViewModel
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import uvg.edu.gt.smartfridge.data.LoginService
import uvg.edu.gt.smartfridge.data.RegisterService
import uvg.edu.gt.smartfridge.models.UserSettings

class RegisterViewModel : ViewModel() {

    private val _httpClient : HttpClient = HttpClient(CIO) {
        install (Logging) {
            logger = Logger.DEFAULT
            filter { request ->  request.url.host.contains("ktor.io")  }
        }
    }

    private val registerService: RegisterService = RegisterService(_httpClient)

    suspend fun sendNewUserCredentials(username : String, password: String) : Result<String>{
        return registerService.register(username,password)
    }
}