package pt.uptomar.dam2026.apisfield.data.remote

import androidx.annotation.StringRes
import pt.uptomar.dam2026.apisfield.R

/**
 * Erros que uma chamada à API pode devolver, já classificados.
 *
 * É uma sealed class: o compilador conhece todos os casos, por isso um `when (erro)` sem
 * `else` obriga a tratar cada um. Os repositórios decidem o que fazer (ver tabela em
 * docs/ARQUITETURA.md) e a UI mostra a mensagem de [mensagem].
 */
sealed class ApiError {
    /** Sem rede, servidor inacessível ou tempo esgotado. */
    data object SemLigacao : ApiError()

    /** 401: sem sessão ou token expirado. A app deve voltar ao login. */
    data object NaoAutenticado : ApiError()

    /** 403: autenticado, mas sem permissão (ex.: editar o avistamento de outra pessoa). */
    data object SemPermissao : ApiError()

    /** 404: não existe, ou pertence a outro utilizador. */
    data object NaoEncontrado : ApiError()

    /** 409: conflito, por exemplo um email que já está registado. */
    data object Conflito : ApiError()

    /**
     * 400: a API rejeitou os dados.
     * @property erros mensagens por campo, com o nome do campo em camelCase (ex.: `"email"`),
     * para os formulários as mostrarem junto do campo certo.
     */
    data class Validacao(val erros: Map<String, List<String>>) : ApiError()

    /** 5xx ou outro código inesperado. */
    data class Servidor(val codigo: Int) : ApiError()

    /** A API respondeu com sucesso, mas o conteúdo não tem a forma esperada. */
    data object RespostaInvalida : ApiError()
}

/** Mensagem a mostrar ao utilizador para este erro. */
@StringRes
fun ApiError.mensagem(): Int = when (this) {
    ApiError.SemLigacao -> R.string.erro_sem_ligacao
    ApiError.NaoAutenticado -> R.string.erro_nao_autenticado
    ApiError.SemPermissao -> R.string.erro_sem_permissao
    ApiError.NaoEncontrado -> R.string.erro_nao_encontrado
    ApiError.Conflito -> R.string.erro_conflito
    is ApiError.Validacao -> R.string.erro_validacao
    is ApiError.Servidor -> R.string.erro_servidor
    ApiError.RespostaInvalida -> R.string.erro_resposta_invalida
}
