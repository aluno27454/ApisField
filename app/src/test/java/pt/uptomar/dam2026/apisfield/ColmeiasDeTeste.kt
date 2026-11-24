package pt.uptomar.dam2026.apisfield

import pt.uptomar.dam2026.apisfield.domain.model.Hive
import pt.uptomar.dam2026.apisfield.domain.model.HiveLocation
import pt.uptomar.dam2026.apisfield.domain.model.HiveModel
import pt.uptomar.dam2026.apisfield.domain.model.HiveSensors
import pt.uptomar.dam2026.apisfield.domain.model.HiveStatus
import java.time.Instant

/** Colmeia com valores normais, para os testes só indicarem o que lhes interessa. */
fun colmeia(id: Int = 1, estado: HiveStatus = HiveStatus.HEALTHY, nome: String = "Colmeia $id") = Hive(
    id = id,
    name = nome,
    model = HiveModel.STARTER,
    status = estado,
    location = HiveLocation(39.6, -8.4, "Tomar"),
    sensors = HiveSensors(34.8, 14.2, 61.0, 38.4, null, 82, 74, false),
    lastUpdate = Instant.parse("2026-11-24T10:00:00Z"),
    ownerId = 2,
)
