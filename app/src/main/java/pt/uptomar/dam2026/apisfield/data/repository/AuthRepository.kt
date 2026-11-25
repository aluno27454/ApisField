package pt.uptomar.dam2026.apisfield.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import pt.uptomar.dam2026.apisfield.data.local.AppDatabase
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

    /**
     * Quantas visitas e avistamentos gravados no telemóvel ainda não chegaram à API.
     * São os que se perdem se a sessão terminar; o diálogo de terminar sessão usa este número para avisar.
     */
    suspend fun registosPorEnviar(): Int

    /**
     * Termina a sessão neste dispositivo e apaga os dados locais (colmeias em cache, visitas e
     * avistamentos), para não ficarem à vista de quem entrar a seguir no mesmo telemóvel.
     */
    suspend fun terminarSessao()
}

/** Implementação real: fala com a API, guarda a sessão no [SessionStore] e os dados locais na [AppDatabase]. */
class AuthRepositoryImpl(
    private val api: ApiService,
    private val sessao: SessionStore,
    private val baseDados: AppDatabase,
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

    override suspend fun registosPorEnviar(): Int =
        baseDados.visitas().contarPorEnviar() + baseDados.avistamentos().contarPorEnviar()

    /**
     * Apaga primeiro a base de dados e só depois a sessão: a MainActivity reage ao fim da sessão
     * voltando ao login, e nessa altura os dados do utilizador já não existem.
     *
     * Quando a sessão expira (401 da API) só se limpa a sessão, no AppContainer: normalmente é a
     * mesma pessoa que volta a entrar, e assim não perde o que ainda não foi enviado.
     */
    override suspend fun terminarSessao() {
        // clearAllTables bloqueia a thread até terminar, por isso corre numa thread de I/O e não na principal.
        // NonCancellable: se o ecrã fechar a meio, as duas limpezas fazem-se na mesma; nunca fica
        // a base de dados apagada com a sessão ainda iniciada.
        withContext(Dispatchers.IO + NonCancellable) {
            baseDados.clearAllTables()
            // Atenção: as fotografias das visitas (issue #14) vão ficar em ficheiros, fora da base de
            // dados; quando existirem, também têm de ser apagadas aqui.
            sessao.limpar()
        }
    }

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
