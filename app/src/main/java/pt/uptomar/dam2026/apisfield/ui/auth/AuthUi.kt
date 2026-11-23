package pt.uptomar.dam2026.apisfield.ui.auth

import android.app.Activity
import android.content.Intent
import android.view.View
import android.widget.TextView
import com.google.android.material.textfield.TextInputLayout
import pt.uptomar.dam2026.apisfield.MainActivity
import pt.uptomar.dam2026.apisfield.ui.common.resolver

/*
 * Código partilhado pelos ecrãs de login e de registo.
 */

/**
 * Desenha o [estado] nos campos [campos], no texto de erro geral [mensagem], no [botao] e no [progresso].
 * Durante o pedido, o botão fica desativado e aparece o indicador de progresso.
 */
fun Activity.mostrarEstado(
    estado: FormularioUiState,
    campos: Map<Campo, TextInputLayout>,
    mensagem: TextView,
    botao: View,
    progresso: View,
) {
    for ((campo, layout) in campos) layout.error = estado.erros[campo]?.resolver(this)
    mensagem.text = estado.mensagem?.resolver(this)
    mensagem.visibility = if (estado.mensagem == null) View.GONE else View.VISIBLE
    botao.isEnabled = !estado.aEnviar
    progresso.visibility = if (estado.aEnviar) View.VISIBLE else View.GONE
}

/**
 * Abre o ecrã principal e fecha todos os ecrãs anteriores (login e registo), para o botão
 * "voltar" do Android não regressar ao login com a sessão já iniciada.
 */
fun Activity.abrirEcraPrincipal() {
    startActivity(
        Intent(this, MainActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
    )
}
