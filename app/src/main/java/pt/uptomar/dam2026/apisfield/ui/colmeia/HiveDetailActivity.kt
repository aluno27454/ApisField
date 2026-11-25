package pt.uptomar.dam2026.apisfield.ui.colmeia

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.core.widget.TextViewCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import pt.uptomar.dam2026.apisfield.ApisFieldApp
import pt.uptomar.dam2026.apisfield.R
import pt.uptomar.dam2026.apisfield.databinding.ActivityHiveDetailBinding
import pt.uptomar.dam2026.apisfield.databinding.ItemSensorBinding
import pt.uptomar.dam2026.apisfield.domain.HiveThresholds
import pt.uptomar.dam2026.apisfield.domain.model.Hive
import pt.uptomar.dam2026.apisfield.domain.model.HiveHistory
import pt.uptomar.dam2026.apisfield.domain.model.HiveModel
import pt.uptomar.dam2026.apisfield.domain.model.HiveSensors
import pt.uptomar.dam2026.apisfield.domain.model.HiveStatus
import pt.uptomar.dam2026.apisfield.domain.model.SensorReading
import pt.uptomar.dam2026.apisfield.ui.common.EcraUiState
import pt.uptomar.dam2026.apisfield.ui.common.LineChartView
import pt.uptomar.dam2026.apisfield.ui.common.UiState
import pt.uptomar.dam2026.apisfield.ui.common.cor
import pt.uptomar.dam2026.apisfield.ui.common.corFundo
import pt.uptomar.dam2026.apisfield.ui.common.horaOuData
import pt.uptomar.dam2026.apisfield.ui.common.texto
import java.time.Instant
import java.time.ZoneId
import kotlin.math.roundToInt

/**
 * Detalhe de uma colmeia: estado, avisos e leituras, com as que estão fora do normal a vermelho.
 *
 * Abre-se com [intent]. A lógica está no [HiveDetailViewModel]; aqui só se desenha o estado.
 */
class HiveDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHiveDetailBinding

    private val appContainer get() = (application as ApisFieldApp).container

    private val vm: HiveDetailViewModel by viewModels {
        viewModelFactory {
            initializer { HiveDetailViewModel(this@HiveDetailActivity.intent.getIntExtra(EXTRA_ID, 0), appContainer.hiveRepository) }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityHiveDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        aplicarMargensDoSistema()

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        // O nome da colmeia já aparece em grande no cartão.
        title = ""

        binding.mensagem.btnTentar.setOnClickListener { vm.atualizar() }
        // Os ecrãs das visitas são das issues #10 e #12; até lá, os botões dizem que ainda não existem.
        binding.btnRegistarVisita.setOnClickListener { avisar(R.string.em_construcao) }
        binding.btnVerVisitas.setOnClickListener { avisar(R.string.em_construcao) }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { vm.estado.collect(::mostrar) }
                launch { vm.historico.collect(::mostrarHistorico) }
                // Se a sessão expirar aqui (401), fecha: a MainActivity, por baixo, volta ao login.
                launch {
                    appContainer.sessao.utilizador.first { it == null }
                    finish()
                }
            }
        }
    }

    private fun mostrar(estado: EcraUiState<Hive>) {
        val conteudo = estado.conteudo
        binding.scroll.isVisible = conteudo is UiState.Success
        binding.progresso.isVisible = conteudo is UiState.Loading
        binding.progressoAtualizar.isVisible = estado.aAtualizar && conteudo is UiState.Success
        binding.mensagem.root.isVisible = conteudo is UiState.Error

        when (conteudo) {
            is UiState.Success -> mostrarColmeia(conteudo.data)
            is UiState.Error -> {
                binding.mensagem.tvMensagemTitulo.setText(R.string.detalhe_erro_titulo)
                binding.mensagem.tvMensagemTexto.setText(conteudo.message)
            }
            // Uma colmeia ou existe ou dá erro: este ecrã nunca fica vazio.
            is UiState.Loading, is UiState.Empty -> Unit
        }

        estado.aviso?.let {
            avisar(it)
            vm.avisoMostrado()
        }
    }

    private fun mostrarColmeia(colmeia: Hive) {
        binding.tvNome.text = colmeia.name
        binding.tvLocal.text = colmeia.location.label
        binding.tvLocal.isVisible = colmeia.location.label.isNotBlank()

        binding.faixaEstado.backgroundTintList = cores(colmeia.status.cor())
        binding.tvEstado.setText(colmeia.status.texto())
        binding.tvEstado.backgroundTintList = cores(colmeia.status.corFundo())
        TextViewCompat.setCompoundDrawableTintList(binding.tvEstado, cores(colmeia.status.cor()))

        val pro = colmeia.model == HiveModel.PRO
        binding.tvModelo.setText(if (pro) R.string.modelo_pro else R.string.modelo_starter)
        binding.tvModelo.backgroundTintList = cores(if (pro) R.color.mel_claro else R.color.claro)

        val hora = horaOuData(colmeia.lastUpdate, Instant.now(), ZoneId.systemDefault())
        binding.tvAtualizado.text = getString(R.string.detalhe_atualizado, hora)
        binding.tvAvisoVespa.isVisible = colmeia.sensors.waspDetected
        binding.tvAvisoOffline.isVisible = colmeia.status == HiveStatus.OFFLINE

        mostrarSensores(colmeia.sensors)
        binding.btnAbrirMapa.setOnClickListener { abrirNoMapa(colmeia) }
    }

    private fun mostrarSensores(s: HiveSensors) {
        binding.sensorTempInterior.mostrar(
            R.string.sensor_temp_interior, getString(R.string.colmeia_temperatura, s.tempInterior),
            getString(R.string.limite_max_temperatura, HiveThresholds.TEMPERATURA_MAX)
                .takeIf { HiveThresholds.temperaturaAlta(s.tempInterior) },
        )
        binding.sensorTempExterior.mostrar(
            R.string.sensor_temp_exterior, getString(R.string.colmeia_temperatura, s.tempExterior),
        )
        binding.sensorHumidade.mostrar(
            R.string.sensor_humidade, getString(R.string.valor_percentagem, s.humidity.roundToInt()),
        )
        binding.sensorPeso.mostrar(R.string.sensor_peso, getString(R.string.colmeia_peso, s.weight))
        // O modelo Starter não tem sensor de CO₂.
        binding.sensorCo2.root.isVisible = s.co2 != null
        s.co2?.let { co2 ->
            binding.sensorCo2.mostrar(
                R.string.sensor_co2, getString(R.string.valor_ppm, co2),
                getString(R.string.limite_max_ppm, HiveThresholds.CO2_MAX).takeIf { HiveThresholds.co2Alto(co2) },
            )
        }
        binding.sensorBateria.mostrar(
            R.string.sensor_bateria, getString(R.string.valor_percentagem, s.battery),
            getString(R.string.limite_min_percentagem, HiveThresholds.BATERIA_MIN)
                .takeIf { HiveThresholds.bateriaFraca(s.battery) },
        )
        binding.sensorSinal.mostrar(
            R.string.sensor_sinal, getString(R.string.valor_percentagem, s.signal),
            getString(R.string.limite_min_percentagem, HiveThresholds.SINAL_MIN)
                .takeIf { HiveThresholds.sinalFraco(s.signal) },
        )
    }

    /** Gráficos dos últimos 7 dias, ou a mensagem que os substitui. */
    private fun mostrarHistorico(estado: UiState<HiveHistory>) {
        binding.grupoGraficos.isVisible = estado is UiState.Success
        binding.tvHistorico.isVisible = estado !is UiState.Success
        when (estado) {
            is UiState.Loading -> binding.tvHistorico.setText(R.string.a_carregar)
            is UiState.Empty -> binding.tvHistorico.setText(R.string.grafico_vazio)
            is UiState.Error -> binding.tvHistorico.setText(estado.message)
            is UiState.Success -> {
                mostrarGrafico(binding.grupoTemperatura, binding.graficoTemperatura, estado.data.temperature)
                mostrarGrafico(binding.grupoPeso, binding.graficoPeso, estado.data.weight)
                mostrarGrafico(binding.grupoCo2, binding.graficoCo2, estado.data.co2)
            }
        }
    }

    /** Sem nenhum valor, o gráfico não aparece. É o caso do CO₂ no modelo Starter, que não tem esse sensor. */
    private fun mostrarGrafico(grupo: View, grafico: LineChartView, leituras: List<SensorReading>) {
        grupo.isVisible = leituras.any { it.value != null }
        grafico.mostrar(leituras)
        grafico.contentDescription = getString(R.string.grafico_descricao, grafico.resumo())
    }

    /**
     * Preenche a linha de um sensor. Com [limite], o valor fica a vermelho e o texto diz que limite
     * foi ultrapassado, para a informação não depender só da cor.
     */
    private fun ItemSensorBinding.mostrar(@StringRes nome: Int, valor: String, limite: String? = null) {
        tvNome.setText(nome)
        tvValor.text = valor
        tvLimite.text = limite
        tvLimite.isVisible = limite != null
        val cor = if (limite != null) R.color.estado_perigo else R.color.escuro
        tvValor.setTextColor(ContextCompat.getColor(this@HiveDetailActivity, cor))
    }

    /**
     * Mostra a colmeia na app de mapas do telemóvel, com um intent `geo:`.
     *
     * Referência: https://developer.android.com/guide/components/intents-common#Maps
     */
    private fun abrirNoMapa(colmeia: Hive) {
        val (latitude, longitude) = colmeia.location
        val uri = "geo:$latitude,$longitude?q=$latitude,$longitude(${Uri.encode(colmeia.name)})".toUri()
        try {
            startActivity(Intent(Intent.ACTION_VIEW, uri))
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, R.string.detalhe_sem_mapa, Toast.LENGTH_LONG).show()
        }
    }

    private fun avisar(@StringRes mensagem: Int) =
        Snackbar.make(binding.root, mensagem, Snackbar.LENGTH_LONG).show()

    private fun cores(cor: Int) = ColorStateList.valueOf(ContextCompat.getColor(this, cor))

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_atualizar, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId != R.id.acaoAtualizar) return super.onOptionsItemSelected(item)
        vm.atualizar()
        return true
    }

    /** Seta de "voltar": fecha este ecrã e regressa ao que o abriu (painel ou alertas). */
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    /** Afasta a toolbar e o conteúdo das barras do sistema e do recorte da câmara (ecrã de ponta a ponta). */
    private fun aplicarMargensDoSistema() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val m = insets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout())
            binding.toolbar.updatePadding(left = m.left, top = m.top, right = m.right)
            binding.corpo.updatePadding(left = m.left, right = m.right)
            binding.scroll.updatePadding(bottom = m.bottom)
            insets
        }
    }

    companion object {
        private const val EXTRA_ID = "id"

        /** Intent para abrir o detalhe da colmeia [id], a usar no painel e nos alertas. */
        fun intent(context: Context, id: Int): Intent =
            Intent(context, HiveDetailActivity::class.java).putExtra(EXTRA_ID, id)
    }
}
