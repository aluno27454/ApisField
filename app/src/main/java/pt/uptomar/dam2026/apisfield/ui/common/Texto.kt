package pt.uptomar.dam2026.apisfield.ui.common

import android.content.Context
import androidx.annotation.StringRes

/**
 * Texto a mostrar na UI, que pode vir de `strings.xml` ou já pronto (ex.: uma mensagem da API).
 *
 * Os ViewModels não guardam o Context (perderiam-no ao rodar o ecrã), por isso não resolvem as
 * strings: guardam o id ou o texto, e é a Activity que o converte com [resolver].
 */
sealed interface Texto {
    data class Recurso(@StringRes val id: Int) : Texto
    data class Literal(val valor: String) : Texto
}

fun Texto.resolver(context: Context): String = when (this) {
    is Texto.Recurso -> context.getString(id)
    is Texto.Literal -> valor
}
