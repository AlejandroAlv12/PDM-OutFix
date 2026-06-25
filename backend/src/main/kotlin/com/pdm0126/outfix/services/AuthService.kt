package com.pdm0126.outfix.services

import com.pdm0126.outfix.database.tables.Users
import com.pdm0126.outfix.models.*
import com.pdm0126.outfix.plugins.JwtConfig
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.mindrot.jbcrypt.BCrypt

object AuthService {

    // Registro: crea usuario y retorna token JWT
    fun register(request: RegisterRequest): AuthResponse {
        // Verificar si el email ya existe
        val existing = transaction {
            Users.selectAll().where { Users.email eq request.email }.singleOrNull()
        }
        if (existing != null) {
            throw IllegalArgumentException("Ya existe un usuario con ese email")
        }

        // Hashear contraseña y crear usuario
        val passwordHash = BCrypt.hashpw(request.password, BCrypt.gensalt())

        val userId = transaction {
            Users.insert {
                it[email] = request.email
                it[Users.passwordHash] = passwordHash
                it[displayName] = request.displayName
            } get Users.id
        }

        val user = UserResponse(
            id = userId.toString(),
            email = request.email,
            displayName = request.displayName
        )
        val token = JwtConfig.generateToken(userId.toString(), request.email)

        return AuthResponse(user = user, token = token)
    }

    // Login: valida credenciales y retorna token JWT
    fun login(request: LoginRequest): AuthResponse {
        val row = transaction {
            Users.selectAll().where { Users.email eq request.email }.singleOrNull()
        } ?: throw IllegalArgumentException("Credenciales incorrectas")

        // Verificar contraseña con BCrypt
        if (!BCrypt.checkpw(request.password, row[Users.passwordHash])) {
            throw IllegalArgumentException("Credenciales incorrectas")
        }

        val user = UserResponse(
            id = row[Users.id].toString(),
            email = row[Users.email],
            displayName = row[Users.displayName]
        )
        val token = JwtConfig.generateToken(user.id, user.email)

        return AuthResponse(user = user, token = token)
    }
}
