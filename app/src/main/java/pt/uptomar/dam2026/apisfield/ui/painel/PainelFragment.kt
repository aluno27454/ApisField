package pt.uptomar.dam2026.apisfield.ui.painel

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.recyclerview.widget.ConcatAdapter
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import pt.uptomar.dam2026.apisfield.ApisFieldApp
import pt.uptomar.dam2026.apisfield.R
import pt.uptomar.dam2026.apisfield.databinding.FragmentPainelBinding
import pt.uptomar.dam2026.apisfield.domain.primeiroNome
import pt.uptomar.dam2026.apisfield.ui.colmeia.HiveDetailActivity
import pt.uptomar.dam2026.apisfield.ui.common.EcraUiState
import pt.uptomar.dam2026.apisfield.ui.common.UiState
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

/**
 * Painel: o estado do apiário à primeira vista, com o resumo e a lista de colmeias.
 *
 * A lógica está no [PainelViewModel]; aqui só se desenha o estado.
 */
class PainelFragment : Fragment(), MenuProvider {

    // Só existe entre onCreateView e onDestroyView (ver EmConstrucaoFragment).
    private var _binding: FragmentPainelBinding? = null
    private val binding get() = _binding!!

    private val appContainer get() = (requireActivity().application as ApisFieldApp).container

    private val vm: PainelViewModel by viewModels {
        viewModelFactory { initializer { PainelViewModel(appContainer.hiveRepository) } }
    }

    private val cabecalho = CabecalhoAdapter()
    private val colmeias = ColmeiasAdapter { colmeia ->
        startActivity(HiveDetailActivity.intent(requireContext(), colmeia.id))
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPainelBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lista.adapter = ConcatAdapter(cabecalho, colmeias)
        binding.mensagem.btnTentar.setOnClickListener { vm.atualizar() }
        // Com RESUMED, a ação desaparece da toolbar ao mudar de separador.
        requireActivity().addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.estado.collect(::mostrar)
            }
        }
    }

    private fun mostrar(estado: EcraUiState<PainelDados>) {
        val conteudo = estado.conteudo
        binding.lista.isVisible = conteudo is UiState.Success
        binding.progresso.isVisible = conteudo is UiState.Loading
        binding.progressoAtualizar.isVisible = estado.aAtualizar && conteudo is UiState.Success
        binding.mensagem.root.isVisible = conteudo is UiState.Empty || conteudo is UiState.Error

        when (conteudo) {
            is UiState.Loading -> Unit
            is UiState.Success -> mostrarDados(conteudo.data)
            is UiState.Empty -> {
                binding.mensagem.tvMensagemTitulo.setText(R.string.painel_vazio_titulo)
                binding.mensagem.tvMensagemTexto.setText(R.string.painel_vazio_texto)
                binding.mensagem.btnTentar.isVisible = false
            }
            is UiState.Error -> {
                binding.mensagem.tvMensagemTitulo.setText(R.string.painel_erro_titulo)
                binding.mensagem.tvMensagemTexto.setText(conteudo.message)
                binding.mensagem.btnTentar.isVisible = true
            }
        }

        estado.aviso?.let {
            Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
            vm.avisoMostrado()
        }
    }

    private fun mostrarDados(dados: PainelDados) {
        val nome = primeiroNome(appContainer.sessao.utilizador.value?.name.orEmpty())
        // Só a hora conta. Com os segundos, o cabeçalho seria sempre "diferente" e piscava a cada atualização.
        val agora = LocalDateTime.now().truncatedTo(ChronoUnit.HOURS)
        cabecalho.mostrar(CabecalhoAdapter.Cabecalho(nome, agora, dados.resumo))
        colmeias.submitList(dados.colmeias)
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) =
        menuInflater.inflate(R.menu.menu_atualizar, menu)

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        if (menuItem.itemId != R.id.acaoAtualizar) return false
        vm.atualizar()
        return true
    }

    override fun onDestroyView() {
        // Solta os adapters, que de outra forma mantinham a RecyclerView em memória.
        binding.lista.adapter = null
        super.onDestroyView()
        _binding = null
    }
}
