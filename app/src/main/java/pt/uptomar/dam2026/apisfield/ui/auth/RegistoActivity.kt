package pt.uptomar.dam2026.apisfield.ui.auth

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
import pt.uptomar.dam2026.apisfield.databinding.ActivityRegistoBinding

/**
 * Ecrã de criação de conta, aberto a partir do login. Depois de criar a conta, a sessão fica
 * iniciada e abre-se o ecrã principal. Funciona como a [LoginActivity], com mais dois campos.
 */
class RegistoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegistoBinding

    private val vm: RegistoViewModel by viewModels {
        viewModelFactory { initializer { RegistoViewModel((application as ApisFieldApp).container.authRepository) } }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityRegistoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        aplicarMargensDoSistema()

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.btnCriar.setOnClickListener { registar() }
        binding.etConfirmacao.setOnEditorActionListener { _, acao, _ ->
            if (acao == EditorInfo.IME_ACTION_DONE) registar()
            acao == EditorInfo.IME_ACTION_DONE
        }
        limparErroAoEscrever(binding.etNome, Campo.NOME)
        limparErroAoEscrever(binding.etEmail, Campo.EMAIL)
        limparErroAoEscrever(binding.etPassword, Campo.PASSWORD)
        limparErroAoEscrever(binding.etConfirmacao, Campo.CONFIRMACAO)

        observarEstado()
    }

    private fun registar() = vm.registar(
        binding.etNome.text.toString(),
        binding.etEmail.text.toString(),
        binding.etPassword.text.toString(),
        binding.etConfirmacao.text.toString(),
    )

    private fun limparErroAoEscrever(campo: TextInputEditText, qual: Campo) {
        campo.doAfterTextChanged { if (campo.hasFocus()) vm.limparErro(qual) }
    }

    private fun observarEstado() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.estado.collect { estado ->
                    mostrarEstado(
                        estado,
                        mapOf(
                            Campo.NOME to binding.tilNome,
                            Campo.EMAIL to binding.tilEmail,
                            Campo.PASSWORD to binding.tilPassword,
                            Campo.CONFIRMACAO to binding.tilConfirmacao,
                        ),
                        binding.tvMensagem, binding.btnCriar, binding.progresso,
                    )
                    if (estado.concluido) {
                        abrirEcraPrincipal()
                        finish()
                    }
                }
            }
        }
    }

    /** Seta de voltar da toolbar: regressa ao login. */
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun aplicarMargensDoSistema() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val m = insets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.ime())
            binding.toolbar.updatePadding(left = m.left, top = m.top, right = m.right)
            binding.scrollRegisto.updatePadding(left = m.left, right = m.right, bottom = m.bottom)
            insets
        }
    }
}
