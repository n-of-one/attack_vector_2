package org.n1.mainframe.backend.web.html

import org.n1.mainframe.backend.service.PrincipalService
import org.springframework.boot.web.servlet.error.ErrorAttributes
import org.springframework.boot.web.servlet.error.ErrorController
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.context.request.ServletWebRequest
import javax.servlet.http.HttpServletRequest


private const val INDEX = "../static/index.html"

@Controller
class HtmlController(
        val errorAttributes: ErrorAttributes,
        val principalService: PrincipalService
) : ErrorController {
    override fun getErrorPath(): String {
        return "error"
    }

    @GetMapping("/")
    fun default(): String {
        return INDEX
    }

    @GetMapping("/login")
    fun login(): String {
        return INDEX
    }

    @GetMapping("/login/")
    fun loginSlash(): String {
        return INDEX
    }

    @GetMapping("/hacker")
    fun hacker(): String {
        return INDEX
    }

    @GetMapping("/hacker/")
    fun hackerSlash(): String {
        return INDEX
    }

    @GetMapping("/gm")
    fun gm(): String {
        return INDEX
    }

    @GetMapping("/gm/")
    fun gmSlash(): String {
        return INDEX
    }

    @GetMapping("/edit")
    fun edit(): String {
        return INDEX
    }

    @GetMapping("/edit/")
    fun editSlash(): String {
        return INDEX
    }

    @GetMapping("/edit/{siteId}")
    fun editValue(): String {
        return INDEX
    }

    @GetMapping("/forceError")
    fun forceError(): String {
        throw RuntimeException("intentional problem for test.")
    }

    @ExceptionHandler(Exception::class)
    @RequestMapping("/error")
    fun error(request: HttpServletRequest, ex: Exception, model: Model): String {
//        val errorPage = ModelAndView ("error")

        val requestAttributes = ServletWebRequest(request)
        val errorMap = errorAttributes.getErrorAttributes(requestAttributes, true)
        val cause = errorAttributes.getError(requestAttributes)

        model.addAttribute("timestamp", errorMap["timestamp"])    // 1484162659884
        model.addAttribute("status", errorMap["status"])          // 404 | 500
        model.addAttribute("error", errorMap["error"])            // Not Found | Internal Server error
        model.addAttribute("message", errorMap["message"])        // Not Found | org.springframework.web.util.NestedServletException: Request processing failed; nested exception is java.lang.RuntimeException: die
        model.addAttribute("path", errorMap["path"])              // /gm/sitesa
        model.addAttribute("exception", errorMap["exception"])    // "java.lang.RuntimeException"
        model.addAttribute("fullTrace", cause)                        // the actual exception with stack trace

        if (errorMap["status"] == 403) {
            model.addAttribute("message", "403: Access denied to: ${errorMap["path"]}")
        }

        if (errorMap["status"] == 404) {
            model.addAttribute("message", "Page not found: ${errorMap["path"]}")
        }

        return "error"
    }


//
//    @GetMapping("/edit/{link}")
//    fun edit(): String  {
//        return "redirect:/index.html"
//    }


}