package pt.uptomar.dam2026.apisfield.data.remote.dto

/** Localização de uma colmeia: coordenadas e um nome do sítio (ex.: "Apiário da Serra"). */
data class HiveLocationDto(val latitude: Double, val longitude: Double, val label: String)

/**
 * Últimas leituras dos sensores de uma colmeia.
 *
 * O `co2` é nulo nas colmeias do modelo `starter`, que não têm esse sensor; por isso é o único
 * campo anulável.
 */
data class HiveSensorsDto(
    val tempInterior: Double,
    val tempExterior: Double,
    val humidity: Double,
    val weight: Double,
    val co2: Double?,
    val battery: Int,
    val signal: Int,
    val waspDetected: Boolean,
)

/**
 * Colmeia, tal como vem da API.
 *
 * `model` é `"starter"` ou `"pro"`; `status` é `"healthy"`, `"warning"`, `"danger"` ou `"offline"`.
 * `lastUpdate` vem em ISO 8601 UTC (ex.: `2026-11-13T10:57:00Z`).
 */
data class HiveDto(
    val id: Int,
    val name: String,
    val model: String,
    val status: String,
    val location: HiveLocationDto,
    val sensors: HiveSensorsDto,
    val lastUpdate: String,
    val ownerId: Int,
)

/** Uma leitura diária do histórico. `value` 0 significa que não há dados nesse dia. */
data class SensorReadingDto(val label: String, val date: String, val value: Double)

/** Histórico dos últimos 7 dias. `co2` vem vazio no modelo `starter`. */
data class HiveHistoryDto(
    val temperature: List<SensorReadingDto>,
    val weight: List<SensorReadingDto>,
    val co2: List<SensorReadingDto>,
)

/** Corpo de `POST /api/hives` e `PUT /api/hives/{id}`. */
data class HiveRequest(
    val name: String,
    val model: String,
    val latitude: Double,
    val longitude: Double,
    val label: String,
)
