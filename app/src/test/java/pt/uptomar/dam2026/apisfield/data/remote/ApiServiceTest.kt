package pt.uptomar.dam2026.apisfield.data.remote

import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import pt.uptomar.dam2026.apisfield.data.remote.dto.LoginRequest
import pt.uptomar.dam2026.apisfield.domain.model.HiveModel
import pt.uptomar.dam2026.apisfield.domain.model.HiveStatus

/**
 * Testes da camada de rede contra um servidor falso (MockWebServer), sem precisar da API real.
 *
 * Cada teste define a resposta do servidor, faz a chamada através de [safeApiCall] e verifica
 * o resultado. Referência: https://github.com/square/okhttp/tree/master/mockwebserver
 */
class ApiServiceTest {

    private lateinit var servidor: MockWebServer
    private lateinit var api: ApiService
    private var token: String? = null

    @Before
    fun preparar() {
        servidor = MockWebServer()
        servidor.start()
        api = createApiService(
            baseUrl = servidor.url("/").toString(),
            tokenProvider = { token },
            registarPedidos = false,
        )
    }

    @After
    fun terminar() {
        servidor.shutdown()
    }

    private fun responder(codigo: Int, corpo: String) {
        servidor.enqueue(MockResponse().setResponseCode(codigo).setBody(corpo))
    }

    @Test
    fun colmeiaStarterComCo2NuloEConvertidaParaODominio() = runTest {
        responder(
            200,
            """
            [{"id": 1, "name": "Colmeia 1", "model": "starter", "status": "warning",
              "location": {"latitude": 39.6, "longitude": -8.4, "label": "Apiário"},
              "sensors": {"tempInterior": 34.5, "tempExterior": 12.0, "humidity": 60.0,
                          "weight": 41.2, "co2": null, "battery": 15, "signal": 80,
                          "waspDetected": false},
              "lastUpdate": "2026-11-13T10:57:00Z", "ownerId": 7}]
            """.trimIndent(),
        )

        val resultado = safeApiCall { api.getHives().map { it.toDomain() } }

        val colmeia = (resultado as ApiResult.Sucesso).dados.single()
        assertEquals(HiveModel.STARTER, colmeia.model)
        assertEquals(HiveStatus.WARNING, colmeia.status)
        assertNull(colmeia.sensors.co2)
        assertEquals("/api/hives", servidor.takeRequest().path)
    }

    @Test
    fun historicoComValorZeroFicaSemDados() = runTest {
        responder(
            200,
            """
            {"temperature": [{"label": "Seg", "date": "2026-11-09", "value": 0},
                             {"label": "Ter", "date": "2026-11-10", "value": 34.1}],
             "weight": [], "co2": []}
            """.trimIndent(),
        )

        val historico = (safeApiCall { api.getHiveHistory(1).toDomain() } as ApiResult.Sucesso).dados

        assertNull(historico.temperature[0].value)
        assertEquals(34.1, historico.temperature[1].value!!, 0.001)
    }

    @Test
    fun tokenEEnviadoNoCabecalhoAuthorization() = runTest {
        token = "abc123"
        responder(200, """{"id": 7, "name": "André", "email": "a@b.pt", "role": "user"}""")

        safeApiCall { api.me() }

        assertEquals("Bearer abc123", servidor.takeRequest().getHeader("Authorization"))
    }

    @Test
    fun semTokenNaoHaCabecalhoAuthorization() = runTest {
        responder(200, """{"status": "ok"}""")

        safeApiCall { api.health() }

        assertNull(servidor.takeRequest().getHeader("Authorization"))
    }

    @Test
    fun resposta401DaNaoAutenticado() = runTest {
        responder(401, "")

        val resultado = safeApiCall { api.getHives() }

        assertEquals(ApiResult.Falha(ApiError.NaoAutenticado), resultado)
    }

    @Test
    fun resposta400DevolveOsErrosPorCampoEmCamelCase() = runTest {
        responder(
            400,
            """
            {"type": "https://tools.ietf.org/html/rfc9110#section-15.5.1",
             "title": "One or more validation errors occurred.", "status": 400,
             "errors": {"Email": ["O email não é válido."],
                        "Password": ["A palavra-passe tem de ter pelo menos 8 caracteres."]}}
            """.trimIndent(),
        )

        val resultado = safeApiCall { api.login(LoginRequest("a@b.pt", "x")) }

        val erro = (resultado as ApiResult.Falha).erro as ApiError.Validacao
        assertEquals(listOf("O email não é válido."), erro.erros["email"])
        assertTrue(erro.erros.containsKey("password"))
    }

    @Test
    fun servidorDesligadoDaSemLigacao() = runTest {
        // Desliga o servidor antes do pedido: a ligação é recusada, como sem rede.
        servidor.shutdown()

        val resultado = safeApiCall { api.getHives() }

        assertEquals(ApiResult.Falha(ApiError.SemLigacao), resultado)
    }

    @Test
    fun estadoDesconhecidoDaRespostaInvalida() = runTest {
        responder(
            200,
            """
            {"id": 1, "name": "C", "model": "pro", "status": "zombie",
             "location": {"latitude": 0, "longitude": 0, "label": ""},
             "sensors": {"tempInterior": 0, "tempExterior": 0, "humidity": 0, "weight": 0,
                         "co2": 400, "battery": 100, "signal": 100, "waspDetected": false},
             "lastUpdate": "2026-11-13T10:57:00Z", "ownerId": 1}
            """.trimIndent(),
        )

        val resultado = safeApiCall { api.getHive(1).toDomain() }

        assertEquals(ApiResult.Falha(ApiError.RespostaInvalida), resultado)
    }
}
