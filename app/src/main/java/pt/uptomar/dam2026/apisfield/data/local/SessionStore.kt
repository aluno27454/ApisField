package pt.uptomar.dam2026.apisfield.data.local

import android.content.Context
import androidx.core.content.edit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import pt.uptomar.dam2026.apisfield.domain.model.Role
import pt.uptomar.dam2026.apisfield.domain.model.User

/**
 * Guarda a sessão do utilizador (token e dados do utilizador) nas SharedPreferences.
 *
 * As SharedPreferences ficam na área privada da app, que outras apps não conseguem ler, e o
 * manifesto desativa os backups, por isso o token não sai do dispositivo.
 *
 * O utilizador atual é exposto como [StateFlow]: quando a sessão termina (pelo menu ou por um
 * 401 da API), quem estiver a observar, como a MainActivity, volta ao login.
 *
 * Referência: https://developer.android.com/training/data-storage/shared-preferences
 */
class SessionStore(context: Context) {

    private val prefs = context.getSharedPreferences(FICHEIRO, Context.MODE_PRIVATE)

    private val _utilizador = MutableStateFlow(lerUtilizador())

    /** Utilizador com sessão iniciada, ou null se não houver sessão. */
    val utilizador: StateFlow<User?> = _utilizador.asStateFlow()

    /** Token a enviar no cabeçalho Authorization, ou null se não houver sessão. */
    val token: String?
        get() = prefs.getString(CHAVE_TOKEN, null)

    /** Guarda uma sessão nova, depois do login ou do registo. */
    fun guardar(token: String, user: User) {
        prefs.edit {
            putString(CHAVE_TOKEN, token)
            putInt(CHAVE_ID, user.id)
            putString(CHAVE_NOME, user.name)
            putString(CHAVE_EMAIL, user.email)
            putString(CHAVE_PAPEL, user.role.name)
        }
        _utilizador.value = user
    }

    /** Termina a sessão: apaga o token e os dados do utilizador. */
    fun limpar() {
        prefs.edit { clear() }
        _utilizador.value = null
    }

    /** Reconstrói o utilizador a partir do que ficou guardado (por exemplo, ao reabrir a app). */
    private fun lerUtilizador(): User? {
        if (prefs.getString(CHAVE_TOKEN, null) == null) return null
        return User(
            id = prefs.getInt(CHAVE_ID, 0),
            name = prefs.getString(CHAVE_NOME, "").orEmpty(),
            email = prefs.getString(CHAVE_EMAIL, "").orEmpty(),
            role = runCatching { Role.valueOf(prefs.getString(CHAVE_PAPEL, "").orEmpty()) }.getOrDefault(Role.USER),
        )
    }

    private companion object {
        const val FICHEIRO = "sessao"
        const val CHAVE_TOKEN = "token"
        const val CHAVE_ID = "utilizador_id"
        const val CHAVE_NOME = "utilizador_nome"
        const val CHAVE_EMAIL = "utilizador_email"
        const val CHAVE_PAPEL = "utilizador_papel"
    }
}
