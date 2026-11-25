package pt.uptomar.dam2026.apisfield.data.repository

import pt.uptomar.dam2026.apisfield.data.remote.ApiResult
import pt.uptomar.dam2026.apisfield.data.remote.ApiService
import pt.uptomar.dam2026.apisfield.data.remote.safeApiCall
import pt.uptomar.dam2026.apisfield.data.remote.toDomain
import pt.uptomar.dam2026.apisfield.domain.model.Hive

/** Colmeias do utilizador. É uma interface para os ViewModels serem testados com uma versão falsa, sem rede. */
interface HiveRepository {
    /** Pede a lista de colmeias à API. O administrador recebe as de todos os utilizadores. */
    suspend fun colmeias(): ApiResult<List<Hive>>

    /** Pede uma colmeia à API. Se não existir ou for de outro utilizador, o erro é NaoEncontrado. */
    suspend fun colmeia(id: Int): ApiResult<Hive>
}

/** Implementação real, sobre a API. */
class HiveRepositoryImpl(private val api: ApiService) : HiveRepository {

    override suspend fun colmeias(): ApiResult<List<Hive>> =
        safeApiCall { api.getHives().map { it.toDomain() } }

    override suspend fun colmeia(id: Int): ApiResult<Hive> =
        safeApiCall { api.getHive(id).toDomain() }
}
