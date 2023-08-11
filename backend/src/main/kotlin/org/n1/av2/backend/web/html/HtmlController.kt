package org.n1.av2.backend.web.html

import org.springframework.boot.web.servlet.error.ErrorController
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ResponseBody


private const val INDEX = "../static/index.html"

@Controller
class HtmlController(
) : ErrorController {

    @GetMapping("/")
    fun default(): String {
        return INDEX
    }

    @GetMapping("/hello")
    @ResponseBody
    fun hello(): String {
        return "Hello to you too!"
    }

}