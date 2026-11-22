package pt.uptomar.dam2026.apisfield.data.remote.dto

/**
 * Alerta gerado pela API quando uma colmeia ultrapassa um limiar.
 *
 * `type`: `wasp`, `temperature`, `weight`, `battery`, `signal` ou `co2`.
 * `severity`: `warning` ou `danger`. A `message` já vem escrita em português.
 */
data class AlertDto(
    val id: Int,
    val hiveId: Int,
    val hiveName: String,
    val type: String,
    val severity: String,
    val message: String,
    val timestamp: String,
    val read: Boolean,
)

/**
 * Visita a uma colmeia.
 *
 * `broodLevel`: `fraca`, `media` ou `forte`. `temperament`: `calma`, `nervosa` ou `agressiva`.
 * Os campos com `?` são opcionais no contrato e podem vir a `null`.
 */
data class VisitDto(
    val id: Int,
    val hiveId: Int,
    val hiveName: String,
    val date: String,
    val queenSeen: Boolean,
    val broodLevel: String,
    val temperament: String,
    val treatment: String?,
    val notes: String?,
    val latitude: Double?,
    val longitude: Double?,
    val photoIds: List<Int>,
    val authorId: Int,
)

/** Corpo de `POST /api/hives/{hiveId}/visits` e `PUT /api/visits/{id}`. */
data class VisitRequest(
    val date: String,
    val queenSeen: Boolean,
    val broodLevel: String,
    val temperament: String,
    val treatment: String?,
    val notes: String?,
    val latitude: Double?,
    val longitude: Double?,
)

/**
 * Avistamento de vespa asiática (`type` = `wasp`) ou de ninho (`type` = `nest`).
 * `distanceKm` só vem preenchido quando o pedido inclui a posição do utilizador.
 */
data class WaspSightingDto(
    val id: Int,
    val type: String,
    val latitude: Double,
    val longitude: Double,
    val accuracy: Double?,
    val notes: String?,
    val date: String,
    val photoId: Int?,
    val authorId: Int,
    val authorName: String,
    val distanceKm: Double?,
)

/** Corpo de `POST /api/sightings` e `PUT /api/sightings/{id}`. */
data class SightingRequest(
    val type: String,
    val latitude: Double,
    val longitude: Double,
    val accuracy: Double?,
    val notes: String?,
    val date: String,
)

/** Resposta ao carregar uma fotografia: o id com que fica guardada na API. */
data class PhotoIdDto(val id: Int)

/** Resposta de `GET /api/health`. */
data class HealthDto(val status: String)
