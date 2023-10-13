package uvg.edu.gt.smartfridge.views

import androidx.lifecycle.ViewModel
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.request
import uvg.edu.gt.smartfridge.data.FridgeService

class FridgeViewModel : ViewModel() {

    private val _httpClient = HttpClient(CIO) {
        install (Logging) {
            logger = Logger.DEFAULT
            filter { request ->  request.url.host.contains("ktor.io")  }
        }
    }

    private val fridgeViewModel = FridgeService(_httpClient)


}