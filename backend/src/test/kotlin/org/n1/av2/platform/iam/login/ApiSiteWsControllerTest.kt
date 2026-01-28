package org.n1.av2.platform.iam.login

import jakarta.servlet.http.Cookie
import org.hamcrest.Matchers.containsString
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.n1.av2.integration.service.TestUserService
import org.n1.av2.platform.iam.user.UserType
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.mongo.AutoConfigureDataMongo
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@AutoConfigureDataMongo
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureMockMvc
class ApiSiteWsControllerTest(
) {

    @Autowired
    lateinit var loginService: LoginService

    @Autowired
    lateinit var testUserService: TestUserService

    @Autowired
    lateinit var mvc: MockMvc


    lateinit var authCookie: Cookie

    @BeforeAll
    fun setup() {
        val user = testUserService.createHackerForTest("testGm", UserType.GM)
        authCookie = loginService.generateJwtCookie(user)
    }

    @Test
    fun `test simple get`() {
        mvc.perform(
            get("/openapi/google/clientId")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(content().string("""{"clientId":""}"""))
    }

    @Test
    fun `editSite input validation`() {
        val invalidJwt ="hack attempt \\u0000" // invalid 0 character, escaped for json
        mvc.perform(
            post("/openapi/login/google")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"jwt": "$invalidJwt"}""")
                .cookie(authCookie)
        )
            .andExpect(status().isBadRequest())
            .andExpect(content().string(containsString("must match \"[\\p")))
    }

}
