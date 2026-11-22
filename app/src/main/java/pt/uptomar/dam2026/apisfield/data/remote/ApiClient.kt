package pt.uptomar.dam2026.apisfield.data.remote

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/** Tempo máximo de espera por ligação, leitura e escrita. Acima disto o pedido falha com SemLigacao. */
private const val TIMEOUT_SEGUNDOS = 15L

/**
 * Cria o [ApiService].
 *
 * @param baseUrl endereço da API, terminado em `/` (vem de `BuildConfig.API_BASE_URL`).
 * @param tokenProvider devolve o token da sessão atual, ou null se não houver sessão.
 * @param registarPedidos se true, escreve no Logcat cada pedido e resposta. Só deve ser true
 * em debug: em release o Logcat ficaria com o token e os dados do utilizador.
 */
fun createApiService(
    baseUrl: String,
    tokenProvider: () -> String?,
    registarPedidos: Boolean,
): ApiService {
    val cliente = OkHttpClient.Builder()
        .connectTimeout(TIMEOUT_SEGUNDOS, TimeUnit.SECONDS)
        .readTimeout(TIMEOUT_SEGUNDOS, TimeUnit.SECONDS)
        .writeTimeout(TIMEOUT_SEGUNDOS, TimeUnit.SECONDS)
        .addInterceptor(AuthInterceptor(tokenProvider))
        .apply {
            if (registarPedidos) {
                addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
            }
        }
        .build()

    return Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(cliente)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(ApiService::class.java)
}

/** Corpo de pedido para enviar uma fotografia JPEG à API. */
fun ByteArray.jpegBody(): RequestBody = toRequestBody("image/jpeg".toMediaType())
