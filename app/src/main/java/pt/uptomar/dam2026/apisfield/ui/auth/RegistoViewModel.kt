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

/** Estado e lógica do ecrã de criação de conta. Funciona como o [LoginViewModel], com mais dois campos. */
class RegistoViewModel(private val auth: AuthRepository) : ViewModel() {

    private val _estado = MutableStateFlow(FormularioUiState())
    val estado: StateFlow<FormularioUiState> = _estado.asStateFlow()

    /** Valida os quatro campos e, se estiverem bem, cria a conta (que já fica com a sessão iniciada). */
    fun registar(nome: String, email: String, password: String, confirmacao: String) {
        if (_estado.value.aEnviar) return

        val erros = buildMap {
            Validators.nome(nome)?.let { put(Campo.NOME, it.texto()) }
            Validators.email(email)?.let { put(Campo.EMAIL, it.texto()) }
            Validators.passwordNova(password)?.let { put(Campo.PASSWORD, it.texto()) }
            Validators.confirmacao(password, confirmacao)?.let { put(Campo.CONFIRMACAO, it.texto()) }
        }
        if (erros.isNotEmpty()) {
            _estado.update { it.copy(erros = erros, mensagem = null) }
            return
        }

        _estado.update { it.copy(erros = emptyMap(), mensagem = null, aEnviar = true) }
        viewModelScope.launch {
            when (val r = auth.registar(nome, email, password)) {
                is ApiResult.Sucesso -> _estado.update { it.copy(aEnviar = false, concluido = true) }
                is ApiResult.Falha -> _estado.update { it.comErro(r.erro) }
            }
        }
    }

    fun limparErro(campo: Campo) = _estado.update { it.copy(erros = it.erros - campo) }

    private fun FormularioUiState.comErro(erro: ApiError) = when (erro) {
        // 409: o email já tem conta. O erro aparece no próprio campo do email.
        ApiError.Conflito -> copy(aEnviar = false, erros = mapOf(Campo.EMAIL to Texto.Recurso(R.string.registo_erro_email_em_uso)))
        is ApiError.Validacao -> copy(aEnviar = false, erros = erro.porCampo(), mensagem = Texto.Recurso(erro.mensagem()))
        else -> copy(aEnviar = false, mensagem = Texto.Recurso(erro.mensagem()))
    }
}
