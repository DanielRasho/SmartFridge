package uvg.edu.gt.smartfridge.data

// Throw when something went wrong on a http request.
class ResponseException(
    val statusCode: Int,
    message: String
) : Exception(message)
