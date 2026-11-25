package pt.uptomar.dam2026.apisfield.ui.common

import androidx.annotation.StringRes

/**
 * Estado de um ecrã que carrega dados (ver "Estado do ecrã" em docs/ARQUITETURA.md).
 *
 * É uma sealed interface: o `when` de cada ecrã é obrigado a tratar os quatro casos.
 */
sealed interface UiState<out T> {
    /** A carregar, ainda sem nada para mostrar. */
    data object Loading : UiState<Nothing>

    data class Success<T>(val data: T) : UiState<T>

    /** Carregou bem, mas não há nada para mostrar. */
    data object Empty : UiState<Nothing>

    /** O carregamento falhou e não há dados anteriores para mostrar. */
    data class Error(@param:StringRes val message: Int) : UiState<Nothing>
}
