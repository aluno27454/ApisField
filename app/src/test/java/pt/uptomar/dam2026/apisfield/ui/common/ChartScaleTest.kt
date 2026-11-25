package pt.uptomar.dam2026.apisfield.ui.common

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import pt.uptomar.dam2026.apisfield.data.remote.dto.HiveHistoryDto
import pt.uptomar.dam2026.apisfield.data.remote.dto.SensorReadingDto
import pt.uptomar.dam2026.apisfield.data.remote.toDomain

/** Testes da escala do gráfico: conversão de valores e de dias em posições entre 0 e 1. */
class ChartScaleTest {

    private val tolerancia = 0.0001f

    @Test
    fun oMinimoFicaPertoDoFundoEOMaximoPertoDoTopo() {
        val escala = ChartScale.de(listOf(30.0, 35.0, 40.0))!!

        // Margem de 10% da amplitude (1 grau) de cada lado, para os pontos não tocarem nas bordas.
        assertEquals(29.0, escala.min, 0.0001)
        assertEquals(41.0, escala.max, 0.0001)
        assertEquals(1f / 12, escala.fracao(30.0), tolerancia)
        assertEquals(0.5f, escala.fracao(35.0), tolerancia)
        assertEquals(11f / 12, escala.fracao(40.0), tolerancia)
    }

    @Test
    fun comValoresTodosIguaisALinhaFicaAMeio() {
        val escala = ChartScale.de(listOf(38.4, 38.4, 38.4))!!

        assertEquals(0.5f, escala.fracao(38.4), tolerancia) // sem dividir por zero
        assertTrue(escala.max > escala.min)
    }

    @Test
    fun osDiasSemDadosNaoContamParaAEscala() {
        assertEquals(ChartScale.de(listOf(30.0, 40.0)), ChartScale.de(listOf(null, 30.0, null, 40.0)))
    }

    @Test
    fun semNenhumValorNaoHaEscala() {
        assertNull(ChartScale.de(emptyList()))
        assertNull(ChartScale.de(listOf(null, null)))
    }

    @Test
    fun seAApiEnviarTudoAZeroNaoHaEscala() {
        // No contrato, 0 quer dizer "sem dados"; o mapeador converte-o em null.
        val zeros = List(7) { SensorReadingDto("Seg", "2026-11-23", 0.0) }
        val historico = HiveHistoryDto(zeros, zeros, emptyList()).toDomain()

        assertNull(ChartScale.de(historico.temperature.map { it.value }))
    }

    @Test
    fun umZeroNoMeioNaoPuxaAEscalaParaBaixo() {
        val dias = listOf(34.0, 0.0, 36.0).map { SensorReadingDto("Seg", "2026-11-23", it) }
        val escala = ChartScale.de(HiveHistoryDto(dias, dias, dias).toDomain().weight.map { it.value })

        assertNotNull(escala)
        assertTrue(escala!!.min > 33)
    }

    @Test
    fun osDiasFicamEspalhadosPorIgual() {
        assertEquals(0f, ChartScale.posicaoDia(0, 7), tolerancia)
        assertEquals(0.5f, ChartScale.posicaoDia(3, 7), tolerancia)
        assertEquals(1f, ChartScale.posicaoDia(6, 7), tolerancia)
    }

    @Test
    fun umSoDiaFicaAMeio() {
        assertEquals(0.5f, ChartScale.posicaoDia(0, 1), tolerancia)
    }
}
