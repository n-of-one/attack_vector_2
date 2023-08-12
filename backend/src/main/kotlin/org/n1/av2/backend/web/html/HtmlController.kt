package org.n1.av2.backend.web.html

import org.springframework.boot.web.servlet.error.ErrorController
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ResponseBody


private const val INDEX = "../static/index.html"

@Controller
class HtmlController(
) : ErrorController {

    private val logger = mu.KotlinLogging.logger {}

    @GetMapping("/")
    fun default(): String {
        logger.info("Called default")
        return INDEX
    }

    @GetMapping("/hello")
    @ResponseBody
    fun hello(): String {
        logger.error("oh no, hello!")
        return "Hello to you too!"
    }

}