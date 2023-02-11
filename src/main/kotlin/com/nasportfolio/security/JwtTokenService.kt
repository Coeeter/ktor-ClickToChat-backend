package com.nasportfolio.security

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import java.util.*

class JwtTokenService : TokenService {
    override fun generateToken(
        secret: String,
        audience: String,
        issuer: String,
        expiresIn: Long,
        claims: List<TokenService.TokenClaim>
    ): String = JWT.create()
        .withAudience(audience)
        .withIssuer(issuer)
        .withExpiresAt(Date(System.currentTimeMillis() + expiresIn))
        .apply { claims.forEach { withClaim(it.key, it.value) } }
        .sign(Algorithm.HMAC256(secret))
}