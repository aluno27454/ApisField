package pt.uptomar.dam2026.apisfield.data.remote

import pt.uptomar.dam2026.apisfield.data.remote.dto.AlertDto
import pt.uptomar.dam2026.apisfield.data.remote.dto.HiveDto
import pt.uptomar.dam2026.apisfield.data.remote.dto.HiveHistoryDto
import pt.uptomar.dam2026.apisfield.data.remote.dto.SensorReadingDto
import pt.uptomar.dam2026.apisfield.data.remote.dto.UserDto
import pt.uptomar.dam2026.apisfield.data.remote.dto.VisitDto
import pt.uptomar.dam2026.apisfield.data.remote.dto.WaspSightingDto
import pt.uptomar.dam2026.apisfield.domain.model.Alert
import pt.uptomar.dam2026.apisfield.domain.model.AlertType
import pt.uptomar.dam2026.apisfield.domain.model.BroodLevel
import pt.uptomar.dam2026.apisfield.domain.model.Hive
import pt.uptomar.dam2026.apisfield.domain.model.HiveHistory
import pt.uptomar.dam2026.apisfield.domain.model.HiveLocation
import pt.uptomar.dam2026.apisfield.domain.model.HiveModel
import pt.uptomar.dam2026.apisfield.domain.model.HiveSensors
import pt.uptomar.dam2026.apisfield.domain.model.HiveStatus
import pt.uptomar.dam2026.apisfield.domain.model.Role
import pt.uptomar.dam2026.apisfield.domain.model.SensorReading
import pt.uptomar.dam2026.apisfield.domain.model.Severity
import pt.uptomar.dam2026.apisfield.domain.model.SightingType
import pt.uptomar.dam2026.apisfield.domain.model.Temperament
import pt.uptomar.dam2026.apisfield.domain.model.User
import pt.uptomar.dam2026.apisfield.domain.model.Visit
import pt.uptomar.dam2026.apisfield.domain.model.WaspSighting
import java.time.Instant
import java.time.LocalDate

/*
 * Conversão dos DTOs da API para os modelos de domínio.
 *
 * Os textos fixos da API ("healthy", "pro"...) são convertidos em enums. Se a API enviar um
 * valor que a app não conhece, `enumValueOf` lança IllegalArgumentException; o safeApiCall
 * apanha-a e transforma-a em ApiError.RespostaInvalida, em vez de a app fechar.
 */

/** Converte `"healthy"` em `HEALTHY`, `"co2"` em `CO2`, etc. */
private inline fun <reified T : Enum<T>> String.toEnum(): T = enumValueOf(uppercase())

fun UserDto.toDomain() = User(id, name, email, role.toEnum<Role>())

fun HiveDto.toDomain() = Hive(
    id = id,
    name = name,
    model = model.toEnum<HiveModel>(),
    status = status.toEnum<HiveStatus>(),
    location = HiveLocation(location.latitude, location.longitude, location.label),
    sensors = with(sensors) {
        HiveSensors(tempInterior, tempExterior, humidity, weight, co2, battery, signal, waspDetected)
    },
    lastUpdate = Instant.parse(lastUpdate),
    ownerId = ownerId,
)

// No histórico, a API usa 0 para "sem dados". Na app fica null, para o gráfico não desenhar
// um ponto falso no zero.
private fun SensorReadingDto.toDomain() =
    SensorReading(label, LocalDate.parse(date), value.takeIf { it != 0.0 })

fun HiveHistoryDto.toDomain() = HiveHistory(
    temperature = temperature.map { it.toDomain() },
    weight = weight.map { it.toDomain() },
    co2 = co2.map { it.toDomain() },
)

fun AlertDto.toDomain() = Alert(
    id, hiveId, hiveName, type.toEnum<AlertType>(), severity.toEnum<Severity>(),
    message, Instant.parse(timestamp), read,
)

fun VisitDto.toDomain() = Visit(
    id = id,
    hiveId = hiveId,
    hiveName = hiveName,
    date = Instant.parse(date),
    queenSeen = queenSeen,
    broodLevel = broodLevel.toEnum<BroodLevel>(),
    temperament = temperament.toEnum<Temperament>(),
    treatment = treatment,
    notes = notes,
    latitude = latitude,
    longitude = longitude,
    photoIds = photoIds,
    authorId = authorId,
)

fun WaspSightingDto.toDomain() = WaspSighting(
    id, type.toEnum<SightingType>(), latitude, longitude, accuracy, notes,
    Instant.parse(date), photoId, authorId, authorName, distanceKm,
)
