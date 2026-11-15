package pt.uptomar.dam2026.apisfield.ui.common

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import pt.uptomar.dam2026.apisfield.databinding.FragmentEmConstrucaoBinding

/**
 * Fragment temporário para os separadores que ainda não foram implementados.
 *
 * Mostra o nome do separador (o `android:label` do destino em `nav_graph.xml`) e a indicação
 * "Em construção". Cada separador passa a ter o seu Fragment real na issue respetiva.
 */
class EmConstrucaoFragment : Fragment() {

    // O binding só existe entre onCreateView e onDestroyView; fora desse intervalo é null,
    // para não manter a hierarquia de Views em memória quando o Fragment não está visível.
    private var _binding: FragmentEmConstrucaoBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentEmConstrucaoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.tvTitulo.text = findNavController().currentDestination?.label
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
