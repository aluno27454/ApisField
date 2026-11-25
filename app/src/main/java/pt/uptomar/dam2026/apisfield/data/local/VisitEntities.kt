package pt.uptomar.dam2026.apisfield.data.local

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import pt.uptomar.dam2026.apisfield.domain.model.BroodLevel
import pt.uptomar.dam2026.apisfield.domain.model.SyncState
import pt.uptomar.dam2026.apisfield.domain.model.Temperament
import java.time.Instant

/**
 * Visita gravada no telemóvel.
 *
 * No apiário pode não haver rede, por isso a visita é gravada aqui primeiro, com
 * [syncState] = PENDENTE, e enviada mais tarde (issue #18). Por isso tem dois ids:
 *  - [localId], gerado pela base de dados, que existe logo desde a gravação;
 *  - [serverId], o id na API, que só existe depois de a visita ser enviada.
 *
 * Não há chave estrangeira para [HiveEntity]: a cache de colmeias é apagada e reescrita a cada
 * atualização, e uma visita pendente não pode desaparecer por causa disso.
 */
@Entity(tableName = "visitas", indices = [Index("syncState"), Index("hiveId")])
data class VisitEntity(
    @PrimaryKey(autoGenerate = true) val localId: Long = 0,
    val serverId: Int?,
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
    val authorId: Int,
    val syncState: SyncState,
)

/**
 * Fotografia de um quadro, tirada durante uma visita (issue #14).
 *
 * A imagem fica num ficheiro na área privada da app ([filePath]); a base de dados só guarda o
 * caminho, porque guardar imagens dentro do SQLite torna-o lento e grande.
 * [serverId] é o id da fotografia na API, depois de enviada.
 *
 * A chave estrangeira com `onDelete = CASCADE` faz com que, ao apagar uma visita, o SQLite apague
 * sozinho as suas fotografias (os ficheiros têm de ser apagados à parte, pelo código da #14).
 */
@Entity(
    tableName = "fotos_visita",
    foreignKeys = [
        ForeignKey(
            entity = VisitEntity::class,
            parentColumns = ["localId"],
            childColumns = ["visitLocalId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("visitLocalId")],
)
data class VisitPhotoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val visitLocalId: Long,
    val filePath: String,
    val serverId: Int? = null,
)

/**
 * Uma visita com as suas fotografias, lidas numa só consulta.
 *
 * O Room preenche [photos] com as linhas de `fotos_visita` cujo `visitLocalId` é o `localId` da visita.
 */
data class VisitWithPhotos(
    @Embedded val visit: VisitEntity,
    @Relation(parentColumn = "localId", entityColumn = "visitLocalId")
    val photos: List<VisitPhotoEntity>,
)
