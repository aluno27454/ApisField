package pt.uptomar.dam2026.apisfield

import android.app.Application
import pt.uptomar.dam2026.apisfield.di.AppContainer

/**
 * Classe [Application] da ApisField.
 *
 * É criada pelo Android antes de qualquer Activity e vive enquanto o processo da app existir.
 * Por isso é o sítio certo para guardar o [AppContainer], que partilha as dependências
 * (cliente da API, base de dados, sessão) por todos os ecrãs.
 */
class ApisFieldApp : Application() {

    /** Dependências partilhadas pela app. Inicializado em [onCreate]. */
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
