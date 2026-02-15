package org.n1.av2.platform.util

import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import java.net.HttpURLConnection
import java.net.URI

class HttpClient {

    fun get(
        urlString: String,
        props: Map<String, String>,
        cookies: Array<Cookie> = emptyArray()
    ): String {
        return call("GET", urlString, props, cookies)
    }


    fun post(urlString: String, body: String, props: Map<String, String>, cookies: Array<Cookie> = emptyArray()): String {
        return call("POST", urlString, props, cookies, body)
    }


    private fun call(
        method: String,
        urlString: String,
        props: Map<String, String>,
        cookies: Array<Cookie> = emptyArray(),
        body: String? = null,
    ): String {
        val url = URI(urlString).toURL()
        val conn: HttpURLConnection = url.openConnection() as HttpURLConnection
        try {
            conn.requestMethod = method
            conn.setRequestProperty("Accept", "application/json")
            if (cookies.isNotEmpty()) {
                conn.setRequestProperty("Cookie", cookies.joinToString("; ") { "${it.name}=${it.value}" })
            }
            if (props.isNotEmpty()) {
                props.forEach { conn.setRequestProperty(it.key, it.value) }
            }

            if (!body.isNullOrEmpty()) {
                conn.doOutput = true
                conn.getOutputStream().write(body.toByteArray())
            }

            if (conn.responseCode != 200) {
                error("Failed : HTTP Error code ${conn.getResponseCode()}")
            }
            return conn.inputStream.bufferedReader().readText()
        } finally {
            conn.disconnect()
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
