package pt.uptomar.dam2026.apisfield.ui.common

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val HORA = DateTimeFormatter.ofPattern("HH:mm", Locale.ROOT)
private val HORA_E_DATA = DateTimeFormatter.ofPattern("HH:mm 'de' dd/MM", Locale.ROOT)

/**
 * Hora de [instante] para mostrar ao utilizador: `10:05` se for de hoje, `10:05 de 24/11` se não for.
 *
 * As datas vêm da API em UTC; o dia e a hora são os do fuso [zona], o do telemóvel.
 */
fun horaOuData(instante: Instant, agora: Instant, zona: ZoneId): String {
    val data = instante.atZone(zona)
    val hoje = data.toLocalDate() == agora.atZone(zona).toLocalDate()
    return data.format(if (hoje) HORA else HORA_E_DATA)
}
