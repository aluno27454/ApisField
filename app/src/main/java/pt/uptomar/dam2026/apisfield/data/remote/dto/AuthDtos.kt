package pt.uptomar.dam2026.apisfield.data.remote.dto

/*
 * DTOs (Data Transfer Objects) da autenticação.
 *
 * Um DTO tem exatamente a forma do JSON da API (docs/CONTRATO.md no repositório ApisField-API).
 * O Gson converte o JSON nestas classes pelo nome dos campos, por isso os nomes têm de ser
 * iguais aos da API (camelCase). Os ecrãs não usam DTOs: usam os modelos de domain/model,
 * criados a partir destes em DtoMappers.kt.
 */

/** Corpo de `POST /api/auth/login`. */
data class LoginRequest(val email: String, val password: String)

/** Corpo de `POST /api/auth/register`. */
data class RegisterRequest(val name: String, val email: String, val password: String)

/** Utilizador, tal como vem da API. `role` é `"user"` ou `"admin"`. */
data class UserDto(val id: Int, val name: String, val email: String, val role: String)

/** Resposta do login e do registo: o token de sessão e o utilizador. */
data class AuthResultDto(val token: String, val user: UserDto)
