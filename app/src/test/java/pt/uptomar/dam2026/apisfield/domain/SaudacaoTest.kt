package pt.uptomar.dam2026.apisfield.domain

import org.junit.Assert.assertEquals
import org.junit.Test

/** Testes da saudação, sobretudo das horas em que muda. */
class SaudacaoTest {

    @Test
    fun manhaDasSeisAsTreze() {
        assertEquals(Saudacao.BOM_DIA, Saudacao.daHora(6))
        assertEquals(Saudacao.BOM_DIA, Saudacao.daHora(12))
    }

    @Test
    fun tardeDasTrezeAsVinte() {
        assertEquals(Saudacao.BOA_TARDE, Saudacao.daHora(13))
        assertEquals(Saudacao.BOA_TARDE, Saudacao.daHora(19))
    }

    @Test
    fun noiteDasVinteAsSeis() {
        assertEquals(Saudacao.BOA_NOITE, Saudacao.daHora(20))
        assertEquals(Saudacao.BOA_NOITE, Saudacao.daHora(0))
        assertEquals(Saudacao.BOA_NOITE, Saudacao.daHora(5))
    }

    @Test
    fun oPrimeiroNomeEOQueVemAntesDoEspaco() {
        assertEquals("André", primeiroNome("André Vassalo"))
        assertEquals("Maria", primeiroNome("  Maria  "))
        assertEquals("", primeiroNome(""))
    }
}
