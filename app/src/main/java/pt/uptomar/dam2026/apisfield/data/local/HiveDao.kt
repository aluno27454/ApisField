package pt.uptomar.dam2026.apisfield.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

/**
 * Acesso à cache de colmeias.
 *
 * É uma classe abstrata (e não uma interface) para poder ter [substituirTodas], uma função com
 * código próprio que junta duas operações numa transação. O Room gera o resto da implementação.
 *
 * As funções que devolvem [Flow] emitem uma lista nova sempre que a tabela muda; as funções
 * `suspend` correm numa thread do Room, fora da thread principal.
 */
@Dao
abstract class HiveDao {

    /** Todas as colmeias guardadas, por nome. */
    @Query("SELECT * FROM colmeias ORDER BY name")
    abstract fun observarTodas(): Flow<List<HiveEntity>>

    /**
     * Troca a cache pela lista acabada de chegar da API.
     *
     * Corre numa transação: quem estiver a observar a tabela nunca a vê vazia a meio da troca,
     * e se a inserção falhar a lista antiga fica intacta.
     */
    @Transaction
    open suspend fun substituirTodas(colmeias: List<HiveEntity>) {
        apagarTodas()
        inserir(colmeias)
    }

    @Query("DELETE FROM colmeias")
    protected abstract suspend fun apagarTodas()

    @Insert
    protected abstract suspend fun inserir(colmeias: List<HiveEntity>)
}
