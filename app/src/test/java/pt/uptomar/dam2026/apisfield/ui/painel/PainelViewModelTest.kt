package pt.uptomar.dam2026.apisfield.ui.painel

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
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import pt.uptomar.dam2026.apisfield.ColmeiasFalsas
import pt.uptomar.dam2026.apisfield.R
import pt.uptomar.dam2026.apisfield.colmeia
import pt.uptomar.dam2026.apisfield.data.remote.ApiError
import pt.uptomar.dam2026.apisfield.data.remote.ApiResult
import pt.uptomar.dam2026.apisfield.domain.EstadoApiario
import pt.uptomar.dam2026.apisfield.domain.model.HiveStatus
import pt.uptomar.dam2026.apisfield.ui.common.UiState

/** Testes do [PainelViewModel] com um repositório falso, sem rede. */
@OptIn(ExperimentalCoroutinesApi::class)
class PainelViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val lista = listOf(colmeia(1), colmeia(2, HiveStatus.DANGER))

    @Before
    fun preparar() = Dispatchers.setMain(dispatcher)

    @After
    fun terminar() = Dispatchers.resetMain()

    @Test
    fun carregaAoAbrirOEcra() = runTest(dispatcher) {
        val vm = PainelViewModel(ColmeiasFalsas(ApiResult.Sucesso(lista)))
        assertEquals(UiState.Loading, vm.estado.value.conteudo)

        advanceUntilIdle()

        val painel = (vm.estado.value.conteudo as UiState.Success).data
        assertEquals(lista, painel.colmeias)
        assertEquals(EstadoApiario.Perigo(1), painel.resumo.estado)
        assertFalse(vm.estado.value.aAtualizar)
    }

    @Test
    fun semColmeiasMostraOEstadoVazio() = runTest(dispatcher) {
        val vm = PainelViewModel(ColmeiasFalsas(ApiResult.Sucesso(emptyList())))
        advanceUntilIdle()

        assertEquals(UiState.Empty, vm.estado.value.conteudo)
    }

    @Test
    fun seOPrimeiroCarregamentoFalharMostraOErro() = runTest(dispatcher) {
        val vm = PainelViewModel(ColmeiasFalsas(ApiResult.Falha(ApiError.SemLigacao)))
        advanceUntilIdle()

        assertEquals(UiState.Error(R.string.erro_sem_ligacao), vm.estado.value.conteudo)
        assertNull(vm.estado.value.aviso)
    }

    @Test
    fun tentarOutraVezDepoisDeUmErroMostraOCarregamento() = runTest(dispatcher) {
        val colmeias = ColmeiasFalsas(ApiResult.Falha(ApiError.SemLigacao))
        val vm = PainelViewModel(colmeias)
        advanceUntilIdle()

        colmeias.lista = ApiResult.Sucesso(lista)
        vm.atualizar()
        assertEquals(UiState.Loading, vm.estado.value.conteudo)

        advanceUntilIdle()
        assertTrue(vm.estado.value.conteudo is UiState.Success)
    }

    @Test
    fun aoAtualizarAListaContinuaVisivel() = runTest(dispatcher) {
        val colmeias = ColmeiasFalsas(ApiResult.Sucesso(lista))
        val vm = PainelViewModel(colmeias)
        advanceUntilIdle()

        colmeias.lista = ApiResult.Sucesso(lista + colmeia(3))
        vm.atualizar()
        assertTrue(vm.estado.value.aAtualizar)
        assertTrue(vm.estado.value.conteudo is UiState.Success)

        advanceUntilIdle()
        assertFalse(vm.estado.value.aAtualizar)
        assertEquals(3, (vm.estado.value.conteudo as UiState.Success).data.resumo.total)
    }

    @Test
    fun seAAtualizacaoFalharMantemAListaEAvisaUmaVez() = runTest(dispatcher) {
        val colmeias = ColmeiasFalsas(ApiResult.Sucesso(lista))
        val vm = PainelViewModel(colmeias)
        advanceUntilIdle()

        colmeias.lista = ApiResult.Falha(ApiError.SemLigacao)
        vm.atualizar()
        advanceUntilIdle()

        assertEquals(lista, (vm.estado.value.conteudo as UiState.Success).data.colmeias)
        assertEquals(R.string.erro_sem_ligacao, vm.estado.value.aviso)
        assertFalse(vm.estado.value.aAtualizar)

        vm.avisoMostrado()
        assertNull(vm.estado.value.aviso)
    }

    @Test
    fun toquesRepetidosEmAtualizarSoFazemUmPedido() = runTest(dispatcher) {
        val colmeias = ColmeiasFalsas(ApiResult.Sucesso(lista))
        val vm = PainelViewModel(colmeias)

        vm.atualizar() // o primeiro carregamento ainda está a decorrer
        vm.atualizar()
        advanceUntilIdle()

        assertEquals(1, colmeias.chamadas)
    }
}
