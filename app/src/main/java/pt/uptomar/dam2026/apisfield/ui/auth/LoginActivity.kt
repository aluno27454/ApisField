package pt.uptomar.dam2026.apisfield.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch
import pt.uptomar.dam2026.apisfield.ApisFieldApp
import pt.uptomar.dam2026.apisfield.databinding.ActivityLoginBinding

/**
 * Ecrã de arranque: login com email e palavra-passe.
 *
 * Se já houver sessão guardada, salta logo para a MainActivity sem mostrar o formulário.
 * A lógica (validação, pedido, erros) está no [LoginViewModel]; aqui só se liga o formulário ao estado.
 */
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    /** O ViewModel recebe o repositório do AppContainer (injeção de dependências manual). */
    private val vm: LoginViewModel by viewModels {
        viewModelFactory { initializer { LoginViewModel((application as ApisFieldApp).container.authRepository) } }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if ((application as ApisFieldApp).container.sessao.utilizador.value != null) {
            abrirEcraPrincipal()
            finish()
            return
        }

        enableEdgeToEdge()
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        aplicarMargensDoSistema()

        binding.btnEntrar.setOnClickListener { entrar() }
        // "Concluído" no teclado, no campo da palavra-passe, faz o mesmo que o botão.
        binding.etPassword.setOnEditorActionListener { _, acao, _ ->
            if (acao == EditorInfo.IME_ACTION_DONE) entrar()
            acao == EditorInfo.IME_ACTION_DONE
        }
        binding.btnCriarConta.setOnClickListener { startActivity(Intent(this, RegistoActivity::class.java)) }
        limparErroAoEscrever(binding.etEmail, Campo.EMAIL)
        limparErroAoEscrever(binding.etPassword, Campo.PASSWORD)

        observarEstado()
    }

    private fun entrar() = vm.entrar(binding.etEmail.text.toString(), binding.etPassword.text.toString())

    /**
     * Quando o utilizador volta a escrever num campo com erro, o erro desaparece.
     * Só conta se o campo tiver o foco: ao rodar o ecrã, o Android repõe o texto sozinho e isso
     * não deve apagar os erros.
     */
    private fun limparErroAoEscrever(campo: TextInputEditText, qual: Campo) {
        campo.doAfterTextChanged { if (campo.hasFocus()) vm.limparErro(qual) }
    }

    /** Desenha cada novo estado enquanto o ecrã está visível (STARTED). */
    private fun observarEstado() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.estado.collect { estado ->
                    mostrarEstado(
                        estado,
                        mapOf(Campo.EMAIL to binding.tilEmail, Campo.PASSWORD to binding.tilPassword),
                        binding.tvMensagem, binding.btnEntrar, binding.progresso,
                    )
                    if (estado.concluido) {
                        abrirEcraPrincipal()
                        finish()
                    }
                }
            }
        }
    }

    /** Afasta o conteúdo das barras do sistema e do teclado (ecrã de ponta a ponta). */
    private fun aplicarMargensDoSistema() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val m = insets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.ime())
            v.updatePadding(left = m.left, top = m.top, right = m.right, bottom = m.bottom)
            insets
        }
    }
}
