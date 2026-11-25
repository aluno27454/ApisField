package pt.uptomar.dam2026.apisfield.ui.common

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.content.withStyledAttributes
import pt.uptomar.dam2026.apisfield.R
import pt.uptomar.dam2026.apisfield.domain.model.SensorReading
import java.util.Locale

/**
 * Gráfico de linha desenhado à mão, com o [Canvas] do Android: eixos, rótulos dos dias, linha e pontos.
 *
 * No XML escolhem-se a cor (`corLinha`), a unidade (`unidade`) e as casas decimais (`casasDecimais`).
 * Os dias sem dados não têm ponto, mas a linha passa por cima deles, de um dia com dados ao seguinte.
 *
 * Referência: https://developer.android.com/develop/ui/views/layout/custom-views/custom-drawing
 */
class LineChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : View(context, attrs) {

    private var leituras: List<SensorReading> = emptyList()
    private var escala: ChartScale? = null

    private var unidade = ""
    private var casasDecimais = 0

    // Os objetos de desenho criam-se uma vez: o onDraw pode correr dezenas de vezes por segundo e
    // criá-los lá obrigaria o sistema a estar sempre a limpar memória.
    private val pincelGrelha = Paint().apply {
        color = ContextCompat.getColor(context, R.color.borda)
        strokeWidth = dp(1f)
    }
    private val pincelTexto = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.medio)
        // Em sp, para respeitar o tamanho de letra escolhido no telemóvel.
        textSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 11f, resources.displayMetrics)
    }
    private val pincelLinha = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = dp(2.5f)
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
    }
    private val pincelPonto = Paint(Paint.ANTI_ALIAS_FLAG)
    private val caminho = Path()

    init {
        context.withStyledAttributes(attrs, R.styleable.LineChartView) {
            val cor = getColor(R.styleable.LineChartView_corLinha, ContextCompat.getColor(context, R.color.mel))
            pincelLinha.color = cor
            pincelPonto.color = cor
            unidade = getString(R.styleable.LineChartView_unidade).orEmpty()
            casasDecimais = getInt(R.styleable.LineChartView_casasDecimais, 0)
        }
    }

    /** Mostra [novas] leituras, por ordem de dia. */
    fun mostrar(novas: List<SensorReading>) {
        leituras = novas
        escala = ChartScale.de(novas.map { it.value })
        invalidate() // pede ao sistema que volte a chamar o onDraw
    }

    /**
     * Texto com todos os valores (ex.: "Seg 34,8 °C, Ter sem dados"), para o leitor de ecrã:
     * um desenho no Canvas não lhe diz nada.
     */
    fun resumo(): String = leituras.joinToString(", ") { leitura ->
        val valor = leitura.value
        val texto = if (valor == null) context.getString(R.string.grafico_sem_dados) else "${formatar(valor)} $unidade"
        "${leitura.label} $texto"
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val escala = escala ?: return

        // Retângulo da linha: o que sobra depois dos valores (à esquerda), da unidade (em cima)
        // e dos dias (em baixo).
        val alturaTexto = pincelTexto.fontSpacing
        val marcas = listOf(escala.min, (escala.min + escala.max) / 2, escala.max)
        val larguraValores = marcas.maxOf { pincelTexto.measureText(formatar(it)) }
        val esquerda = paddingLeft + larguraValores + dp(8f)
        // Metade de um rótulo fica para cada lado do ponto, por isso o último dia precisa de espaço.
        val direita = width - paddingRight - dp(16f)
        val topo = paddingTop + alturaTexto * 2f
        val fundo = height - paddingBottom - alturaTexto * 1.5f

        // Conversão de valores em píxeis. No Canvas o y cresce para baixo: o valor mais alto
        // (fração 1) tem de ficar no topo, que é o y mais pequeno.
        fun y(valor: Double) = fundo - escala.fracao(valor) * (fundo - topo)

        // O primeiro e o último dia ficam um pouco para dentro das bordas.
        val inicio = esquerda + dp(12f)
        fun x(dia: Int) = inicio + ChartScale.posicaoDia(dia, leituras.size) * (direita - inicio)

        desenharEixos(canvas, marcas, esquerda, direita, ::y)
        desenharDias(canvas, ::x)
        desenharLinha(canvas, ::x, ::y)
    }

    private fun desenharEixos(canvas: Canvas, marcas: List<Double>, esquerda: Float, direita: Float, y: (Double) -> Float) {
        pincelTexto.textAlign = Paint.Align.LEFT
        canvas.drawText(unidade, paddingLeft.toFloat(), paddingTop - pincelTexto.ascent(), pincelTexto)

        pincelTexto.textAlign = Paint.Align.RIGHT
        // Desloca o texto para ficar centrado na linha; o drawText usa a linha de base, e não o meio.
        val centrar = -(pincelTexto.ascent() + pincelTexto.descent()) / 2
        marcas.forEach { marca ->
            canvas.drawLine(esquerda, y(marca), direita, y(marca), pincelGrelha)
            canvas.drawText(formatar(marca), esquerda - dp(8f), y(marca) + centrar, pincelTexto)
        }
    }

    private fun desenharDias(canvas: Canvas, x: (Int) -> Float) {
        pincelTexto.textAlign = Paint.Align.CENTER
        val base = height - paddingBottom - pincelTexto.descent()
        leituras.forEachIndexed { dia, leitura -> canvas.drawText(leitura.label, x(dia), base, pincelTexto) }
    }

    private fun desenharLinha(canvas: Canvas, x: (Int) -> Float, y: (Double) -> Float) {
        caminho.rewind()
        leituras.forEachIndexed { dia, leitura ->
            val valor = leitura.value ?: return@forEachIndexed // sem dados: segue para o próximo dia
            if (caminho.isEmpty) caminho.moveTo(x(dia), y(valor)) else caminho.lineTo(x(dia), y(valor))
        }
        canvas.drawPath(caminho, pincelLinha)

        leituras.forEachIndexed { dia, leitura ->
            leitura.value?.let { canvas.drawCircle(x(dia), y(it), dp(4f), pincelPonto) }
        }
    }

    private fun formatar(valor: Double) = String.format(Locale.getDefault(), "%.${casasDecimais}f", valor)

    private fun dp(valor: Float) = valor * resources.displayMetrics.density
}
