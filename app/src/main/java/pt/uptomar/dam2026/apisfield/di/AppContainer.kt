package pt.uptomar.dam2026.apisfield.di

import android.content.Context
import pt.uptomar.dam2026.apisfield.BuildConfig
import pt.uptomar.dam2026.apisfield.data.local.AppDatabase
import pt.uptomar.dam2026.apisfield.data.remote.ApiService
import pt.uptomar.dam2026.apisfield.data.remote.createApiService

/**
 * Contentor de dependências da app (injeção de dependências manual).
 *
 * Em vez de usar uma framework como o Hilt, as dependências partilhadas são criadas aqui,
 * uma única vez, e os ecrãs vão buscá-las através de `(application as ApisFieldApp).container`.
 * Assim fica explícito quem cria o quê.
 *
 * Referência: https://developer.android.com/training/dependency-injection/manual
 *
 * As próximas funcionalidades acrescentam aqui a sessão do utilizador e os repositórios.
 *
 * @param appContext contexto da aplicação (nunca o de uma Activity, para não a manter em memória).
 */
class AppContainer(private val appContext: Context) {

    /**
     * Cliente da API. O endereço depende do tipo de build (ver app/build.gradle.kts) e os
     * pedidos só são escritos no Logcat em debug.
     *
     * O token ainda é sempre null: a sessão (SessionStore) é criada na issue #5.
     */
    val api: ApiService by lazy {
        createApiService(
            baseUrl = BuildConfig.API_BASE_URL,
            tokenProvider = { null },
            registarPedidos = BuildConfig.DEBUG,
        )
    }

    /**
     * Base de dados local (colmeias em cache, visitas e avistamentos por enviar).
     * Só é aberta na primeira vez que alguém a pede (`by lazy`), e depois é sempre a mesma.
     */
    val baseDados: AppDatabase by lazy { AppDatabase.criar(appContext) }
}
