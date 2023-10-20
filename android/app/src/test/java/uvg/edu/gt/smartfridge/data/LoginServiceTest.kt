package uvg.edu.gt.smartfridge.data

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import uvg.edu.gt.smartfridge.models.Settings

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class LoginServiceTest {

    private lateinit var loginService: LoginService
    private lateinit var httpClient: HttpClient

    @BeforeAll
    fun setup(){
        httpClient = HttpClient(CIO) {
            install (Logging) {
                logger = Logger.DEFAULT
                filter { request ->  request.url.host.contains("ktor.io")  }
            }
        }
        //Log.i("INFO","HEY THERE")
        loginService = LoginService(httpClient)
    }

    @Test
    fun login() {
        println("HEY THERE!")
        runBlocking {
            val response : Result<Pair<String, Settings>> = loginService.login(
                "smaugthur", "1234")
            if (response.isSuccess){
                val (token, settings) = response.getOrThrow()
                println("\tTOKEN : $token")
                println("\tSettings: {$settings}")
            }

            else
                response.exceptionOrNull()
        }
    }
}