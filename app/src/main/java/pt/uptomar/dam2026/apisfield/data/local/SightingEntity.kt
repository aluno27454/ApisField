package pt.uptomar.dam2026.apisfield.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import pt.uptomar.dam2026.apisfield.domain.model.SightingType
import pt.uptomar.dam2026.apisfield.domain.model.SyncState
import java.time.Instant

/**
 * Avistamento de vespa ou de ninho gravado no telemóvel.
 *
 * Tal como as visitas, é gravado primeiro aqui ([syncState] = PENDENTE) e enviado quando houver
 * rede, por isso tem um [localId] gerado pela base de dados e um [serverId] que só existe depois
 * do envio.
 *
 * A distância ao utilizador não é guardada: depende de onde ele está no momento, por isso é
 * calculada sempre que é precisa.
 */
@Entity(tableName = "avistamentos", indices = [Index("syncState")])
data class SightingEntity(
    @PrimaryKey(autoGenerate = true) val localId: Long = 0,
    val serverId: Int?,
    val type: SightingType,
    val latitude: Double,
    val longitude: Double,
    val accuracy: Double?,
    val notes: String?,
    val date: Instant,
    val photoId: Int?,
    val authorId: Int,
    val authorName: String,
    val syncState: SyncState,
)
