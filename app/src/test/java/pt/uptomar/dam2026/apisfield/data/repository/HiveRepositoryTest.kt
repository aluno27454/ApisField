package pt.uptomar.dam2026.apisfield.data.repository

import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import pt.uptomar.dam2026.apisfield.data.remote.ApiError
import pt.uptomar.dam2026.apisfield.data.remote.ApiResult
import pt.uptomar.dam2026.apisfield.data.remote.createApiService
import pt.uptomar.dam2026.apisfield.domain.model.HiveStatus

/** Testes do [HiveRepositoryImpl] contra um servidor HTTP local (MockWebServer). */
class HiveRepositoryTest {

    private val servidor = MockWebServer()
    private lateinit var repositorio: HiveRepository

    @Before
    fun preparar() {
        servidor.start()
        repositorio = HiveRepositoryImpl(
            createApiService(servidor.url("/").toString(), tokenProvider = { "t" }, registarPedidos = false)
        )
    }

    @After
    fun terminar() = servidor.shutdown()

    @Test
    fun converteAsColmeiasParaODominio() = runTest {
        servidor.enqueue(
            MockResponse().setBody(
                """[{"id":2,"name":"Sobreiro Velho","model":"starter","status":"danger",
                "location":{"latitude":39.5,"longitude":-8.4,"label":"Asseiceira"},
                "sensors":{"tempInterior":35.1,"tempExterior":13.8,"humidity":58,"weight":41.7,"co2":null,
                "battery":67,"signal":55,"waspDetected":true},
                "lastUpdate":"2026-11-24T10:00:00Z","ownerId":2}]"""
            )
        )

        val colmeias = (repositorio.colmeias() as ApiResult.Sucesso).dados

        assertEquals(listOf("Sobreiro Velho" to HiveStatus.DANGER), colmeias.map { it.name to it.status })
        assertEquals("/api/hives", servidor.takeRequest().path)
    }

    @Test
    fun umErroDaApiChegaClassificado() = runTest {
        servidor.enqueue(MockResponse().setResponseCode(500))

        assertEquals(ApiResult.Falha(ApiError.Servidor(500)), repositorio.colmeias())
    }
}
