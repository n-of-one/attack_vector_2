package org.n1.av2.backend.web.rest

import jakarta.servlet.http.Cookie
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.notNullValue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.n1.av2.platform.iam.user.UserEntityService
import org.n1.av2.platform.iam.user.UserType
import org.n1.av2.platform.iam.login.LoginService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.mongo.AutoConfigureDataMongo
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

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
    lateinit var userEntityService: UserEntityService

    @Autowired
    lateinit var mvc: MockMvc


    lateinit var authCookie: Cookie

    @BeforeAll
    fun setup() {
        val user = userEntityService.createUserForTest("testGm", UserType.GM)
        authCookie = loginService.generateJwtCookie(user)
    }

    @Test
    fun `editSite happy flow`() {
        mvc.perform(
            post("/api/site/edit")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"siteName": "Hello"}""")
                .cookie(authCookie)
        )
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id", `is`(notNullValue())))
    }

    @Test
    fun `editSite input validation`() {
        val invalidSiteName = "hackattempt \\u0000" // invalid 0 character, escaped for json
        mvc.perform(
            post("/api/site/edit")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"siteName": "$invalidSiteName"}""")
                .cookie(authCookie)
        )
            .andExpect(status().isBadRequest())
    }


    @Test
    fun `deleteSite happy flow`() {
        mvc.perform(
            post("/api/site/edit")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"siteName": "Hello"}""")
                .cookie(authCookie)
        )
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id", `is`(notNullValue())))
    }
}
