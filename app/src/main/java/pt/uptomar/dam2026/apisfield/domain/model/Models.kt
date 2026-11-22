package pt.uptomar.dam2026.apisfield.domain.model

import java.time.Instant
import java.time.LocalDate

/*
 * Modelos de domínio: os dados como a app os usa.
 *
 * Diferem dos DTOs em três pontos:
 *  - os valores fixos (estado, modelo, tipo de alerta...) são enums em vez de texto, para o
 *    compilador apanhar erros de escrita e o `when` obrigar a tratar todos os casos;
 *  - as datas são Instant / LocalDate em vez de texto;
 *  - não dependem do Retrofit nem do Gson, por isso podem ser usados em testes sem rede.
 */

/** Papel do utilizador: o administrador vê todas as colmeias. */
enum class Role { USER, ADMIN }

data class User(val id: Int, val name: String, val email: String, val role: Role)

/** Modelo de colmeia. Só o [PRO] tem sensor de CO₂. */
enum class HiveModel { STARTER, PRO }

/** Estado calculado pela API a partir das leituras (regras em docs/CONTRATO.md da API). */
enum class HiveStatus { HEALTHY, WARNING, DANGER, OFFLINE }

data class HiveLocation(val latitude: Double, val longitude: Double, val label: String)

/** Últimas leituras. [co2] é nulo no modelo [HiveModel.STARTER]. */
data class HiveSensors(
    val tempInterior: Double,
    val tempExterior: Double,
    val humidity: Double,
    val weight: Double,
    val co2: Double?,
    val battery: Int,
    val signal: Int,
    val waspDetected: Boolean,
)

data class Hive(
    val id: Int,
    val name: String,
    val model: HiveModel,
    val status: HiveStatus,
    val location: HiveLocation,
    val sensors: HiveSensors,
    val lastUpdate: Instant,
    val ownerId: Int,
)

/** Leitura de um dia do histórico. [value] é nulo quando não houve dados nesse dia. */
data class SensorReading(val label: String, val date: LocalDate, val value: Double?)

data class HiveHistory(
    val temperature: List<SensorReading>,
    val weight: List<SensorReading>,
    val co2: List<SensorReading>,
)

enum class AlertType { WASP, TEMPERATURE, WEIGHT, BATTERY, SIGNAL, CO2 }
enum class Severity { WARNING, DANGER }

data class Alert(
    val id: Int,
    val hiveId: Int,
    val hiveName: String,
    val type: AlertType,
    val severity: Severity,
    val message: String,
    val timestamp: Instant,
    val read: Boolean,
)

/** Quantidade de criação observada numa visita. O nome na API está entre parênteses. */
enum class BroodLevel(val apiValue: String) { FRACA("fraca"), MEDIA("media"), FORTE("forte") }

/** Comportamento das abelhas durante a visita. */
enum class Temperament(val apiValue: String) { CALMA("calma"), NERVOSA("nervosa"), AGRESSIVA("agressiva") }

data class Visit(
    val id: Int,
    val hiveId: Int,
    val hiveName: String,
    val date: Instant,
    val queenSeen: Boolean,
    val broodLevel: BroodLevel,
    val temperament: Temperament,
    val treatment: String?,
    val notes: String?,
    val latitude: Double?,
    val longitude: Double?,
    val photoIds: List<Int>,
    val authorId: Int,
)

/** Avistamento de uma vespa ou de um ninho. */
enum class SightingType { WASP, NEST }

data class WaspSighting(
    val id: Int,
    val type: SightingType,
    val latitude: Double,
    val longitude: Double,
    val accuracy: Double?,
    val notes: String?,
    val date: Instant,
    val photoId: Int?,
    val authorId: Int,
    val authorName: String,
    val distanceKm: Double?,
)
