package com.kryptongroup.ratiba.config.security

import org.springframework.core.convert.converter.Converter
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.util.StringUtils
import java.util.stream.Collectors


class OidcJwtAuthConverter(
    private val EMAIL_CLAIM :String= "email",
    private val ROLES_CLAIM: String = "roles"
): Converter<Jwt,AbstractAuthenticationToken> {

    override fun convert(source: Jwt): AbstractAuthenticationToken? {
        return UsernamePasswordAuthenticationToken(
            getUserName(jwt = source),
            "n/a",
            getAuthorities(jwt = source)
        )
    }

    private fun getUserName(jwt: Jwt): String? {
        return jwt.getClaimAsString(EMAIL_CLAIM)
    }

    private fun getAuthorities(jwt: Jwt): MutableSet<SimpleGrantedAuthority>? {
        return this.getRoles(jwt).stream()
            .map { role -> SimpleGrantedAuthority(role.toLowerCase()) }
            .collect(Collectors.toSet())
    }

    private fun getRoles(jwt: Jwt): Collection<String> {
        val claim = jwt.getClaims()[ROLES_CLAIM]
        if (claim is String && StringUtils.hasText(claim)) {
            return claim.split(" ")
        }
        if (claim is Collection<*>) {
            return claim.map { it.toString() }.toSet()
        }
        return emptyList()
    }
}