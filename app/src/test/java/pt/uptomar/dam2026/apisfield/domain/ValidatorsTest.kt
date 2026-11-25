package pt.uptomar.dam2026.apisfield.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** Testes das regras de validação (as mesmas da secção "Validação" do contrato da API). */
class ValidatorsTest {

    @Test
    fun emailValidoPassaMesmoComEspacosNasPontas() {
        assertNull(Validators.email("  andre@apisfield.pt "))
    }

    @Test
    fun emailVazioOuMalFormadoERecusado() {
        assertEquals(ErroCampo.OBRIGATORIO, Validators.email("   "))
        assertEquals(ErroCampo.EMAIL_INVALIDO, Validators.email("andre@apisfield"))
        assertEquals(ErroCampo.EMAIL_INVALIDO, Validators.email("andre apisfield.pt"))
        assertEquals(ErroCampo.EMAIL_INVALIDO, Validators.email("a".repeat(116) + "@x.pt")) // 121 caracteres
    }

    @Test
    fun passwordNovaPrecisaDe8CaracteresComLetraEDigito() {
        assertNull(Validators.passwordNova("Abelha26"))
        assertEquals(ErroCampo.PASSWORD_FRACA, Validators.passwordNova("Abc123")) // curta
        assertEquals(ErroCampo.PASSWORD_FRACA, Validators.passwordNova("12345678")) // sem letras
        assertEquals(ErroCampo.PASSWORD_FRACA, Validators.passwordNova("abelhamel")) // sem dígitos
        assertEquals(ErroCampo.OBRIGATORIO, Validators.passwordNova(""))
    }

    @Test
    fun passwordDoLoginSoTemDeVirPreenchida() {
        assertNull(Validators.passwordLogin("x"))
        assertEquals(ErroCampo.OBRIGATORIO, Validators.passwordLogin(""))
    }

    @Test
    fun nomeEntre2E60Caracteres() {
        assertNull(Validators.nome("Al"))
        assertNull(Validators.nome("a".repeat(60)))
        assertEquals(ErroCampo.NOME_TAMANHO, Validators.nome(" A "))
        assertEquals(ErroCampo.NOME_TAMANHO, Validators.nome("a".repeat(61)))
        assertEquals(ErroCampo.OBRIGATORIO, Validators.nome(""))
    }

    @Test
    fun confirmacaoTemDeSerIgual() {
        assertNull(Validators.confirmacao("Abelha26", "Abelha26"))
        assertEquals(ErroCampo.CONFIRMACAO_DIFERENTE, Validators.confirmacao("Abelha26", "abelha26"))
        assertEquals(ErroCampo.OBRIGATORIO, Validators.confirmacao("Abelha26", ""))
    }
}
