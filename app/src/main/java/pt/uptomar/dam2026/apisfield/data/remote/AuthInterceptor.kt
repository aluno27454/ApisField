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
 * @param tokenProvider devolve o token atual. É uma função, e não um valor fixo, porque o token
 * muda quando o utilizador inicia ou termina sessão.
 *
 * Referência: https://square.github.io/okhttp/features/interceptors/
 */
class AuthInterceptor(private val tokenProvider: () -> String?) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val token = tokenProvider()
        val pedido = if (token.isNullOrBlank()) {
            chain.request()
        } else {
            chain.request().newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        }
        return chain.proceed(pedido)
    }
}
