package pt.uptomar.dam2026.apisfield.ui.painel

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.TextViewCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import pt.uptomar.dam2026.apisfield.R
import pt.uptomar.dam2026.apisfield.databinding.ItemColmeiaBinding
import pt.uptomar.dam2026.apisfield.domain.model.Hive
import pt.uptomar.dam2026.apisfield.domain.model.HiveModel
import pt.uptomar.dam2026.apisfield.domain.model.HiveStatus
import pt.uptomar.dam2026.apisfield.ui.common.cor
import pt.uptomar.dam2026.apisfield.ui.common.corFundo
import pt.uptomar.dam2026.apisfield.ui.common.texto

/**
 * Lista de colmeias do painel.
 *
 * O [ListAdapter] compara a lista nova com a anterior (numa thread de fundo) e só redesenha os
 * cartões que mudaram.
 *
 * @param aoTocar chamada com a colmeia do cartão em que se tocou.
 */
class ColmeiasAdapter(private val aoTocar: (Hive) -> Unit) :
    ListAdapter<Hive, ColmeiasAdapter.ColmeiaViewHolder>(Diferenca) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ColmeiaViewHolder(ItemColmeiaBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ColmeiaViewHolder, position: Int) {
        val colmeia = getItem(position)
        holder.mostrar(colmeia)
        holder.itemView.setOnClickListener { aoTocar(colmeia) }
    }

    class ColmeiaViewHolder(private val binding: ItemColmeiaBinding) : RecyclerView.ViewHolder(binding.root) {

        private val context = binding.root.context

        fun mostrar(colmeia: Hive) {
            binding.tvNome.text = colmeia.name
            binding.tvLocal.text = colmeia.location.label
            binding.tvLocal.isVisible = colmeia.location.label.isNotBlank()
            mostrarModelo(colmeia.model)
            mostrarEstado(colmeia.status)

            val offline = colmeia.status == HiveStatus.OFFLINE
            binding.tvSemComunicacao.isVisible = offline
            binding.grupoSensores.isVisible = !offline
            if (!offline) mostrarSensores(colmeia)
        }

        private fun mostrarModelo(modelo: HiveModel) {
            val pro = modelo == HiveModel.PRO
            binding.tvModelo.setText(if (pro) R.string.modelo_pro else R.string.modelo_starter)
            binding.tvModelo.backgroundTintList = cores(if (pro) R.color.mel_claro else R.color.claro)
        }

        private fun mostrarEstado(estado: HiveStatus) {
            binding.faixaEstado.backgroundTintList = cores(estado.cor())
            binding.tvEstado.setText(estado.texto())
            binding.tvEstado.backgroundTintList = cores(estado.corFundo())
            TextViewCompat.setCompoundDrawableTintList(binding.tvEstado, cores(estado.cor()))
        }

        private fun mostrarSensores(colmeia: Hive) {
            val s = colmeia.sensors
            binding.chipTemperatura.text = context.getString(R.string.colmeia_temperatura, s.tempInterior)
            binding.chipPeso.text = context.getString(R.string.colmeia_peso, s.weight)
            binding.chipBateria.text = context.getString(R.string.colmeia_bateria, s.battery)
            // Só o modelo Pro tem sensor de CO₂.
            binding.chipCo2.isVisible = s.co2 != null
            s.co2?.let { binding.chipCo2.text = context.getString(R.string.colmeia_co2, it) }
            binding.chipVespa.isVisible = s.waspDetected
        }

        private fun cores(cor: Int) = ColorStateList.valueOf(ContextCompat.getColor(context, cor))
    }

    /** Diz ao ListAdapter se dois elementos são a mesma colmeia e se o conteúdo mudou. */
    private object Diferenca : DiffUtil.ItemCallback<Hive>() {
        override fun areItemsTheSame(antiga: Hive, nova: Hive) = antiga.id == nova.id
        override fun areContentsTheSame(antiga: Hive, nova: Hive) = antiga == nova
    }
}
