package pt.uptomar.dam2026.apisfield

import pt.uptomar.dam2026.apisfield.data.remote.ApiError
import pt.uptomar.dam2026.apisfield.data.remote.ApiResult
import pt.uptomar.dam2026.apisfield.data.repository.HiveRepository
import pt.uptomar.dam2026.apisfield.domain.model.Hive
import pt.uptomar.dam2026.apisfield.domain.model.HiveHistory
import pt.uptomar.dam2026.apisfield.domain.model.HiveLocation
import pt.uptomar.dam2026.apisfield.domain.model.HiveModel
import pt.uptomar.dam2026.apisfield.domain.model.HiveSensors
import pt.uptomar.dam2026.apisfield.domain.model.HiveStatus
import java.time.Instant

/** Repositório falso: devolve os resultados escolhidos pelo teste e regista o que foi pedido. */
class ColmeiasFalsas(
    var lista: ApiResult<List<Hive>> = ApiResult.Sucesso(emptyList()),
    var colmeia: ApiResult<Hive> = ApiResult.Falha(ApiError.NaoEncontrado),
    var historico: ApiResult<HiveHistory> = ApiResult.Falha(ApiError.NaoEncontrado),
) : HiveRepository {
    /** Quantas vezes se pediu a lista. */
    var chamadas = 0

    /** Ids pedidos a [colmeia], por ordem. */
    val idsPedidos = mutableListOf<Int>()

    override suspend fun colmeias(): ApiResult<List<Hive>> {
        chamadas++
        return lista
    }

    override suspend fun colmeia(id: Int): ApiResult<Hive> {
        idsPedidos += id
        return colmeia
    }

    override suspend fun historico(id: Int): ApiResult<HiveHistory> = historico
}

/** Colmeia com valores normais, para os testes só indicarem o que lhes interessa. */
fun colmeia(id: Int = 1, estado: HiveStatus = HiveStatus.HEALTHY, nome: String = "Colmeia $id") = Hive(
    id = id,
    name = nome,
    model = HiveModel.STARTER,
    status = estado,
    location = HiveLocation(39.6, -8.4, "Tomar"),
    sensors = HiveSensors(34.8, 14.2, 61.0, 38.4, null, 82, 74, false),
    lastUpdate = Instant.parse("2026-11-24T10:00:00Z"),
    ownerId = 2,
)
