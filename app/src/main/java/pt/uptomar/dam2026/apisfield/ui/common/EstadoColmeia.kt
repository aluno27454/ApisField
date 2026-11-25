package pt.uptomar.dam2026.apisfield.ui.common

import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import pt.uptomar.dam2026.apisfield.R
import pt.uptomar.dam2026.apisfield.domain.model.HiveStatus

/*
 * Texto e cores de cada estado de uma colmeia, num só sítio para todos os ecrãs mostrarem o mesmo.
 */

@StringRes
fun HiveStatus.texto(): Int = when (this) {
    HiveStatus.HEALTHY -> R.string.estado_saudavel
    HiveStatus.WARNING -> R.string.estado_atencao
    HiveStatus.DANGER -> R.string.estado_perigo
    HiveStatus.OFFLINE -> R.string.estado_offline
}

/** Cor forte, para pontos e faixas. Não se usa em texto: o amarelo sobre branco lê-se mal. */
@ColorRes
fun HiveStatus.cor(): Int = when (this) {
    HiveStatus.HEALTHY -> R.color.estado_saudavel
    HiveStatus.WARNING -> R.color.estado_atencao
    HiveStatus.DANGER -> R.color.estado_perigo
    HiveStatus.OFFLINE -> R.color.estado_offline
}

/** Cor clara, para o fundo por trás do texto do estado. */
@ColorRes
fun HiveStatus.corFundo(): Int = when (this) {
    HiveStatus.HEALTHY -> R.color.floresta_claro
    HiveStatus.WARNING -> R.color.fundo_atencao
    HiveStatus.DANGER -> R.color.fundo_perigo
    HiveStatus.OFFLINE -> R.color.fundo_offline
}
