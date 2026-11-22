package pt.uptomar.dam2026.apisfield.data.remote

import okhttp3.Interceptor
import okhttp3.Response

/**
 * Acrescenta o cabeçalho `Authorization: Bearer <token>` a todos os pedidos.
 *
 * Um interceptor do OkHttp é chamado antes de cada pedido sair, por isso o token não tem de
 * ser passado a cada função do [ApiService]. Se não houver sessão (token nulo), o pedido segue
 * sem cabeçalho: é o caso do login, do registo e do `health`, que não precisam dele.
 *
 * Se a API responder 401 a um pedido que levava token, a sessão já não é válida (expirou ou foi
 * revogada): chama-se [aoSessaoExpirar], que termina a sessão e leva a app de volta ao login.
 * Assim nenhum ecrã tem de tratar o 401 por si.
 *
 * @param tokenProvider devolve o token atual. É uma função, e não um valor fixo, porque o token
 * muda quando o utilizador inicia ou termina sessão.
 * @param aoSessaoExpirar chamada quando um pedido com token recebe 401.
 *
 * Referência: https://square.github.io/okhttp/features/interceptors/
 */
class AuthInterceptor(
    private val tokenProvider: () -> String?,
    private val aoSessaoExpirar: () -> Unit = {},
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val token = tokenProvider()
        val pedido = if (token.isNullOrBlank()) {
            chain.request()
        } else {
            chain.request().newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        }
        val resposta = chain.proceed(pedido)
        // Um 401 sem token (ex.: login com a palavra-passe errada) não significa sessão expirada.
        if (resposta.code == 401 && !token.isNullOrBlank()) aoSessaoExpirar()
        return resposta
    }
}
