package uvg.edu.gt.smartfridge.data

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import uvg.edu.gt.smartfridge.models.Ingredient

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class FridgeServiceTest {
    private lateinit var fridgeService: FridgeService
    private lateinit var httpClient: HttpClient
    private lateinit var JWT_TOKEN : String

    @BeforeAll
    fun setUp() {
        httpClient = HttpClient(CIO) {
            install (Logging) {
                logger = Logger.DEFAULT
                filter { request ->  request.url.host.contains("ktor.io")  }
            }
        }
        //Log.i("INFO","HEY THERE")
        fridgeService = FridgeService(httpClient)
    }

    @Test
    fun getIngredients() {
        println("HEY THERE")

        runBlocking {
            val response : Result<List<Ingredient>> = fridgeService.getIngredients("")
            if (response.isSuccess)
                println(response.getOrThrow())
            else
                response.exceptionOrNull()
        }
    }
}