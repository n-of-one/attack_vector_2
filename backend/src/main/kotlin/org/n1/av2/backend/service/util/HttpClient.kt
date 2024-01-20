package org.n1.av2.backend.service.util

import jakarta.servlet.http.Cookie
import java.net.HttpURLConnection
import java.net.URI

class HttpClient {

    fun get(
        urlString: String,
        props: Map<String, String>,
        cookies: Array<Cookie> = emptyArray()
    ): String {
        val url = URI(urlString).toURL()
        val conn: HttpURLConnection = url.openConnection() as HttpURLConnection
        try {
            conn.requestMethod = "GET"
            conn.setRequestProperty("Accept", "application/json")
            conn.setRequestProperty("Cookie", cookies.joinToString("; ") { "${it.name}=${it.value}" })
            props.forEach { conn.setRequestProperty(it.key, it.value) }

            if (conn.responseCode != 200) {
                error("Failed : HTTP Error code ${conn.getResponseCode()}")
            }
            return conn.inputStream.bufferedReader().readText()
        } finally {
            conn.disconnect()
        }
    }

}