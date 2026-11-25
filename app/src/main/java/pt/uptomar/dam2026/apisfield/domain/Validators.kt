package pt.uptomar.dam2026.apisfield.domain

/**
 * Motivos pelos quais um campo de um formulário pode ser recusado.
 *
 * O domínio não conhece os textos: a UI converte cada motivo numa mensagem de `strings.xml`.
 * Assim as regras podem ser testadas sem Android.
 */
enum class ErroCampo { OBRIGATORIO, EMAIL_INVALIDO, PASSWORD_FRACA, NOME_TAMANHO, CONFIRMACAO_DIFERENTE }

/**
 * Regras de validação dos formulários, iguais às da API (secção "Validação" de docs/CONTRATO.md).
 *
 * Validar na app antes de enviar evita um pedido à rede que já se sabe que vai falhar, e mostra o
 * erro logo junto do campo. A API valida na mesma, porque a app não é a única forma de lhe chegar.
 *
 * Cada função devolve null se o valor for válido, ou o [ErroCampo] se não for.
 */
object Validators {

    /** Tamanho máximo do email no contrato. */
    private const val EMAIL_MAX = 120

    /**
     * Formato simples de email: texto, @, domínio com ponto, sem espaços.
     * Não se usa android.util.Patterns.EMAIL_ADDRESS para estas regras correrem em testes sem Android.
     */
    private val FORMATO_EMAIL = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")

    fun email(valor: String): ErroCampo? {
        val email = valor.trim()
        return when {
            email.isEmpty() -> ErroCampo.OBRIGATORIO
            email.length > EMAIL_MAX || !FORMATO_EMAIL.matches(email) -> ErroCampo.EMAIL_INVALIDO
            else -> null
        }
    }

    /** Pelo menos 8 caracteres, com pelo menos uma letra e um dígito. */
    fun passwordNova(valor: String): ErroCampo? = when {
        valor.isEmpty() -> ErroCampo.OBRIGATORIO
        valor.length < 8 || valor.none { it.isLetter() } || valor.none { it.isDigit() } -> ErroCampo.PASSWORD_FRACA
        else -> null
    }

    /**
     * No login só se exige que a palavra-passe venha preenchida: as regras de força aplicam-se
     * ao criar a conta, e uma conta antiga podia ter sido criada com outras regras.
     */
    fun passwordLogin(valor: String): ErroCampo? = if (valor.isEmpty()) ErroCampo.OBRIGATORIO else null

    /** Entre 2 e 60 caracteres, sem contar os espaços nas pontas. */
    fun nome(valor: String): ErroCampo? {
        val nome = valor.trim()
        return when {
            nome.isEmpty() -> ErroCampo.OBRIGATORIO
            nome.length !in 2..60 -> ErroCampo.NOME_TAMANHO
            else -> null
        }
    }

    fun confirmacao(password: String, confirmacao: String): ErroCampo? = when {
        confirmacao.isEmpty() -> ErroCampo.OBRIGATORIO
        confirmacao != password -> ErroCampo.CONFIRMACAO_DIFERENTE
        else -> null
    }
}
