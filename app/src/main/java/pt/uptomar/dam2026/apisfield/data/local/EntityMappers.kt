package pt.uptomar.dam2026.apisfield.data.local

import pt.uptomar.dam2026.apisfield.domain.model.Hive
import pt.uptomar.dam2026.apisfield.domain.model.Visit
import pt.uptomar.dam2026.apisfield.domain.model.WaspSighting

/*
 * Conversão entre as entidades do Room e os modelos de domínio.
 *
 * Os ecrãs e os repositórios trabalham com os modelos de domínio; as entidades só existem para
 * o Room saber como guardar cada coisa numa tabela. Assim, mudar uma tabela não obriga a mudar
 * os ecrãs.
 */

fun HiveEntity.toDomain() = Hive(id, name, model, status, location, sensors, lastUpdate, ownerId)

fun Hive.toEntity() = HiveEntity(id, name, model, status, location, sensors, lastUpdate, ownerId)

/**
 * Converte uma visita guardada. Os ids das fotografias são os da API, por isso só entram as
 * fotografias que já foram enviadas; as que estão por enviar ficam em [VisitWithPhotos.photos].
 */
fun VisitWithPhotos.toDomain() = Visit(
    id = visit.serverId,
    hiveId = visit.hiveId,
    hiveName = visit.hiveName,
    date = visit.date,
    queenSeen = visit.queenSeen,
    broodLevel = visit.broodLevel,
    temperament = visit.temperament,
    treatment = visit.treatment,
    notes = visit.notes,
    latitude = visit.latitude,
    longitude = visit.longitude,
    photoIds = photos.mapNotNull { it.serverId },
    authorId = visit.authorId,
    localId = visit.localId,
    syncState = visit.syncState,
)

/**
 * Converte uma visita para a gravar. Uma visita nova ainda não tem `localId`: fica a 0, que é o
 * valor com que o Room sabe que tem de gerar um id. As fotografias gravam-se à parte
 * ([VisitDao.inserirFoto]).
 */
fun Visit.toEntity() = VisitEntity(
    localId = localId ?: 0,
    serverId = id,
    hiveId = hiveId,
    hiveName = hiveName,
    date = date,
    queenSeen = queenSeen,
    broodLevel = broodLevel,
    temperament = temperament,
    treatment = treatment,
    notes = notes,
    latitude = latitude,
    longitude = longitude,
    authorId = authorId,
    syncState = syncState,
)

/** A distância ao utilizador não é guardada (depende de onde ele está), por isso fica null. */
fun SightingEntity.toDomain() = WaspSighting(
    id = serverId,
    type = type,
    latitude = latitude,
    longitude = longitude,
    accuracy = accuracy,
    notes = notes,
    date = date,
    photoId = photoId,
    authorId = authorId,
    authorName = authorName,
    distanceKm = null,
    localId = localId,
    syncState = syncState,
)

fun WaspSighting.toEntity() = SightingEntity(
    localId = localId ?: 0,
    serverId = id,
    type = type,
    latitude = latitude,
    longitude = longitude,
    accuracy = accuracy,
    notes = notes,
    date = date,
    photoId = photoId,
    authorId = authorId,
    authorName = authorName,
    syncState = syncState,
)
