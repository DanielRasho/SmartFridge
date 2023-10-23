package uvg.edu.gt.smartfridge.viewModels

import androidx.lifecycle.ViewModel
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import uvg.edu.gt.smartfridge.data.LoginService
import uvg.edu.gt.smartfridge.models.UserSettings

class LoginViewModel : ViewModel() {

    private val _httpClient : HttpClient = HttpClient(CIO) {
        install (Logging) {
            logger = Logger.DEFAULT
            filter { request ->  request.url.host.contains("ktor.io")  }
        }
    };

    private val loginService: LoginService = LoginService(_httpClient)

    suspend fun sendLoginCredentials(username : String, password: String) : Result<Pair<String, UserSettings>>{
        return loginService.login(username, password)
    }

}