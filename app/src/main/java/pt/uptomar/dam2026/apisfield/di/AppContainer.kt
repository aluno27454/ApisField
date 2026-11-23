package pt.uptomar.dam2026.apisfield.di

import android.content.Context
import pt.uptomar.dam2026.apisfield.BuildConfig
import pt.uptomar.dam2026.apisfield.data.local.SessionStore
import pt.uptomar.dam2026.apisfield.data.remote.ApiService
import pt.uptomar.dam2026.apisfield.data.remote.createApiService
import pt.uptomar.dam2026.apisfield.data.repository.AuthRepository
import pt.uptomar.dam2026.apisfield.data.repository.AuthRepositoryImpl

/**
 * Contentor de dependências da app (injeção de dependências manual).
 *
 * Em vez de usar uma framework como o Hilt, as dependências partilhadas são criadas aqui,
 * uma única vez, e os ecrãs vão buscá-las através de `(application as ApisFieldApp).container`.
 * Assim fica explícito quem cria o quê.
 *
 * Referência: https://developer.android.com/training/dependency-injection/manual
 *
 * As próximas funcionalidades acrescentam aqui a base de dados local e os outros repositórios.
 *
 * @param appContext contexto da aplicação (nunca o de uma Activity, para não a manter em memória).
 */
class AppContainer(private val appContext: Context) {

    /** Sessão do utilizador (token e dados), guardada no dispositivo. */
    val sessao: SessionStore by lazy { SessionStore(appContext) }

    /**
     * Cliente da API. O endereço depende do tipo de build (ver app/build.gradle.kts) e os
     * pedidos só são escritos no Logcat em debug. Cada pedido leva o token da [sessao]; se a API
     * responder 401, a sessão termina e a MainActivity volta ao login.
     */
    val api: ApiService by lazy {
        createApiService(
            baseUrl = BuildConfig.API_BASE_URL,
            tokenProvider = { sessao.token },
            registarPedidos = BuildConfig.DEBUG,
            aoSessaoExpirar = { sessao.limpar() },
        )
    }

    val authRepository: AuthRepository by lazy { AuthRepositoryImpl(api, sessao) }
}
