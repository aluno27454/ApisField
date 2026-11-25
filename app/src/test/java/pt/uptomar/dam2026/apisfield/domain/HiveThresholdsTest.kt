package pt.uptomar.dam2026.apisfield.domain

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Testes dos limiares dos sensores, sobretudo do valor exato de cada limite, que ainda é normal. */
class HiveThresholdsTest {

    @Test
    fun temperaturaInteriorAcimaDe37() {
        assertFalse(HiveThresholds.temperaturaAlta(37.0))
        assertTrue(HiveThresholds.temperaturaAlta(37.1))
    }

    @Test
    fun co2AcimaDe3500() {
        assertFalse(HiveThresholds.co2Alto(3500.0))
        assertTrue(HiveThresholds.co2Alto(3500.1))
    }

    @Test
    fun bateriaAbaixoDe20() {
        assertFalse(HiveThresholds.bateriaFraca(20))
        assertTrue(HiveThresholds.bateriaFraca(19))
    }

    @Test
    fun sinalAbaixoDe30() {
        assertFalse(HiveThresholds.sinalFraco(30))
        assertTrue(HiveThresholds.sinalFraco(29))
    }
}
