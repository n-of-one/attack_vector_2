package org.n1.av2.platform.iam.login

import org.hamcrest.Matchers.containsString
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.n1.av2.platform.config.ConfigItem
import org.n1.av2.platform.config.ConfigService
import org.n1.av2.platform.connection.ConnectionService
import org.n1.av2.platform.iam.user.CurrentUserService
import org.n1.av2.platform.iam.authentication.JwtAuthenticationFilter
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

/**
 * Web slice test: controller wiring, JSON shape and input validation only.
 * The real filter chain + cookie auth is covered by the websocket integration tests.
 * (openapi paths are permitAll in production, so filters are disabled here.)
 */
@WebMvcTest(LoginRestController::class)
@AutoConfigureMockMvc(addFilters = false)
class ApiSiteWsControllerTest {

    @MockitoBean
    lateinit var loginService: LoginService

    @MockitoBean
    lateinit var configService: ConfigService

    // Replaces the bean definition: the real filter's constructor pulls in JwtTokenProvider,
    // which reads config in its constructor.
    @MockitoBean
    lateinit var jwtAuthenticationFilter: JwtAuthenticationFilter

    // Needed by ConstraintViolationWsExceptionHandler, a @ControllerAdvice included in the slice.
    @MockitoBean
    lateinit var connectionService: ConnectionService

    @MockitoBean
    lateinit var currentUserService: CurrentUserService

    @Autowired
    lateinit var mvc: MockMvc

    @BeforeEach
    fun stub() {
        given(configService.get(ConfigItem.LOGIN_GOOGLE_CLIENT_ID)).willReturn("")
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
        )
            .andExpect(status().isBadRequest())
            .andExpect(content().string(containsString("must match \"[\\p")))
    }

}
