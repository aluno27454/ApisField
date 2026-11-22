package pt.uptomar.dam2026.apisfield.data.repository

import pt.uptomar.dam2026.apisfield.data.local.SessionStore
import pt.uptomar.dam2026.apisfield.data.remote.ApiResult
import pt.uptomar.dam2026.apisfield.data.remote.ApiService
import pt.uptomar.dam2026.apisfield.data.remote.dto.LoginRequest
import pt.uptomar.dam2026.apisfield.data.remote.dto.RegisterRequest
import pt.uptomar.dam2026.apisfield.data.remote.safeApiCall
import pt.uptomar.dam2026.apisfield.data.remote.toDomain
import pt.uptomar.dam2026.apisfield.domain.model.User

/**
 * Login, registo e fim de sessão.
 *
 * É uma interface para os ViewModels poderem ser testados com uma versão falsa, sem rede
 * (ver LoginViewModelTest).
 */
interface AuthRepository {
    /** Inicia sessão e, se correr bem, guarda-a. */
    suspend fun login(email: String, password: String): ApiResult<User>

    /** Cria a conta e já fica com a sessão iniciada (a API devolve o token no registo). */
    suspend fun registar(nome: String, email: String, password: String): ApiResult<User>

    /** Termina a sessão neste dispositivo. */
    fun terminarSessao()
}

/** Implementação real: fala com a API e guarda a sessão no [SessionStore]. */
class AuthRepositoryImpl(
    private val api: ApiService,
    private val sessao: SessionStore,
) : AuthRepository {

    override suspend fun login(email: String, password: String): ApiResult<User> =
        guardarSeSucesso(safeApiCall {
            val res = api.login(LoginRequest(email.trim(), password))
            res.token to res.user.toDomain()
        })

    override suspend fun registar(nome: String, email: String, password: String): ApiResult<User> =
        guardarSeSucesso(safeApiCall {
            val res = api.register(RegisterRequest(nome.trim(), email.trim(), password))
            res.token to res.user.toDomain()
        })

    override fun terminarSessao() = sessao.limpar()

    /** Guarda o token e o utilizador quando o pedido corre bem e devolve só o utilizador. */
    private fun guardarSeSucesso(resultado: ApiResult<Pair<String, User>>): ApiResult<User> = when (resultado) {
        is ApiResult.Sucesso -> {
            val (token, user) = resultado.dados
            sessao.guardar(token, user)
            ApiResult.Sucesso(user)
        }
        is ApiResult.Falha -> resultado
    }
}
