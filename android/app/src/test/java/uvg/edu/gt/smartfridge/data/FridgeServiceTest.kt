package uvg.edu.gt.smartfridge.data

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.jupiter.api.BeforeEach
import uvg.edu.gt.smartfridge.models.Ingredient

class FridgeServiceTest {

    private lateinit var service: FridgeService
    private lateinit var httpClient: HttpClient
    @BeforeEach
    fun setUp() {
        httpClient = HttpClient(CIO) {
            install (Logging) {
                logger = Logger.DEFAULT
                filter { request ->  request.url.host.contains("ktor.io")  }
            }
        }
        service = FridgeService(httpClient)
    }

    @Test
    fun getIngredients() {
        runBlocking {
            val response : Result<List<Ingredient>> = service.getIngredients()
            if (response.isSuccess)
                println(response.getOrThrow())
            else
                response.exceptionOrNull()
        }
    }
}