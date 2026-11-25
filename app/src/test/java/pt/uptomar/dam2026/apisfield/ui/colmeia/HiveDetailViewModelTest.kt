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
import pt.uptomar.dam2026.apisfield.domain.model.HiveStatus
import pt.uptomar.dam2026.apisfield.ui.common.UiState

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
}
