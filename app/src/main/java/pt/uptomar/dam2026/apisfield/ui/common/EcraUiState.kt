package pt.uptomar.dam2026.apisfield.ui.common

import androidx.annotation.StringRes

/**
 * Estado de um ecrã que carrega dados e os pode atualizar (painel, detalhe da colmeia...).
 *
 * @property conteudo o que ocupa o ecrã.
 * @property aAtualizar há um pedido a decorrer.
 * @property aviso mensagem a mostrar uma vez, quando a atualização falha mas há dados antigos no ecrã.
 */
data class EcraUiState<T>(
    val conteudo: UiState<T> = UiState.Loading,
    val aAtualizar: Boolean = false,
    @param:StringRes val aviso: Int? = null,
) {
    /** Começou um pedido. Os dados que já estiverem no ecrã continuam visíveis. */
    fun aCarregar() = EcraUiState<T>(
        conteudo = if (conteudo is UiState.Success) conteudo else UiState.Loading,
        aAtualizar = true,
    )

    /** O pedido correu bem. */
    fun comConteudo(novo: UiState<T>) = EcraUiState<T>(conteudo = novo)

    /** O pedido falhou. Com dados no ecrã, o erro é só um aviso; sem eles, ocupa o ecrã. */
    fun comFalha(@StringRes mensagem: Int): EcraUiState<T> =
        if (conteudo is UiState.Success) EcraUiState(conteudo = conteudo, aviso = mensagem)
        else EcraUiState(conteudo = UiState.Error(mensagem))
}
