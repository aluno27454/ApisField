package pt.uptomar.dam2026.apisfield.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pt.uptomar.dam2026.apisfield.R
import pt.uptomar.dam2026.apisfield.data.remote.ApiError
import pt.uptomar.dam2026.apisfield.data.remote.ApiResult
import pt.uptomar.dam2026.apisfield.data.remote.mensagem
import pt.uptomar.dam2026.apisfield.data.repository.AuthRepository
import pt.uptomar.dam2026.apisfield.domain.Validators
import pt.uptomar.dam2026.apisfield.ui.common.Texto

/**
 * Estado e lógica do ecrã de login.
 *
 * O ViewModel sobrevive à rotação do ecrã: um pedido a meio não se perde e os erros continuam visíveis.
 * A Activity só desenha o [estado] e chama [entrar].
 */
class LoginViewModel(private val auth: AuthRepository) : ViewModel() {

    private val _estado = MutableStateFlow(FormularioUiState())
    val estado: StateFlow<FormularioUiState> = _estado.asStateFlow()

    /** Valida os campos e, se estiverem bem, inicia sessão. */
    fun entrar(email: String, password: String) {
        // Ignora toques repetidos no botão enquanto o pedido está a decorrer.
        if (_estado.value.aEnviar) return

        val erros = buildMap {
            Validators.email(email)?.let { put(Campo.EMAIL, it.texto()) }
            Validators.passwordLogin(password)?.let { put(Campo.PASSWORD, it.texto()) }
        }
        if (erros.isNotEmpty()) {
            _estado.update { it.copy(erros = erros, mensagem = null) }
            return
        }

        _estado.update { it.copy(erros = emptyMap(), mensagem = null, aEnviar = true) }
        viewModelScope.launch {
            when (val r = auth.login(email, password)) {
                is ApiResult.Sucesso -> _estado.update { it.copy(aEnviar = false, concluido = true) }
                is ApiResult.Falha -> _estado.update { it.comErro(r.erro) }
            }
        }
    }

    /** Apaga o erro de um campo quando o utilizador volta a escrever nele. */
    fun limparErro(campo: Campo) = _estado.update { it.copy(erros = it.erros - campo) }

    private fun FormularioUiState.comErro(erro: ApiError) = when (erro) {
        // No login, o 401 significa email ou palavra-passe errados (não sessão expirada).
        ApiError.NaoAutenticado -> copy(aEnviar = false, mensagem = Texto.Recurso(R.string.login_erro_credenciais))
        is ApiError.Validacao -> copy(aEnviar = false, erros = erro.porCampo(), mensagem = Texto.Recurso(erro.mensagem()))
        else -> copy(aEnviar = false, mensagem = Texto.Recurso(erro.mensagem()))
    }
}
