package pt.uptomar.dam2026.apisfield

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import pt.uptomar.dam2026.apisfield.databinding.ActivityMainBinding

/**
 * Ecrã principal da app, mostrado depois do login.
 *
 * Contém a barra superior (com o menu **Sobre** e **Terminar sessão**) e a barra de navegação
 * inferior com os quatro separadores: Painel, Visitas, Vespas e Alertas. O conteúdo de cada
 * separador é um Fragment, trocado pelo Navigation Component segundo `res/navigation/nav_graph.xml`.
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
        // As ações do menu são implementadas nas issues #2 (Sobre) e #5 (Terminar sessão).
        R.id.acaoSobre, R.id.acaoTerminarSessao -> true
        else -> super.onOptionsItemSelected(item)
    }
}
