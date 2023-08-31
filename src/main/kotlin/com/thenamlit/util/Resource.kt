package com.thenamlit.util

import io.ktor.http.*


typealias SimpleResource = Resource<Unit>

sealed class Resource<T>(
    val data: T? = null,
    val message: String? = null,
    val logging: String? = null,
    val httpStatusCode: HttpStatusCode? = null
) {
    class Success<T>(
        data: T?,
        message: String? = null,
        logging: String? = null,
        httpStatusCode: HttpStatusCode? = null
    ) :
        Resource<T>(data = data, message = message, logging = logging, httpStatusCode = httpStatusCode)

    class Error<T>(
        message: String? = null,
        data: T? = null,
        logging: String? = null,
        httpStatusCode: HttpStatusCode? = null
    ) :
        Resource<T>(data = data, message = message, logging = logging, httpStatusCode = httpStatusCode)
}
