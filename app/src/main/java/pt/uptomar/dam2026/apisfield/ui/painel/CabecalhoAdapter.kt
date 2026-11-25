package pt.uptomar.dam2026.apisfield.ui.painel

import android.content.Context
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import pt.uptomar.dam2026.apisfield.R
import pt.uptomar.dam2026.apisfield.databinding.ItemCabecalhoPainelBinding
import pt.uptomar.dam2026.apisfield.domain.DashboardSummary
import pt.uptomar.dam2026.apisfield.domain.EstadoApiario
import pt.uptomar.dam2026.apisfield.domain.Saudacao
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Cabeçalho do painel, como primeiro elemento da lista.
 *
 * É um adapter de um só elemento, junto ao das colmeias com um ConcatAdapter. Assim o cabeçalho
 * desliza com a lista, o que deixa mais espaço com o telemóvel deitado, e a RecyclerView continua
 * a reciclar os cartões (não continuaria dentro de um ScrollView).
 */
class CabecalhoAdapter : RecyclerView.Adapter<CabecalhoAdapter.CabecalhoViewHolder>() {

    /** Conteúdo do cabeçalho. A data e a saudação são as do momento em que é criado. */
    data class Cabecalho(val nome: String, val agora: LocalDateTime, val resumo: DashboardSummary)

    private var cabecalho: Cabecalho? = null

    /** Mostra o cabeçalho, ou esconde-o com null. */
    fun mostrar(novo: Cabecalho?) {
        val anterior = cabecalho
        cabecalho = novo
        when {
            anterior == null && novo != null -> notifyItemInserted(0)
            anterior != null && novo == null -> notifyItemRemoved(0)
            novo != null && anterior != novo -> notifyItemChanged(0)
        }
    }

    override fun getItemCount() = if (cabecalho == null) 0 else 1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        CabecalhoViewHolder(ItemCabecalhoPainelBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: CabecalhoViewHolder, position: Int) {
        cabecalho?.let(holder::mostrar)
    }

    class CabecalhoViewHolder(private val binding: ItemCabecalhoPainelBinding) : RecyclerView.ViewHolder(binding.root) {

        private val context: Context = binding.root.context

        fun mostrar(c: Cabecalho) {
            binding.tvData.text = c.agora.format(FORMATO_DATA)
            binding.tvSaudacao.text = context.getString(textoDe(Saudacao.daHora(c.agora.hour)), c.nome)
            mostrarEstado(c.resumo.estado)

            binding.tvTotal.text = numero(c.resumo.total)
            binding.tvSaudaveis.text = numero(c.resumo.saudaveis)
            binding.tvAtencao.text = numero(c.resumo.atencao)
            binding.tvPerigo.text = numero(c.resumo.perigo)
            binding.tvOffline.text = numero(c.resumo.offline)
            // Quase sempre a zero: só ocupa espaço quando há alguma colmeia sem comunicação.
            binding.cartaoOffline.isVisible = c.resumo.offline > 0
        }

        private fun textoDe(saudacao: Saudacao) = when (saudacao) {
            Saudacao.BOM_DIA -> R.string.painel_bom_dia
            Saudacao.BOA_TARDE -> R.string.painel_boa_tarde
            Saudacao.BOA_NOITE -> R.string.painel_boa_noite
        }

        private fun mostrarEstado(estado: EstadoApiario) {
            val res = context.resources
            val (texto, cor) = when (estado) {
                EstadoApiario.Tranquilo ->
                    context.getString(R.string.painel_tranquilo) to R.color.estado_saudavel
                is EstadoApiario.Atencao ->
                    res.getQuantityString(R.plurals.painel_em_atencao, estado.colmeias, estado.colmeias) to R.color.estado_atencao
                is EstadoApiario.Perigo ->
                    res.getQuantityString(R.plurals.painel_em_perigo, estado.colmeias, estado.colmeias) to R.color.estado_perigo
            }
            binding.tvEstado.text = texto
            binding.pontoEstado.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context, cor))
        }

        private fun numero(n: Int) = String.format(Locale.getDefault(), "%d", n)
    }

    private companion object {
        /** A app só existe em português, por isso a data também, qualquer que seja a língua do telemóvel. */
        val FORMATO_DATA: DateTimeFormatter =
            DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM", Locale.forLanguageTag("pt-PT"))
    }
}
