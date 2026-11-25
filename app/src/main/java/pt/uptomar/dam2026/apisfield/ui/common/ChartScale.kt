package pt.uptomar.dam2026.apisfield.ui.common

/**
 * Escala vertical de um gráfico: converte um valor (ex.: 34,8 °C) numa fração da altura.
 *
 * Não conhece píxeis nem o Canvas, por isso pode ser testada sem Android; é a [LineChartView]
 * que multiplica a fração pela altura disponível.
 */
data class ChartScale(val min: Double, val max: Double) {

    /** Posição de [valor] na altura do gráfico: 0 é o fundo e 1 é o topo. */
    fun fracao(valor: Double): Float = ((valor - min) / (max - min)).toFloat()

    companion object {
        /** Margem acima do máximo e abaixo do mínimo, em fração da amplitude. */
        private const val MARGEM = 0.1

        /**
         * Escala para [valores], ignorando os dias sem dados (null).
         *
         * A escala não começa em zero: entre 34 e 36 °C a variação é que interessa, e a partir
         * do zero a linha pareceria plana.
         *
         * @return null se não houver nenhum valor para desenhar.
         */
        fun de(valores: List<Double?>): ChartScale? {
            val comDados = valores.filterNotNull()
            if (comDados.isEmpty()) return null

            val min = comDados.min()
            val max = comDados.max()
            // Com os valores todos iguais a amplitude é 0 e a fração dividiria por zero: abre-se
            // uma unidade para cada lado e a linha fica a meio.
            val margem = if (max == min) 1.0 else (max - min) * MARGEM
            return ChartScale(min - margem, max + margem)
        }

        /** Posição horizontal do dia [indice] em [total] dias: 0 é a esquerda e 1 é a direita. */
        fun posicaoDia(indice: Int, total: Int): Float =
            if (total <= 1) 0.5f else indice.toFloat() / (total - 1)
    }
}
