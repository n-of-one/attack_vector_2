package org.n1.av2.frontend.web.html

import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.n1.av2.platform.config.ConfigItem
import org.n1.av2.platform.config.ConfigService
import org.n1.av2.platform.iam.login.LoginService
import org.springframework.boot.web.servlet.error.ErrorController
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import java.io.File
import java.net.URLDecoder

private const val INDEX = "../static/index.html"

@Controller
class HtmlController(
    private val loginService: LoginService,
    private val configService: ConfigService,
) : ErrorController {

    private val localRoot = File("local")

    @GetMapping(
        "/",
        "/login",
        "/adminLogin",
        "/loggedOut",
        "/about",
        "/privacy",
        "/hacker",
        "/hacker/",
        "/gm",
        "/gm/",
        "/edit",
        "/edit/",
        "/edit/{siteId}",
        "/x/{reference}",
        "/o/{reference}",
        "/website",
        "/website/{page}",
        "/larp/**"
    )
    fun default(): String {
        return INDEX
    }

    @GetMapping("/local/**")
    fun local(request: HttpServletRequest): ResponseEntity<ByteArray> {
        if (!localRoot.exists() || !localRoot.isDirectory) {
            return ResponseEntity(
                "local content folder does not exist. create folder: ${localRoot.canonicalPath}".toByteArray(),
                HttpStatus.INTERNAL_SERVER_ERROR
            )
        }

        val path = URLDecoder.decode(request.requestURI.substringAfter("/local/"), "UTF-8")
        val file = File(localRoot.canonicalPath + File.separator + path)

        if (!fileInLocalRoot(file)) {
            return ResponseEntity("invalid path".toByteArray(), HttpStatus.BAD_REQUEST)
        }

        if (!file.exists()) {
            return ResponseEntity("file not found: ${file.canonicalPath}".toByteArray(), HttpStatus.NOT_FOUND)
        }

        val headers = HttpHeaders()
        headers.add("Content-Type", guessMimeType(file.extension))

        return ResponseEntity(file.readBytes(), headers, HttpStatus.OK)
    }

    private fun fileInLocalRoot(candidate: File): Boolean {
        if (candidate.parentFile.canonicalPath == localRoot.canonicalPath) {
            return true
        }
        if (candidate.parentFile == null) {
            return false
        }
        return fileInLocalRoot(candidate.parentFile)
    }

    fun guessMimeType(extension: String): String {
        val extensionNormalized = extension.lowercase()

        when (extensionNormalized) {
            "png", "jpeg", "gif", "webp" -> return "image/$extensionNormalized"
            "jpg" -> return "image/jpeg"
            "svg" -> return "image/svg+xml"
            "txt", "text" -> return "text/plain"
            "pdf" -> return "application/pdf"
            "html" -> return "text/html"
            "css" -> return "text/css"
            "js" -> return "text/javascript"
            "woff" -> return "font/woff"
            "woff2" -> return "font/woff2"
            "ttf" -> return "font/ttf"
            "otf" -> return "font/otf"
            "eot" -> return "font/eot"
            else -> return "application/octet-stream"
        }
    }

    @GetMapping("/localLogout")
    fun logout(response: HttpServletResponse) {
        val logoutCookies = loginService.logout()
        logoutCookies.forEach {
            val cookie = Cookie(it.name, "")
            cookie.path = "/"
            cookie.maxAge = 0
            response.addCookie(cookie)
        }

        response.sendRedirect("/loggedOut")
    }

    @GetMapping("/forceError")
    fun forceError(): String {
        throw RuntimeException("intentional problem for test.")
    }
}
