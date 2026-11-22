package pt.uptomar.dam2026.apisfield.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import pt.uptomar.dam2026.apisfield.BuildConfig

/**
 * Base de dados local da app (SQLite, através do Room).
 *
 * Guarda o que tem de estar disponível sem rede: a última lista de colmeias e as visitas e
 * avistamentos registados no campo, até serem enviados para a API.
 *
 * O Room gera, durante a compilação (com o KSP), o código SQL de cada DAO e verifica as consultas:
 * um erro numa `@Query` aparece ao compilar, e não só quando a app corre.
 *
 * `exportSchema = false` porque ainda não há migrações (ver [criar]).
 *
 * Referência: https://developer.android.com/training/data-storage/room
 */
@Database(
    entities = [HiveEntity::class, VisitEntity::class, VisitPhotoEntity::class, SightingEntity::class],
    version = 1,
    exportSchema = false,
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun colmeias(): HiveDao
    abstract fun visitas(): VisitDao
    abstract fun avistamentos(): SightingDao

    companion object {
        private const val NOME = "apisfield.db"

        /**
         * Abre (ou cria) a base de dados. Deve existir uma só instância na app: é o AppContainer
         * que a cria e partilha.
         *
         * Ainda não há migrações. Em debug, se a estrutura das tabelas mudar entre versões, a base
         * de dados é apagada e criada de novo (perdem-se os dados locais, o que durante o
         * desenvolvimento não faz mal). Em release isso nunca acontece em silêncio: antes de
         * publicar uma alteração às tabelas é preciso escrever a migração.
         */
        fun criar(context: Context): AppDatabase {
            val builder = Room.databaseBuilder(context, AppDatabase::class.java, NOME)
            if (BuildConfig.DEBUG) builder.fallbackToDestructiveMigration(dropAllTables = true)
            return builder.build()
        }
    }
}
