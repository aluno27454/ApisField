package pt.uptomar.dam2026.apisfield.domain

/** Saudação do painel, conforme a hora do dia. */
enum class Saudacao {
    BOM_DIA, BOA_TARDE, BOA_NOITE;

    companion object {
        /** Saudação para uma [hora] entre 0 e 23. */
        fun daHora(hora: Int): Saudacao = when (hora) {
            in 6..12 -> BOM_DIA
            in 13..19 -> BOA_TARDE
            else -> BOA_NOITE
        }
    }
}

/** Primeiro nome de um nome completo, para a saudação não ficar comprida. */
fun primeiroNome(nome: String): String = nome.trim().substringBefore(' ')
