package pt.uptomar.dam2026.apisfield.domain

import pt.uptomar.dam2026.apisfield.domain.model.Hive
import pt.uptomar.dam2026.apisfield.domain.model.HiveStatus

/** Estado geral do apiário, da situação mais calma para a mais grave. */
sealed interface EstadoApiario {
    data object Tranquilo : EstadoApiario

    /** Há [colmeias] a precisar de atenção e nenhuma em perigo. */
    data class Atencao(val colmeias: Int) : EstadoApiario

    /** Há [colmeias] em perigo. */
    data class Perigo(val colmeias: Int) : EstadoApiario
}

/** Contagens mostradas no topo do painel. */
data class DashboardSummary(
    val total: Int,
    val saudaveis: Int,
    val atencao: Int,
    val perigo: Int,
    val offline: Int,
) {
    /**
     * Frase principal do painel. O perigo tem prioridade sobre a atenção.
     *
     * Uma colmeia sem comunicação conta como a precisar de atenção: sem leituras não se sabe
     * como está, por isso não se pode dizer que está tudo tranquilo.
     */
    val estado: EstadoApiario
        get() = when {
            perigo > 0 -> EstadoApiario.Perigo(perigo)
            atencao + offline > 0 -> EstadoApiario.Atencao(atencao + offline)
            else -> EstadoApiario.Tranquilo
        }

    companion object {
        /** Conta as [colmeias] por estado. */
        fun de(colmeias: List<Hive>) = DashboardSummary(
            total = colmeias.size,
            saudaveis = colmeias.count { it.status == HiveStatus.HEALTHY },
            atencao = colmeias.count { it.status == HiveStatus.WARNING },
            perigo = colmeias.count { it.status == HiveStatus.DANGER },
            offline = colmeias.count { it.status == HiveStatus.OFFLINE },
        )
    }
}
