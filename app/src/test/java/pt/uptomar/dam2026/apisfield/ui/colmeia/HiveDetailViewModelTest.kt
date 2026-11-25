package pt.uptomar.dam2026.apisfield.ui.colmeia

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import pt.uptomar.dam2026.apisfield.ColmeiasFalsas
import pt.uptomar.dam2026.apisfield.R
import pt.uptomar.dam2026.apisfield.colmeia
import pt.uptomar.dam2026.apisfield.data.remote.ApiError
import pt.uptomar.dam2026.apisfield.data.remote.ApiResult
import pt.uptomar.dam2026.apisfield.domain.model.HiveHistory
import pt.uptomar.dam2026.apisfield.domain.model.HiveStatus
import pt.uptomar.dam2026.apisfield.domain.model.SensorReading
import pt.uptomar.dam2026.apisfield.ui.common.UiState
import java.time.LocalDate

/** Testes do [HiveDetailViewModel] com um repositório falso, sem rede. */
@OptIn(ExperimentalCoroutinesApi::class)
class HiveDetailViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @Before
    fun preparar() = Dispatchers.setMain(dispatcher)

    @After
    fun terminar() = Dispatchers.resetMain()

    @Test
    fun carregaAColmeiaPedida() = runTest(dispatcher) {
        val colmeias = ColmeiasFalsas(colmeia = ApiResult.Sucesso(colmeia(7)))
        val vm = HiveDetailViewModel(7, colmeias)
        assertEquals(UiState.Loading, vm.estado.value.conteudo)

        advanceUntilIdle()

        assertEquals(UiState.Success(colmeia(7)), vm.estado.value.conteudo)
        assertEquals(listOf(7), colmeias.idsPedidos)
    }

    @Test
    fun umaColmeiaApagadaMostraOErro() = runTest(dispatcher) {
        val vm = HiveDetailViewModel(7, ColmeiasFalsas(colmeia = ApiResult.Falha(ApiError.NaoEncontrado)))
        advanceUntilIdle()

        assertEquals(UiState.Error(R.string.erro_nao_encontrado), vm.estado.value.conteudo)
    }

    @Test
    fun seAAtualizacaoFalharMantemAsLeiturasEAvisa() = runTest(dispatcher) {
        val colmeias = ColmeiasFalsas(colmeia = ApiResult.Sucesso(colmeia(7)))
        val vm = HiveDetailViewModel(7, colmeias)
        advanceUntilIdle()

        colmeias.colmeia = ApiResult.Falha(ApiError.SemLigacao)
        vm.atualizar()
        assertTrue(vm.estado.value.aAtualizar)
        advanceUntilIdle()

        assertEquals(UiState.Success(colmeia(7)), vm.estado.value.conteudo)
        assertEquals(R.string.erro_sem_ligacao, vm.estado.value.aviso)
        assertFalse(vm.estado.value.aAtualizar)
    }

    @Test
    fun atualizarTrazAsLeiturasNovas() = runTest(dispatcher) {
        val colmeias = ColmeiasFalsas(colmeia = ApiResult.Sucesso(colmeia(7)))
        val vm = HiveDetailViewModel(7, colmeias)
        advanceUntilIdle()

        colmeias.colmeia = ApiResult.Sucesso(colmeia(7, HiveStatus.DANGER))
        vm.atualizar()
        advanceUntilIdle()

        assertEquals(HiveStatus.DANGER, (vm.estado.value.conteudo as UiState.Success).data.status)
    }

    // ---- Histórico ----

    private fun serie(vararg valores: Double?) =
        valores.mapIndexed { i, v -> SensorReading("D$i", LocalDate.of(2026, 11, 19).plusDays(i.toLong()), v) }

    private val historico = HiveHistory(serie(34.0, null, 35.0), serie(40.0, 41.0, 41.5), emptyList())

    @Test
    fun carregaOHistorico() = runTest(dispatcher) {
        val vm = HiveDetailViewModel(
            7, ColmeiasFalsas(colmeia = ApiResult.Sucesso(colmeia(7)), historico = ApiResult.Sucesso(historico)),
        )
        assertEquals(UiState.Loading, vm.historico.value)

        advanceUntilIdle()

        assertEquals(UiState.Success(historico), vm.historico.value)
    }

    @Test
    fun umHistoricoSemValoresFicaVazio() = runTest(dispatcher) {
        val vazio = HiveHistory(serie(null, null), serie(null, null), emptyList())
        val vm = HiveDetailViewModel(
            7, ColmeiasFalsas(colmeia = ApiResult.Sucesso(colmeia(7)), historico = ApiResult.Sucesso(vazio)),
        )
        advanceUntilIdle()

        assertEquals(UiState.Empty, vm.historico.value)
    }

    @Test
    fun seSoOHistoricoFalharAsLeiturasAparecemNaMesma() = runTest(dispatcher) {
        val vm = HiveDetailViewModel(
            7, ColmeiasFalsas(colmeia = ApiResult.Sucesso(colmeia(7)), historico = ApiResult.Falha(ApiError.Servidor(500))),
        )
        advanceUntilIdle()

        assertEquals(UiState.Success(colmeia(7)), vm.estado.value.conteudo)
        assertEquals(UiState.Error(R.string.erro_servidor), vm.historico.value)
    }

    @Test
    fun seAAtualizacaoDoHistoricoFalharMantemOGraficoAnterior() = runTest(dispatcher) {
        val colmeias = ColmeiasFalsas(colmeia = ApiResult.Sucesso(colmeia(7)), historico = ApiResult.Sucesso(historico))
        val vm = HiveDetailViewModel(7, colmeias)
        advanceUntilIdle()

        colmeias.historico = ApiResult.Falha(ApiError.SemLigacao)
        vm.atualizar()
        advanceUntilIdle()

        assertEquals(UiState.Success(historico), vm.historico.value)
    }
}
