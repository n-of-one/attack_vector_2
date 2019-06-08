package org.n1.mainframe.backend.web.html

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

private const val INDEX = "../static/index.html"

@Controller
class HtmlController {

    @GetMapping("/")
    fun default(): String  {
        return INDEX
    }

    @GetMapping("/login")
    fun login(): String  {
        return INDEX
    }
    @GetMapping("/login/")
    fun loginSlash(): String  {
        return INDEX
    }

    @GetMapping("/hacker")
    fun hacker(): String  {
        return INDEX
    }
    @GetMapping("/hacker/")
    fun hackerSlash(): String  {
        return INDEX
    }

    @GetMapping("/gm")
    fun gm(): String  {
        return INDEX
    }

    @GetMapping("/gm/")
    fun gmSlash(): String  {
        return INDEX
    }

    @GetMapping("/edit")
    fun edit(): String  {
        return INDEX
    }
    @GetMapping("/edit/")
    fun editSlash(): String  {
        return INDEX
    }
    @GetMapping("/edit/{siteId}")
    fun editValue(): String  {
        return INDEX
    }


//
//    @GetMapping("/edit/{link}")
//    fun edit(): String  {
//        return "redirect:/index.html"
//    }


}