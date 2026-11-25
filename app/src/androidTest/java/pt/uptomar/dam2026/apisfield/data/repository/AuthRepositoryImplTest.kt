package pt.uptomar.dam2026.apisfield.data.repository

import android.content.Context
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
import pt.uptomar.dam2026.apisfield.data.local.AppDatabase
import pt.uptomar.dam2026.apisfield.data.local.SessionStore
import pt.uptomar.dam2026.apisfield.data.local.toEntity
import pt.uptomar.dam2026.apisfield.data.remote.createApiService
import pt.uptomar.dam2026.apisfield.domain.model.BroodLevel
import pt.uptomar.dam2026.apisfield.domain.model.Role
import pt.uptomar.dam2026.apisfield.domain.model.SightingType
import pt.uptomar.dam2026.apisfield.domain.model.SyncState
import pt.uptomar.dam2026.apisfield.domain.model.Temperament
import pt.uptomar.dam2026.apisfield.domain.model.User
import pt.uptomar.dam2026.apisfield.domain.model.Visit
import pt.uptomar.dam2026.apisfield.domain.model.WaspSighting
import java.time.Instant

/**
 * Testes do fim de sessão no [AuthRepositoryImpl]: contar os registos por enviar e apagar os
 * dados locais.
 *
 * Usa uma base de dados em memória e a sessão real (SharedPreferences da app de teste). A API não
 * é chamada nestes testes, por isso o endereço do cliente não interessa.
 */
@RunWith(AndroidJUnit4::class)
class AuthRepositoryImplTest {

    private lateinit var context: Context
    private lateinit var db: AppDatabase
    private lateinit var sessao: SessionStore
    private lateinit var repositorio: AuthRepositoryImpl

    @Before
    fun preparar() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        sessao = SessionStore(context)
        val api = createApiService("http://localhost/", { null }, registarPedidos = false)
        repositorio = AuthRepositoryImpl(api, sessao, db)
        sessao.guardar("token-de-teste", User(7, "Samuel Ponte", "samuel@apisfield.pt", Role.USER))
    }

    @After
    fun terminar() {
        sessao.limpar()
        db.close()
    }

    private fun visita(estado: SyncState) = Visit(
        id = null, hiveId = 3, hiveName = "Colmeia do Olival", date = Instant.parse("2026-11-20T10:00:00Z"),
        queenSeen = true, broodLevel = BroodLevel.MEDIA, temperament = Temperament.CALMA, treatment = null,
        notes = null, latitude = null, longitude = null, photoIds = emptyList(), authorId = 7, syncState = estado,
    )

    private fun avistamento(estado: SyncState) = WaspSighting(
        id = null, type = SightingType.WASP, latitude = 39.6, longitude = -8.4, accuracy = null, notes = null,
        date = Instant.parse("2026-11-21T15:30:00Z"), photoId = null, authorId = 7, authorName = "Samuel Ponte",
        distanceKm = null, syncState = estado,
    )

    @Test
    fun contaComoPorEnviarAsPendentesEAsRecusadasMasNaoAsSincronizadas() = runTest {
        db.visitas().inserir(visita(SyncState.PENDENTE).toEntity())
        db.visitas().inserir(visita(SyncState.ERRO).toEntity())
        db.visitas().inserir(visita(SyncState.SINCRONIZADO).toEntity())
        db.avistamentos().inserir(avistamento(SyncState.PENDENTE).toEntity())

        assertEquals(3, repositorio.registosPorEnviar())
    }

    @Test
    fun terminarSessaoApagaOsDadosLocaisEASessao() = runTest {
        db.visitas().inserir(visita(SyncState.PENDENTE).toEntity())
        db.avistamentos().inserir(avistamento(SyncState.SINCRONIZADO).toEntity())

        repositorio.terminarSessao()

        assertTrue(db.visitas().observarTodas().first().isEmpty())
        assertTrue(db.avistamentos().observarTodos().first().isEmpty())
        assertEquals(0, repositorio.registosPorEnviar())
        assertNull(sessao.token)
        assertNull(sessao.utilizador.value)
    }
}
