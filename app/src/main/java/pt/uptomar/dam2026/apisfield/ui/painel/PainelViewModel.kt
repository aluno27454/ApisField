package pt.uptomar.dam2026.apisfield.ui.painel

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
import pt.uptomar.dam2026.apisfield.domain.DashboardSummary
import pt.uptomar.dam2026.apisfield.domain.model.Hive
import pt.uptomar.dam2026.apisfield.ui.common.EcraUiState
import pt.uptomar.dam2026.apisfield.ui.common.UiState

/** Dados do painel já prontos a desenhar. */
data class PainelDados(val resumo: DashboardSummary, val colmeias: List<Hive>)

/** Carrega as colmeias e calcula o resumo. O primeiro carregamento é feito ao criar o ViewModel. */
class PainelViewModel(private val colmeias: HiveRepository) : ViewModel() {

    private val _estado = MutableStateFlow(EcraUiState<PainelDados>())
    val estado: StateFlow<EcraUiState<PainelDados>> = _estado.asStateFlow()

    init {
        atualizar()
    }

    /** Volta a pedir as colmeias. Se já houver uma lista no ecrã, ela continua visível durante o pedido. */
    fun atualizar() {
        if (_estado.value.aAtualizar) return

        _estado.update { it.aCarregar() }
        viewModelScope.launch {
            val resultado = colmeias.colmeias()
            _estado.update {
                when (resultado) {
                    is ApiResult.Sucesso -> it.comConteudo(conteudoDe(resultado.dados))
                    is ApiResult.Falha -> it.comFalha(resultado.erro.mensagem())
                }
            }
        }
    }

    /** A Snackbar já apareceu: sem isto, voltava a aparecer ao rodar o ecrã. */
    fun avisoMostrado() = _estado.update { it.copy(aviso = null) }

    private fun conteudoDe(lista: List<Hive>): UiState<PainelDados> =
        if (lista.isEmpty()) UiState.Empty else UiState.Success(PainelDados(DashboardSummary.de(lista), lista))
}
