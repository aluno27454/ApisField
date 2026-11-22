package pt.uptomar.dam2026.apisfield.data.local

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import pt.uptomar.dam2026.apisfield.domain.model.BroodLevel
import pt.uptomar.dam2026.apisfield.domain.model.Hive
import pt.uptomar.dam2026.apisfield.domain.model.HiveLocation
import pt.uptomar.dam2026.apisfield.domain.model.HiveModel
import pt.uptomar.dam2026.apisfield.domain.model.HiveSensors
import pt.uptomar.dam2026.apisfield.domain.model.HiveStatus
import pt.uptomar.dam2026.apisfield.domain.model.SightingType
import pt.uptomar.dam2026.apisfield.domain.model.SyncState
import pt.uptomar.dam2026.apisfield.domain.model.Temperament
import pt.uptomar.dam2026.apisfield.domain.model.Visit
import pt.uptomar.dam2026.apisfield.domain.model.WaspSighting
import java.time.Instant

/**
 * Testes dos DAOs com uma base de dados em memória.
 *
 * São testes instrumentados (correm num dispositivo ou emulador) porque o Room precisa do SQLite
 * do Android. A base de dados em memória é criada de novo antes de cada teste e desaparece no fim,
 * por isso os testes não dependem uns dos outros nem deixam dados no dispositivo.
 *
 * Referência: https://developer.android.com/training/data-storage/room/testing-db
 */
@RunWith(AndroidJUnit4::class)
class AppDatabaseTest {

    private lateinit var db: AppDatabase

    @Before
    fun abrir() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
    }

    @After
    fun fechar() {
        db.close()
    }

    private fun visita(
        data: String = "2026-11-20T10:00:00Z",
        syncState: SyncState = SyncState.PENDENTE,
    ) = Visit(
        id = null,
        hiveId = 3,
        hiveName = "Colmeia do Olival",
        date = Instant.parse(data),
        queenSeen = true,
        broodLevel = BroodLevel.FORTE,
        temperament = Temperament.CALMA,
        treatment = null,
        notes = "Rainha vista no quadro 4",
        latitude = 39.6,
        longitude = -8.41,
        photoIds = emptyList(),
        authorId = 7,
        syncState = syncState,
    )

    private fun colmeia(id: Int, nome: String) = Hive(
        id = id,
        name = nome,
        model = HiveModel.STARTER,
        status = HiveStatus.WARNING,
        location = HiveLocation(39.6, -8.4, "Apiário"),
        sensors = HiveSensors(34.5, 12.0, 60.0, 41.2, null, 15, 80, false),
        lastUpdate = Instant.parse("2026-11-13T10:57:00Z"),
        ownerId = 7,
    )

    @Test
    fun visitaPendenteGravadaVoltaIgualComOLocalIdAtribuido() = runTest {
        val localId = db.visitas().inserir(visita().toEntity())

        val guardada = db.visitas().observarTodas().first().single().toDomain()

        assertEquals(visita().copy(localId = localId), guardada)
        assertNull(guardada.id)
    }

    @Test
    fun soAsVisitasPendentesAparecemNosPendentes() = runTest {
        db.visitas().inserir(visita(syncState = SyncState.PENDENTE).toEntity())
        db.visitas().inserir(visita(syncState = SyncState.SINCRONIZADO).toEntity())

        val pendentes = db.visitas().porEstado(SyncState.PENDENTE)

        assertEquals(1, pendentes.size)
        assertEquals(SyncState.PENDENTE, pendentes.single().visit.syncState)
    }

    @Test
    fun visitaAtualizadaDepoisDeEnviadaSaiDosPendentes() = runTest {
        val localId = db.visitas().inserir(visita().toEntity())

        // O que a issue #18 vai fazer quando a API aceitar a visita: guardar o id e mudar o estado.
        db.visitas().atualizar(visita().copy(id = 42, localId = localId, syncState = SyncState.SINCRONIZADO).toEntity())

        assertTrue(db.visitas().porEstado(SyncState.PENDENTE).isEmpty())
        assertEquals(42, db.visitas().observarTodas().first().single().visit.serverId)
    }

    @Test
    fun visitasVemDaMaisRecenteParaAMaisAntiga() = runTest {
        db.visitas().inserir(visita(data = "2026-11-01T10:00:00Z").toEntity())
        db.visitas().inserir(visita(data = "2026-11-20T10:00:00Z").toEntity())

        val datas = db.visitas().observarTodas().first().map { it.visit.date }

        assertEquals(listOf(Instant.parse("2026-11-20T10:00:00Z"), Instant.parse("2026-11-01T10:00:00Z")), datas)
    }

    @Test
    fun soAsFotografiasJaEnviadasEntramNosIdsDaVisita() = runTest {
        val localId = db.visitas().inserir(visita().toEntity())
        db.visitas().inserirFoto(VisitPhotoEntity(visitLocalId = localId, filePath = "fotos/a.jpg", serverId = 10))
        db.visitas().inserirFoto(VisitPhotoEntity(visitLocalId = localId, filePath = "fotos/b.jpg"))

        val guardada = db.visitas().observarTodas().first().single()

        assertEquals(2, guardada.photos.size)
        assertEquals(listOf(10), guardada.toDomain().photoIds)
    }

    @Test
    fun apagarUmaVisitaApagaAsFotografiasEmCascata() = runTest {
        val localId = db.visitas().inserir(visita().toEntity())
        db.visitas().inserirFoto(VisitPhotoEntity(visitLocalId = localId, filePath = "fotos/a.jpg"))
        db.visitas().inserirFoto(VisitPhotoEntity(visitLocalId = localId, filePath = "fotos/b.jpg"))

        db.visitas().apagar(visita().copy(localId = localId).toEntity())

        assertTrue(db.visitas().fotosDaVisita(localId).isEmpty())
    }

    @Test
    fun substituirColmeiasTrocaACacheInteira() = runTest {
        db.colmeias().substituirTodas(listOf(colmeia(1, "Antiga A").toEntity(), colmeia(2, "Antiga B").toEntity()))

        db.colmeias().substituirTodas(listOf(colmeia(3, "Nova").toEntity()))

        // Também confirma que enums, datas e campos embutidos (com o co2 nulo) voltam iguais.
        assertEquals(listOf(colmeia(3, "Nova")), db.colmeias().observarTodas().first().map { it.toDomain() })
    }

    @Test
    fun avistamentoPendenteApareceNosPendentes() = runTest {
        val avistamento = WaspSighting(
            id = null,
            type = SightingType.NEST,
            latitude = 39.61,
            longitude = -8.42,
            accuracy = 8.0,
            notes = "Ninho num pinheiro",
            date = Instant.parse("2026-11-21T15:30:00Z"),
            photoId = null,
            authorId = 7,
            authorName = "Samuel Ponte",
            distanceKm = null,
            syncState = SyncState.PENDENTE,
        )
        val localId = db.avistamentos().inserir(avistamento.toEntity())

        val pendentes = db.avistamentos().porEstado(SyncState.PENDENTE).map { it.toDomain() }

        assertEquals(listOf(avistamento.copy(localId = localId)), pendentes)
    }
}
