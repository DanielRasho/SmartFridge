package uvg.edu.gt.smartfridge.data

import io.ktor.client.HttpClient
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.RedirectResponseException
import io.ktor.client.plugins.ServerResponseException
import uvg.edu.gt.smartfridge.models.Ingredient
import java.lang.Exception

// Representation of an HTTP Service.
open class Service (protected val client : HttpClient) {

    protected suspend inline fun <reified T> handleHttpRequest(
        requestBlock: suspend () -> List<T>): Result<List<T>> {

        val response : List<T> = emptyList()

        try {
            val response =  requestBlock()
            return Result.success(response)
        } catch (e: RedirectResponseException) {
            // 3xx - responses
            println("Error: ${e.response.status.description}")
            return Result.failure(e)
        } catch (e: ClientRequestException) {
            // 4xx - responses
            println("Error: ${e.response.status.description}")
            return Result.failure(e)
        } catch (e: ServerResponseException) {
            // 5xx - responses
            println("Error: ${e.response.status.description}")
            return Result.failure(e)
        } catch (e: Exception) {
            // General Exceptions
            println("Error: ${e.message}")
            return Result.failure(e)
        }
    }
}