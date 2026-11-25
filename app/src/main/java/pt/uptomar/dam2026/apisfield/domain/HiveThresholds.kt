package pt.uptomar.dam2026.apisfield.domain

/**
 * Limiares a partir dos quais uma leitura está fora do normal.
 *
 * São os mesmos com que a API calcula o estado da colmeia (docs/CONTRATO.md da API). Aqui servem
 * para assinalar o sensor que causou o estado. O valor igual ao limite ainda é normal.
 */
object HiveThresholds {
    const val TEMPERATURA_MAX = 37
    const val CO2_MAX = 3500
    const val BATERIA_MIN = 20
    const val SINAL_MIN = 30

    /** Temperatura interior, em °C. Acima do limite há risco de sobreaquecimento da criação. */
    fun temperaturaAlta(celsius: Double) = celsius > TEMPERATURA_MAX

    /** CO₂, em ppm (só no modelo Pro). */
    fun co2Alto(ppm: Double) = ppm > CO2_MAX

    /** Bateria, em %. */
    fun bateriaFraca(percentagem: Int) = percentagem < BATERIA_MIN

    /** Sinal de comunicação, em %. */
    fun sinalFraco(percentagem: Int) = percentagem < SINAL_MIN
}
