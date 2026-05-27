package org.n1.av2.platform.util

import io.jsonwebtoken.io.IOException
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import org.springframework.beans.factory.annotation.Value
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL


class HttpClient {

    @Value("\${http-client.log-request}")
    private var enableLog: Boolean = false

    private val logger = mu.KotlinLogging.logger {}

    class Response(val code: Int, val body: String) {
        fun isOk(): Boolean = code in 200..299
    }

    fun get(
        urlString: String,
        props: Map<String, String>,
        cookies: Array<Cookie> = emptyArray()
    ): String {
        val response = call("GET", urlString, props, cookies)
        if (!response.isOk()) {
            error("Failed : HTTP Error code ${response.code}")
        }
        return response.body
    }


    fun post(urlString: String, body: String, props: Map<String, String>, cookies: Array<Cookie> = emptyArray()): Response {
        return call("POST", urlString, props, cookies, body)
    }


    private fun call(
        method: String,
        urlString: String,
        props: Map<String, String>,
        cookies: Array<Cookie> = emptyArray(),
        requestBody: String? = null,
    ): Response {
        return try {
            getResponseAndFollowRedirect(
                URI(urlString).toURL(),
                method,
                props,
                cookies,
                requestBody
            )
        } catch (e: IOException) {
            Response(-1, e.message ?: "Unknown error")
        }
    }

    /**
     * Enable redirection between HTTP URL and HTTPS URL
     */
    private fun getResponseAndFollowRedirect(
        url: URL,
        method: String,
        props: Map<String, String>,
        cookies: Array<Cookie> = emptyArray(),
        requestBody: String? = null,
    ): Response {
        var conn = url.openConnection() as HttpURLConnection

        try {
            var nbOfRedirects = 0
            var responseBody = ""
            while (nbOfRedirects < 5) {
                conn.setInstanceFollowRedirects(false)

                conn.requestMethod = method
                conn.setRequestProperty("Accept", "application/json")
                if (cookies.isNotEmpty()) {
                    conn.setRequestProperty("Cookie", cookies.joinToString("; ") { "${it.name}=${it.value}" })
                }
                if (props.isNotEmpty()) {
                    props.forEach { conn.setRequestProperty(it.key, it.value) }
                }

                logRequest(conn, requestBody)
                if (!requestBody.isNullOrEmpty()) {
                    conn.setDoOutput(true)
                    conn.getOutputStream().write(requestBody.toByteArray())
                }

                // Read response body
                responseBody = conn.inputStream.bufferedReader().readText()
                logResponse(conn, responseBody)
                when (conn.responseCode) {
                    HttpURLConnection.HTTP_MOVED_PERM, HttpURLConnection.HTTP_MOVED_TEMP, 307, 308 -> {
                        val loc = conn.getHeaderField("Location")
                        val target = URI(loc).toURL()
                        logger.info("redirected to $target")
                        conn.disconnect()

                        nbOfRedirects += 1

                        conn = target.openConnection() as HttpURLConnection
                    }

                    else -> {
                        return Response(conn.responseCode, responseBody)
                    }
                }
            }

            // Read response body
            return Response(conn.responseCode, responseBody)

        } catch (e: Exception) {
            logger.error(e.message, e)
            if (conn.errorStream != null) {
                val responseBody = conn.errorStream.bufferedReader().readText()
                logResponse(conn, responseBody)
                return Response(conn.responseCode, responseBody)
            }
            return Response(-1, e.message ?: e.javaClass.simpleName)
        } finally {
            conn.disconnect()
        }
    }


    private fun logRequest(conn: HttpURLConnection, body: String?) {
        if (!enableLog) return
        logger.info("Request URL : ${conn.url}")
        logger.info("Request Method : ${conn.requestMethod}")
        logger.info("Request Headers : ${conn.requestProperties}")
        if (!body.isNullOrEmpty()) {
            logger.info("Request Body : $body")
        }
    }

    private fun logResponse(conn: HttpURLConnection, body: String?) {
        if (!enableLog) return
        logger.info("Response Status Code : ${conn.responseCode} ${conn.responseMessage}")
        logger.info("Response Headers : ${conn.headerFields}")
        if (!body.isNullOrEmpty()) {
            logger.info("Response Body : $body")
        }
    }

}

fun getIp(request: HttpServletRequest): String {
    val xfHeader = request.getHeader("X-Forwarded-For") ?: return request.remoteAddr
    if (!xfHeader.contains(request.remoteAddr)) {
        return request.remoteAddr
    }
    return xfHeader.split(",")[0].trim()
}
