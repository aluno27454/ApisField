package pt.uptomar.dam2026.apisfield.ui.colmeia

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pt.uptomar.dam2026.apisfield.data.remote.ApiResult
import pt.uptomar.dam2026.apisfield.data.remote.mensagem
import pt.uptomar.dam2026.apisfield.data.repository.HiveRepository
import pt.uptomar.dam2026.apisfield.domain.model.Hive
import pt.uptomar.dam2026.apisfield.ui.common.EcraUiState
import pt.uptomar.dam2026.apisfield.ui.common.UiState

/** Carrega uma colmeia pelo [id]. O primeiro carregamento é feito ao criar o ViewModel. */
class HiveDetailViewModel(private val id: Int, private val colmeias: HiveRepository) : ViewModel() {

    private val _estado = MutableStateFlow(EcraUiState<Hive>())
    val estado: StateFlow<EcraUiState<Hive>> = _estado.asStateFlow()

    init {
        atualizar()
    }

    /** Volta a pedir a colmeia. As leituras que já estiverem no ecrã continuam visíveis durante o pedido. */
    fun atualizar() {
        if (_estado.value.aAtualizar) return

        _estado.update { it.aCarregar() }
        viewModelScope.launch {
            val resultado = colmeias.colmeia(id)
            _estado.update {
                when (resultado) {
                    is ApiResult.Sucesso -> it.comConteudo(UiState.Success(resultado.dados))
                    is ApiResult.Falha -> it.comFalha(resultado.erro.mensagem())
                }
            }
        }
    }

    /** A Snackbar já apareceu: sem isto, voltava a aparecer ao rodar o ecrã. */
    fun avisoMostrado() = _estado.update { it.copy(aviso = null) }
}
