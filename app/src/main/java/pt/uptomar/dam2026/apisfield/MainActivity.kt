package pt.uptomar.dam2026.apisfield

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
import pt.uptomar.dam2026.apisfield.databinding.ActivityMainBinding
import pt.uptomar.dam2026.apisfield.ui.auth.LoginActivity
import pt.uptomar.dam2026.apisfield.ui.sobre.SobreActivity

/**
 * Ecrã principal da app, mostrado depois do login.
 *
 * Contém a barra superior (com o menu **Sobre** e **Terminar sessão**) e a barra de navegação
 * inferior com os quatro separadores: Painel, Visitas, Vespas e Alertas. O conteúdo de cada
 * separador é um Fragment, trocado pelo Navigation Component segundo `res/navigation/nav_graph.xml`.
 *
 * Enquanto está visível, observa a sessão: se ela terminar (pelo menu ou por um 401 da API),
 * volta ao ecrã de login.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        aplicarMargensDoSistema()

        setSupportActionBar(binding.toolbar)
        configurarNavegacao()
        voltarAoLoginSeASessaoTerminar()
    }

    private val container get() = (application as ApisFieldApp).container

    /**
     * Quando a sessão deixa de existir, abre o login e fecha este ecrã. Cobre o "Terminar sessão"
     * e o 401 da API (token expirado), tratado no AuthInterceptor.
     */
    private fun voltarAoLoginSeASessaoTerminar() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                container.sessao.utilizador.collect { utilizador ->
                    if (utilizador == null) {
                        startActivity(
                            Intent(this@MainActivity, LoginActivity::class.java)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        )
                        finish()
                    }
                }
            }
        }
    }

    /**
     * Pede confirmação antes de terminar a sessão, para um toque acidental não obrigar a novo login.
     *
     * Terminar a sessão apaga os dados guardados no telemóvel. Se houver visitas ou avistamentos
     * que ainda não chegaram à API, a mensagem diz quantos são, porque esses perdem-se.
     */
    private fun confirmarTerminarSessao() {
        lifecycleScope.launch {
            val porEnviar = container.authRepository.registosPorEnviar()
            val mensagem = buildString {
                append(getString(R.string.terminar_sessao_mensagem))
                if (porEnviar > 0) {
                    append("\n\n")
                    val aviso = R.plurals.terminar_sessao_aviso_por_enviar
                    append(resources.getQuantityString(aviso, porEnviar, porEnviar))
                }
            }
            MaterialAlertDialogBuilder(this@MainActivity)
                .setTitle(R.string.terminar_sessao_titulo)
                .setMessage(mensagem)
                .setNegativeButton(R.string.cancelar, null)
                .setPositiveButton(R.string.terminar_sessao_confirmar) { _, _ ->
                    lifecycleScope.launch { container.authRepository.terminarSessao() }
                }
                .show()
        }
    }

    /**
     * Liga a barra inferior e o título da toolbar ao [androidx.navigation.NavController].
     *
     * Os quatro separadores são destinos de topo, por isso a toolbar nunca mostra a seta de "voltar"
     * ao alternar entre eles.
     */
    private fun configurarNavegacao() {
        // O NavHostFragment é obtido pelo FragmentManager porque, com FragmentContainerView,
        // o NavController ainda não existe quando a Activity é criada.
        val navHost = supportFragmentManager.findFragmentById(R.id.navHost) as NavHostFragment
        val navController = navHost.navController

        val destinosDeTopo = setOf(R.id.painel, R.id.visitas, R.id.vespas, R.id.alertas)
        setupActionBarWithNavController(navController, AppBarConfiguration(destinosDeTopo))
        binding.bottomNav.setupWithNavController(navController)
    }

    /**
     * Com o ecrã de ponta a ponta (edge-to-edge), o conteúdo desenha-se por baixo das barras
     * do sistema. Aqui afastam-se a toolbar da barra de estado e a barra inferior da barra de gestos.
     */
    private fun aplicarMargensDoSistema() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val barras = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.toolbar.updatePadding(top = barras.top)
            binding.bottomNav.updatePadding(bottom = barras.bottom)
            insets
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_principal, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.acaoSobre -> {
            startActivity(Intent(this, SobreActivity::class.java))
            true
        }
        R.id.acaoTerminarSessao -> {
            confirmarTerminarSessao()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }
}
