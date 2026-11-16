package pt.uptomar.dam2026.apisfield.ui.sobre

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import pt.uptomar.dam2026.apisfield.BuildConfig
import pt.uptomar.dam2026.apisfield.R
import pt.uptomar.dam2026.apisfield.databinding.ActivitySobreBinding
import pt.uptomar.dam2026.apisfield.databinding.ItemBibliotecaBinding

/**
 * Ecrã **Sobre**, obrigatório pelas regras da UC.
 *
 * Identifica o trabalho (curso, unidade curricular, ano letivo e instituição), os autores
 * (n.º, nome e fotografia), a versão da app e as bibliotecas e código de terceiros usados,
 * com a respetiva origem. É aberto a partir do menu da barra superior da
 * [pt.uptomar.dam2026.apisfield.MainActivity].
 *
 * O conteúdo é fixo, por isso não há estado a guardar quando o ecrã roda: a posição do scroll
 * é reposta pelo próprio `ScrollView`, porque tem um id no layout.
 */
class SobreActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySobreBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySobreBinding.inflate(layoutInflater)
        setContentView(binding.root)
        aplicarMargensDoSistema()

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // A versão vem do versionName em app/build.gradle.kts, para nunca ficar desatualizada.
        binding.tvVersao.text = getString(R.string.sobre_versao, BuildConfig.VERSION_NAME)
        mostrarBibliotecas()
    }

    /**
     * Acrescenta à secção de bibliotecas uma linha por cada entrada das listas
     * `sobre_bibliotecas_nomes` e `sobre_bibliotecas_origens` de `strings.xml`.
     *
     * Usa-se um [android.widget.LinearLayout] em vez de um `RecyclerView` porque a lista é curta
     * e fixa: não há nada para reciclar.
     */
    private fun mostrarBibliotecas() {
        val nomes = resources.getStringArray(R.array.sobre_bibliotecas_nomes)
        val origens = resources.getStringArray(R.array.sobre_bibliotecas_origens)
        // As duas listas têm de andar a par; se alguém acrescentar só a uma, falha logo ao abrir o ecrã.
        check(nomes.size == origens.size) {
            "Listas de bibliotecas com tamanhos diferentes: ${nomes.size} nomes e ${origens.size} origens"
        }

        nomes.zip(origens).forEach { (nome, origem) ->
            val linha = ItemBibliotecaBinding.inflate(layoutInflater, binding.listaBibliotecas, true)
            linha.tvNome.text = nome
            linha.tvOrigem.text = origem
            linha.root.setOnClickListener { abrirNoBrowser(origem) }
        }
    }

    /**
     * Abre [endereco] na app de browser do dispositivo.
     *
     * Se não houver nenhuma app capaz de abrir ligações, o Android lança
     * [ActivityNotFoundException]; nesse caso mostra-se uma mensagem em vez de a app fechar.
     */
    private fun abrirNoBrowser(endereco: String) {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, endereco.toUri()))
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, R.string.sobre_erro_sem_browser, Toast.LENGTH_LONG).show()
        }
    }

    /**
     * Seta de "voltar" da toolbar: fecha este ecrã e regressa à MainActivity que já está aberta.
     *
     * Não se usa `android:parentActivityName` no manifesto porque, com o modo de lançamento normal,
     * a MainActivity seria recriada e perdia-se o separador que estava escolhido.
     */
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    /**
     * Com o ecrã de ponta a ponta (edge-to-edge), o conteúdo desenha-se por baixo das barras
     * do sistema. Aqui afasta-se a toolbar da barra de estado e o conteúdo da barra de navegação.
     * Os lados também contam: em modo paisagem a barra de navegação e o recorte da câmara
     * podem ficar à esquerda ou à direita.
     */
    private fun aplicarMargensDoSistema() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val margens = insets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
            )
            binding.toolbar.updatePadding(left = margens.left, top = margens.top, right = margens.right)
            binding.scrollSobre.updatePadding(left = margens.left, right = margens.right, bottom = margens.bottom)
            insets
        }
    }
}
