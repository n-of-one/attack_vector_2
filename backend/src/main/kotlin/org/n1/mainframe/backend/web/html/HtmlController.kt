package org.n1.mainframe.backend.web.html

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

@Controller
class HtmlController {

    @GetMapping("/")
    fun index(): String  {
        return "index"
    }

    @GetMapping("/edit/{link}")
    fun edit(): String  {
        return "index"
    }


}