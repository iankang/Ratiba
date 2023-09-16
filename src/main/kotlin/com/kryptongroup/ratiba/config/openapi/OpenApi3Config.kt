package com.kryptongroup.ratiba.config.openapi

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.*
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.info.BuildProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import springfox.documentation.service.AuthorizationScope
import springfox.documentation.service.SecurityReference
import springfox.documentation.spi.service.contexts.SecurityContext
import java.util.*
import java.util.List


@Configuration
class OpenApi3Config(
    @Value("\${oidc.auth_url}")
    private val authUrl:String,
    @Value("\${oidc.token_url}")
    private val tokenUrl:String ,
) {
    private val SCHEME_NAME = "bearerScheme"
    private val SCHEME = "Bearer"
    @Bean
    fun openAPI(): OpenAPI? {
        return OpenAPI()
            .components(
                Components()
                    .addSecuritySchemes(
                        "oauth2", SecurityScheme()
                            .type(SecurityScheme.Type.OAUTH2)
                            .description("OAuth2 Flow")
                            .flows(
                                OAuthFlows()
                                    .authorizationCode(
                                        OAuthFlow()
                                            .authorizationUrl(authUrl)
                                            .tokenUrl(tokenUrl)
                                            .scopes(Scopes())
                                    )
                            )
                    )  .addSecuritySchemes(
                        "basicAuth", SecurityScheme()
                            .type(SecurityScheme.Type.HTTP)
                            .description("basic Auth")
                            .scheme("basic")
                    ).addSecuritySchemes(SCHEME_NAME, createSecurityScheme())
            )
            .security(
                List.of(
                    SecurityRequirement()
                        .addList("oauth2"),
                    SecurityRequirement()
                        .addList("basicAuth"),
                    SecurityRequirement()
                        .addList(SCHEME_NAME)
                )
            )
            .info(
                Info()
                    .title("Ratiba REST Service")
                    .description(
                        """
REST Service used to
secure a Spring REST service with
FusionAuth.
                     
                     """.trimIndent()
                    )
                    .version("1.0")
            )
    }

    private fun securityContext(): SecurityContext? {
        return SecurityContext.builder().securityReferences(defaultAuth()).build()
    }

    private fun defaultAuth(): kotlin.collections.List<SecurityReference?>? {
        val authorizationScope = AuthorizationScope("global", "accessEverything")
        val authorizationScopes: Array<AuthorizationScope?> = arrayOfNulls<AuthorizationScope>(1)
        authorizationScopes[0] = authorizationScope
        return Arrays.asList(SecurityReference("JWT", authorizationScopes))
    }

    private fun createSecurityScheme(): SecurityScheme {
        return SecurityScheme()
            .name(SCHEME_NAME)
            .type(SecurityScheme.Type.HTTP)
            .scheme(SCHEME)
    }
}