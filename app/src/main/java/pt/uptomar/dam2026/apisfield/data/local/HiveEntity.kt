package pt.uptomar.dam2026.apisfield.data.local

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import pt.uptomar.dam2026.apisfield.domain.model.HiveLocation
import pt.uptomar.dam2026.apisfield.domain.model.HiveModel
import pt.uptomar.dam2026.apisfield.domain.model.HiveSensors
import pt.uptomar.dam2026.apisfield.domain.model.HiveStatus
import java.time.Instant

/**
 * Cópia local da última lista de colmeias recebida da API.
 *
 * Existe para a app mostrar as colmeias sem rede (no apiário) e para a issue #16 escolher a
 * colmeia mais próxima por GPS mesmo offline. É só uma cache: as colmeias criam-se e alteram-se
 * na API, e esta tabela é substituída por inteiro sempre que chega uma lista nova
 * ([HiveDao.substituirTodas]).
 *
 * A chave é o id da API, porque uma colmeia nunca é criada só no telemóvel.
 * A localização e os sensores ficam "embutidos" ([Embedded]): as suas propriedades passam a ser
 * colunas desta tabela (latitude, longitude, tempInterior...), sem tabelas à parte.
 */
@Entity(tableName = "colmeias")
data class HiveEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val model: HiveModel,
    val status: HiveStatus,
    @Embedded val location: HiveLocation,
    @Embedded val sensors: HiveSensors,
    val lastUpdate: Instant,
    val ownerId: Int,
)
