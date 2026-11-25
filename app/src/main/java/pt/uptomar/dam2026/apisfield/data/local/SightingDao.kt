package pt.uptomar.dam2026.apisfield.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import pt.uptomar.dam2026.apisfield.domain.model.SyncState

/** Acesso aos avistamentos gravados no telemóvel. Funciona como o [VisitDao], sem fotografias à parte. */
@Dao
interface SightingDao {

    /** Todos os avistamentos, do mais recente para o mais antigo. */
    @Query("SELECT * FROM avistamentos ORDER BY date DESC")
    fun observarTodos(): Flow<List<SightingEntity>>

    /** Avistamentos num dado estado (ex.: os que estão por enviar). */
    @Query("SELECT * FROM avistamentos WHERE syncState = :estado ORDER BY date")
    suspend fun porEstado(estado: SyncState): List<SightingEntity>

    /** Número de avistamentos que ainda não chegaram à API (pendentes ou recusados), como na visita. */
    @Query("SELECT COUNT(*) FROM avistamentos WHERE syncState != 'SINCRONIZADO'")
    suspend fun contarPorEnviar(): Int

    /** Grava um avistamento novo e devolve o `localId` que lhe foi atribuído. */
    @Insert
    suspend fun inserir(avistamento: SightingEntity): Long

    @Update
    suspend fun atualizar(avistamento: SightingEntity)

    @Delete
    suspend fun apagar(avistamento: SightingEntity)
}
