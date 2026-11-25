package pt.uptomar.dam2026.apisfield.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import pt.uptomar.dam2026.apisfield.domain.model.SyncState

/**
 * Acesso às visitas gravadas no telemóvel e às suas fotografias.
 *
 * As consultas que devolvem [VisitWithPhotos] têm [Transaction] porque o Room as executa em duas
 * consultas (visitas e fotografias); a transação garante que as duas veem os mesmos dados.
 */
@Dao
interface VisitDao {

    /** Todas as visitas, da mais recente para a mais antiga. */
    @Transaction
    @Query("SELECT * FROM visitas ORDER BY date DESC")
    fun observarTodas(): Flow<List<VisitWithPhotos>>

    /** Visitas num dado estado; a issue #18 usa-a para obter as que estão por enviar. */
    @Transaction
    @Query("SELECT * FROM visitas WHERE syncState = :estado ORDER BY date")
    suspend fun porEstado(estado: SyncState): List<VisitWithPhotos>

    /**
     * Número de visitas que ainda não chegaram à API: as pendentes e as que a API recusou (ERRO).
     * O Room guarda os enums pelo nome, por isso compara-se com o texto 'SINCRONIZADO'.
     */
    @Query("SELECT COUNT(*) FROM visitas WHERE syncState != 'SINCRONIZADO'")
    suspend fun contarPorEnviar(): Int

    /** Grava uma visita nova e devolve o `localId` que lhe foi atribuído. */
    @Insert
    suspend fun inserir(visita: VisitEntity): Long

    /** Atualiza uma visita existente (procurada pelo `localId`). */
    @Update
    suspend fun atualizar(visita: VisitEntity)

    /** Apaga uma visita; as suas fotografias são apagadas em cascata pelo SQLite. */
    @Delete
    suspend fun apagar(visita: VisitEntity)

    /** Associa uma fotografia a uma visita e devolve o id que lhe foi atribuído. */
    @Insert
    suspend fun inserirFoto(foto: VisitPhotoEntity): Long

    @Query("SELECT * FROM fotos_visita WHERE visitLocalId = :visitLocalId")
    suspend fun fotosDaVisita(visitLocalId: Long): List<VisitPhotoEntity>
}
