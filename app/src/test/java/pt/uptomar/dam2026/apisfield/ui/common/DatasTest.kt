package pt.uptomar.dam2026.apisfield.ui.common

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant
import java.time.ZoneId

/** Testes do texto da hora de uma leitura. Lisboa está em UTC no inverno e em UTC+1 no verão. */
class DatasTest {

    private val lisboa = ZoneId.of("Europe/Lisbon")

    private fun hora(data: String, agora: String) = horaOuData(Instant.parse(data), Instant.parse(agora), lisboa)

    @Test
    fun noProprioDiaMostraSoAHora() {
        assertEquals("10:05", hora("2026-11-25T10:05:40Z", "2026-11-25T18:00:00Z"))
    }

    @Test
    fun noutroDiaMostraTambemAData() {
        assertEquals("23:50 de 24/11", hora("2026-11-24T23:50:00Z", "2026-11-25T00:10:00Z"))
    }

    @Test
    fun usaAHoraLocalENaoAUtc() {
        assertEquals("11:05", hora("2026-07-10T10:05:00Z", "2026-07-10T18:00:00Z"))
        // 23:30 UTC já é dia 11 em Lisboa, o mesmo dia de "agora".
        assertEquals("00:30", hora("2026-07-10T23:30:00Z", "2026-07-11T08:00:00Z"))
    }
}
