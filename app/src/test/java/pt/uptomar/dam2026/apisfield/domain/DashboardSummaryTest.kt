package pt.uptomar.dam2026.apisfield.domain

import org.junit.Assert.assertEquals
import org.junit.Test
import pt.uptomar.dam2026.apisfield.colmeia
import pt.uptomar.dam2026.apisfield.domain.model.HiveStatus
import pt.uptomar.dam2026.apisfield.domain.model.HiveStatus.DANGER
import pt.uptomar.dam2026.apisfield.domain.model.HiveStatus.HEALTHY
import pt.uptomar.dam2026.apisfield.domain.model.HiveStatus.OFFLINE
import pt.uptomar.dam2026.apisfield.domain.model.HiveStatus.WARNING

/** Testes do cálculo do resumo do painel. */
class DashboardSummaryTest {

    private fun resumo(vararg estados: HiveStatus) =
        DashboardSummary.de(estados.mapIndexed { i, estado -> colmeia(id = i, estado = estado) })

    @Test
    fun contaAsColmeiasPorEstado() {
        val r = resumo(HEALTHY, WARNING, HEALTHY, DANGER, OFFLINE, HEALTHY)

        assertEquals(DashboardSummary(total = 6, saudaveis = 3, atencao = 1, perigo = 1, offline = 1), r)
    }

    @Test
    fun todasSaudaveisEstaTudoTranquilo() {
        assertEquals(EstadoApiario.Tranquilo, resumo(HEALTHY, HEALTHY).estado)
    }

    @Test
    fun semColmeiasEstaTudoTranquilo() {
        assertEquals(EstadoApiario.Tranquilo, resumo().estado)
    }

    @Test
    fun comColmeiasEmAtencaoDizQuantas() {
        assertEquals(EstadoApiario.Atencao(2), resumo(WARNING, HEALTHY, WARNING).estado)
    }

    @Test
    fun umaColmeiaSemComunicacaoTambemPrecisaDeAtencao() {
        // Sem leituras não se sabe como está a colmeia, por isso não se pode dizer que está tudo tranquilo.
        assertEquals(EstadoApiario.Atencao(1), resumo(OFFLINE, HEALTHY).estado)
        assertEquals(EstadoApiario.Atencao(2), resumo(OFFLINE, WARNING).estado)
    }

    @Test
    fun oPerigoTemPrioridadeSobreAAtencao() {
        assertEquals(EstadoApiario.Perigo(1), resumo(WARNING, WARNING, OFFLINE, DANGER).estado)
    }
}
