package com.pdm0126.outfix.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*

// Configuración JWT — en producción usar variables de entorno
object JwtConfig {
    const val SECRET = "outfix-jwt-secret-cambiar-en-produccion"
    const val ISSUER = "outfix-api"
    const val AUDIENCE = "outfix-users"
    const val REALM = "OutFix API"

    // Genera un token JWT con el userId y email dentro del payload
    fun generateToken(userId: String, email: String): String {
        return JWT.create()
            .withAudience(AUDIENCE)
            .withIssuer(ISSUER)
            .withClaim("userId", userId)
            .withClaim("email", email)
            .withExpiresAt(
                java.util.Date(System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000) // 7 días
            )
            .sign(Algorithm.HMAC256(SECRET))
    }
}

fun Application.configureSecurity() {
    authentication {
        jwt("auth-jwt") {
            realm = JwtConfig.REALM
            verifier(
                JWT.require(Algorithm.HMAC256(JwtConfig.SECRET))
                    .withAudience(JwtConfig.AUDIENCE)
                    .withIssuer(JwtConfig.ISSUER)
                    .build()
            )
            validate { credential ->
                // Si el token tiene el claim "userId", es válido
                if (credential.payload.getClaim("userId").asString() != null) {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }
            challenge { _, _ ->
                call.respond(
                    HttpStatusCode.Unauthorized,
                    mapOf(
                        "success" to false,
                        "message" to "Token inválido o expirado",
                        "data" to null
                    )
                )
            }
        }
    }
}
