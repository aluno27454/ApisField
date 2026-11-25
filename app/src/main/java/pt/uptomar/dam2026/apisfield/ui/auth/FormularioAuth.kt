package pt.uptomar.dam2026.apisfield.ui.auth

import pt.uptomar.dam2026.apisfield.R
import pt.uptomar.dam2026.apisfield.data.remote.ApiError
import pt.uptomar.dam2026.apisfield.domain.ErroCampo
import pt.uptomar.dam2026.apisfield.ui.common.Texto

/** Campos dos formulários de login e registo. */
enum class Campo { NOME, EMAIL, PASSWORD, CONFIRMACAO }

/**
 * Estado de um formulário de autenticação, observado pela Activity.
 *
 * @property erros mensagem de erro de cada campo (os campos sem erro não aparecem no mapa).
 * @property aEnviar true enquanto o pedido está a decorrer: o botão fica desativado e aparece o progresso.
 * @property mensagem erro geral, que não pertence a nenhum campo (ex.: sem ligação).
 * @property concluido true quando a sessão foi iniciada: a Activity abre o ecrã principal.
 */
data class FormularioUiState(
    val erros: Map<Campo, Texto> = emptyMap(),
    val aEnviar: Boolean = false,
    val mensagem: Texto? = null,
    val concluido: Boolean = false,
)

/** Mensagem de `strings.xml` para cada motivo de recusa de um campo. */
fun ErroCampo.texto(): Texto = Texto.Recurso(
    when (this) {
        ErroCampo.OBRIGATORIO -> R.string.campo_obrigatorio
        ErroCampo.EMAIL_INVALIDO -> R.string.campo_email_invalido
        ErroCampo.PASSWORD_FRACA -> R.string.campo_password_fraca
        ErroCampo.NOME_TAMANHO -> R.string.campo_nome_tamanho
        ErroCampo.CONFIRMACAO_DIFERENTE -> R.string.campo_confirmacao_diferente
    }
)

/**
 * Converte os erros por campo de um 400 da API nos campos do formulário.
 * A API usa os nomes do contrato (`name`, `email`, `password`); mostra-se a primeira mensagem de cada um.
 */
fun ApiError.Validacao.porCampo(): Map<Campo, Texto> = buildMap {
    val campos = mapOf("name" to Campo.NOME, "email" to Campo.EMAIL, "password" to Campo.PASSWORD)
    for ((nomeApi, mensagens) in erros) {
        val campo = campos[nomeApi] ?: continue
        mensagens.firstOrNull()?.let { put(campo, Texto.Literal(it)) }
    }
}
