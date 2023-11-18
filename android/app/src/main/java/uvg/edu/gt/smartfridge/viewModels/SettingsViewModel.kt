package uvg.edu.gt.smartfridge.viewModels

import androidx.lifecycle.ViewModel
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import uvg.edu.gt.smartfridge.data.SettingsService
import uvg.edu.gt.smartfridge.models.UserSettings

class SettingsViewModel: ViewModel() {

    private val _httpClient = HttpClient(CIO) {
        install(Logging) {
            logger = Logger.DEFAULT
            filter { request -> request.url.host.contains("ktor.io") }
        }
    }

    private val settingsService = SettingsService(_httpClient)

    suspend fun saveSettings(JWT_TOKEN: String, userSettings : UserSettings) : Result<String> {
        return settingsService.saveSettings(JWT_TOKEN, userSettings)
    }

    suspend fun logout(JWT_TOKEN: String) : Result<String>{
        return settingsService.logout(JWT_TOKEN)
    }
}