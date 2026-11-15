package pt.uptomar.dam2026.apisfield.di

import android.content.Context

/**
 * Contentor de dependências da app (injeção de dependências manual).
 *
 * Em vez de usar uma framework como o Hilt, as dependências partilhadas são criadas aqui,
 * uma única vez, e os ecrãs vão buscá-las através de `(application as ApisFieldApp).container`.
 * Assim fica explícito quem cria o quê.
 *
 * Referência: https://developer.android.com/training/dependency-injection/manual
 *
 * Por agora está vazio; as próximas funcionalidades acrescentam aqui o cliente da API,
 * a base de dados local e a sessão do utilizador.
 *
 * @param appContext contexto da aplicação (nunca o de uma Activity, para não a manter em memória).
 */
class AppContainer(private val appContext: Context)
