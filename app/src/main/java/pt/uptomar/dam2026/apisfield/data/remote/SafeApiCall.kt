package pt.uptomar.dam2026.apisfield.data.remote

import com.google.gson.Gson
import com.google.gson.JsonParseException
import kotlinx.coroutines.CancellationException
import retrofit2.HttpException
import java.io.IOException
import java.time.format.DateTimeParseException

/**
 * Resultado de uma chamada à API: [Sucesso] com os dados ou [Falha] com o erro.
 *
 * Assim nenhum erro chega à UI como exceção: quem chama é obrigado a tratar os dois casos.
 */
sealed class ApiResult<out T> {
    data class Sucesso<T>(val dados: T) : ApiResult<T>()
    data class Falha(val erro: ApiError) : ApiResult<Nothing>()
}

/**
 * Executa [chamada] e converte o resultado ou a exceção num [ApiResult].
 *
 * Exemplo: `safeApiCall { api.getHives().map { it.toDomain() } }`
 *
 * A conversão para modelos de domínio deve ser feita dentro de [chamada], para que um valor
 * desconhecido (ex.: um estado novo na API) também resulte em [ApiError.RespostaInvalida].
 */
suspend fun <T> safeApiCall(chamada: suspend () -> T): ApiResult<T> = try {
    ApiResult.Sucesso(chamada())
} catch (e: CancellationException) {
    // Uma coroutine cancelada (ex.: o utilizador saiu do ecrã) não é um erro: deixa-se seguir.
    throw e
} catch (e: HttpException) {
    ApiResult.Falha(erroHttp(e.code(), e.response()?.errorBody()?.string()))
} catch (e: IOException) {
    // Inclui sem rede, servidor desligado e tempo esgotado (SocketTimeoutException).
    ApiResult.Falha(ApiError.SemLigacao)
} catch (e: JsonParseException) {
    ApiResult.Falha(ApiError.RespostaInvalida)
} catch (e: DateTimeParseException) {
    ApiResult.Falha(ApiError.RespostaInvalida)
} catch (e: IllegalArgumentException) {
    // Valor de enum desconhecido ao converter para o domínio.
    ApiResult.Falha(ApiError.RespostaInvalida)
} catch (e: NullPointerException) {
    // O Gson não respeita a nulidade do Kotlin: se a API omitir um campo obrigatório, ele fica
    // a null e o erro só aparece ao convertê-lo para o domínio.
    ApiResult.Falha(ApiError.RespostaInvalida)
}

/** Corpo de erro 400 do ASP.NET (`ValidationProblemDetails`); só interessa o mapa `errors`. */
private class ValidationProblem(val errors: Map<String, List<String>>?)

/** Classifica um código HTTP de erro. [corpo] só é lido nos 400, para extrair os erros por campo. */
internal fun erroHttp(codigo: Int, corpo: String?): ApiError = when (codigo) {
    400 -> ApiError.Validacao(errosDeValidacao(corpo))
    401 -> ApiError.NaoAutenticado
    403 -> ApiError.SemPermissao
    404 -> ApiError.NaoEncontrado
    409 -> ApiError.Conflito
    else -> ApiError.Servidor(codigo)
}

/**
 * Extrai os erros por campo do corpo de um 400.
 *
 * O ASP.NET pode enviar o nome do campo com maiúscula inicial (`"Email"`); aqui passa-se
 * sempre para camelCase (`"email"`), igual aos nomes do contrato. Se o corpo não tiver o
 * formato esperado, devolve um mapa vazio: o formulário mostra então a mensagem geral.
 */
private fun errosDeValidacao(corpo: String?): Map<String, List<String>> {
    if (corpo.isNullOrBlank()) return emptyMap()
    val problema = try {
        Gson().fromJson(corpo, ValidationProblem::class.java)
    } catch (e: JsonParseException) {
        return emptyMap()
    }
    return problema?.errors.orEmpty().mapKeys { (campo, _) -> campo.replaceFirstChar { it.lowercase() } }
}
