package pt.uptomar.dam2026.apisfield.ui.colmeia

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pt.uptomar.dam2026.apisfield.data.remote.ApiResult
import pt.uptomar.dam2026.apisfield.data.remote.mensagem
import pt.uptomar.dam2026.apisfield.data.repository.HiveRepository
import pt.uptomar.dam2026.apisfield.domain.model.Hive
import pt.uptomar.dam2026.apisfield.domain.model.HiveHistory
import pt.uptomar.dam2026.apisfield.ui.common.EcraUiState
import pt.uptomar.dam2026.apisfield.ui.common.UiState

/**
 * Carrega uma colmeia pelo [id] e o seu histórico. O primeiro carregamento é feito ao criar o ViewModel.
 *
 * O histórico tem um estado à parte: se só ele falhar, as leituras atuais aparecem na mesma.
 */
class HiveDetailViewModel(private val id: Int, private val colmeias: HiveRepository) : ViewModel() {

    private val _estado = MutableStateFlow(EcraUiState<Hive>())
    val estado: StateFlow<EcraUiState<Hive>> = _estado.asStateFlow()

    private val _historico = MutableStateFlow<UiState<HiveHistory>>(UiState.Loading)
    val historico: StateFlow<UiState<HiveHistory>> = _historico.asStateFlow()

    /** Os dois pedidos a decorrer, para não os repetir a cada toque em Atualizar. */
    private var pedidos: Job? = null

    init {
        atualizar()
    }

    /** Volta a pedir a colmeia e o histórico. O que já estiver no ecrã continua visível durante os pedidos. */
    fun atualizar() {
        if (pedidos?.isActive == true) return

        _estado.update { it.aCarregar() }
        if (_historico.value !is UiState.Success) _historico.value = UiState.Loading
        // Em paralelo: o ecrã aparece assim que a colmeia chega, sem esperar pelo histórico.
        pedidos = viewModelScope.launch {
            launch { carregarColmeia() }
            launch { carregarHistorico() }
        }
    }

    private suspend fun carregarColmeia() {
        val resultado = colmeias.colmeia(id)
        _estado.update {
            when (resultado) {
                is ApiResult.Sucesso -> it.comConteudo(UiState.Success(resultado.dados))
                is ApiResult.Falha -> it.comFalha(resultado.erro.mensagem())
            }
        }
    }

    private suspend fun carregarHistorico() {
        when (val resultado = colmeias.historico(id)) {
            is ApiResult.Sucesso ->
                _historico.value = if (resultado.dados.temValores()) UiState.Success(resultado.dados) else UiState.Empty
            // Se já houver gráficos no ecrã, ficam os anteriores.
            is ApiResult.Falha -> if (_historico.value !is UiState.Success) {
                _historico.value = UiState.Error(resultado.erro.mensagem())
            }
        }
    }

    private fun HiveHistory.temValores() = (temperature + weight + co2).any { it.value != null }

    /** A Snackbar já apareceu: sem isto, voltava a aparecer ao rodar o ecrã. */
    fun avisoMostrado() = _estado.update { it.copy(aviso = null) }
}
