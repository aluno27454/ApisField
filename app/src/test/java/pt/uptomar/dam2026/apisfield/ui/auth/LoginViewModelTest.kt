package pt.uptomar.dam2026.apisfield.ui.auth

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
import pt.uptomar.dam2026.apisfield.R
import pt.uptomar.dam2026.apisfield.data.remote.ApiError
import pt.uptomar.dam2026.apisfield.data.remote.ApiResult
import pt.uptomar.dam2026.apisfield.data.repository.AuthRepository
import pt.uptomar.dam2026.apisfield.domain.model.Role
import pt.uptomar.dam2026.apisfield.domain.model.User
import pt.uptomar.dam2026.apisfield.ui.common.Texto

/**
 * Testes do [LoginViewModel] com um repositório falso, sem rede.
 *
 * O `viewModelScope` corre no dispatcher Main, que não existe nos testes unitários; por isso
 * substitui-se por um [StandardTestDispatcher], que só avança quando o teste manda
 * (`advanceUntilIdle`). Assim dá para ver o estado a meio do pedido.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    /** Repositório falso: devolve o [resultado] escolhido pelo teste e conta as chamadas. */
    private class AuthFalso(var resultado: ApiResult<User>) : AuthRepository {
        var chamadas = 0
        override suspend fun login(email: String, password: String): ApiResult<User> {
            chamadas++
            return resultado
        }
        override suspend fun registar(nome: String, email: String, password: String) = resultado
        override suspend fun registosPorEnviar() = 0
        override suspend fun terminarSessao() = Unit
    }

    private val dispatcher = StandardTestDispatcher()
    private val andre = User(1, "André", "andre@apisfield.pt", Role.USER)

    @Before
    fun preparar() = Dispatchers.setMain(dispatcher)

    @After
    fun terminar() = Dispatchers.resetMain()

    @Test
    fun camposInvalidosNaoChegamAFazerOPedido() = runTest(dispatcher) {
        val auth = AuthFalso(ApiResult.Sucesso(andre))
        val vm = LoginViewModel(auth)

        vm.entrar("nao-e-email", "")
        advanceUntilIdle()

        assertEquals(0, auth.chamadas)
        assertEquals(setOf(Campo.EMAIL, Campo.PASSWORD), vm.estado.value.erros.keys)
    }

    @Test
    fun duranteOPedidoFicaAEnviarEDepoisConcluido() = runTest(dispatcher) {
        val vm = LoginViewModel(AuthFalso(ApiResult.Sucesso(andre)))

        vm.entrar("andre@apisfield.pt", "Abelha2026!")
        assertTrue(vm.estado.value.aEnviar) // o pedido ainda não correu

        advanceUntilIdle()
        assertFalse(vm.estado.value.aEnviar)
        assertTrue(vm.estado.value.concluido)
    }

    @Test
    fun toqueRepetidoNoBotaoSoFazUmPedido() = runTest(dispatcher) {
        val auth = AuthFalso(ApiResult.Sucesso(andre))
        val vm = LoginViewModel(auth)

        vm.entrar("andre@apisfield.pt", "Abelha2026!")
        vm.entrar("andre@apisfield.pt", "Abelha2026!")
        advanceUntilIdle()

        assertEquals(1, auth.chamadas)
    }

    @Test
    fun credenciaisErradasMostramMensagemDeLogin() = runTest(dispatcher) {
        val vm = LoginViewModel(AuthFalso(ApiResult.Falha(ApiError.NaoAutenticado)))

        vm.entrar("andre@apisfield.pt", "errada")
        advanceUntilIdle()

        assertEquals(Texto.Recurso(R.string.login_erro_credenciais), vm.estado.value.mensagem)
        assertFalse(vm.estado.value.concluido)
    }

    @Test
    fun semRedeMostraMensagemDeSemLigacao() = runTest(dispatcher) {
        val vm = LoginViewModel(AuthFalso(ApiResult.Falha(ApiError.SemLigacao)))

        vm.entrar("andre@apisfield.pt", "Abelha2026!")
        advanceUntilIdle()

        assertEquals(Texto.Recurso(R.string.erro_sem_ligacao), vm.estado.value.mensagem)
    }

    @Test
    fun errosDaApiPorCampoAparecemNoCampoCerto() = runTest(dispatcher) {
        val erro = ApiError.Validacao(mapOf("email" to listOf("O email não é válido.")))
        val vm = LoginViewModel(AuthFalso(ApiResult.Falha(erro)))

        vm.entrar("andre@apisfield.pt", "Abelha2026!")
        advanceUntilIdle()

        assertEquals(Texto.Literal("O email não é válido."), vm.estado.value.erros[Campo.EMAIL])
    }

    @Test
    fun escreverNoCampoLimpaOErro() = runTest(dispatcher) {
        val vm = LoginViewModel(AuthFalso(ApiResult.Sucesso(andre)))
        vm.entrar("", "x")

        vm.limparErro(Campo.EMAIL)

        assertNull(vm.estado.value.erros[Campo.EMAIL])
    }
}
