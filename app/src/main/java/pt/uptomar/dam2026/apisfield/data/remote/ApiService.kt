package pt.uptomar.dam2026.apisfield.data.remote

import okhttp3.RequestBody
import okhttp3.ResponseBody
import pt.uptomar.dam2026.apisfield.data.remote.dto.AlertDto
import pt.uptomar.dam2026.apisfield.data.remote.dto.AuthResultDto
import pt.uptomar.dam2026.apisfield.data.remote.dto.HealthDto
import pt.uptomar.dam2026.apisfield.data.remote.dto.HiveDto
import pt.uptomar.dam2026.apisfield.data.remote.dto.HiveHistoryDto
import pt.uptomar.dam2026.apisfield.data.remote.dto.HiveRequest
import pt.uptomar.dam2026.apisfield.data.remote.dto.LoginRequest
import pt.uptomar.dam2026.apisfield.data.remote.dto.PhotoIdDto
import pt.uptomar.dam2026.apisfield.data.remote.dto.RegisterRequest
import pt.uptomar.dam2026.apisfield.data.remote.dto.SightingRequest
import pt.uptomar.dam2026.apisfield.data.remote.dto.UserDto
import pt.uptomar.dam2026.apisfield.data.remote.dto.VisitDto
import pt.uptomar.dam2026.apisfield.data.remote.dto.VisitRequest
import pt.uptomar.dam2026.apisfield.data.remote.dto.WaspSightingDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Rotas da API ApisField (contrato em docs/CONTRATO.md do repositório ApisField-API).
 *
 * É uma interface: o Retrofit gera a implementação a partir das anotações. Cada função é
 * `suspend`, por isso corre numa coroutine sem bloquear a thread principal.
 *
 * Se a API responder com um código de erro (4xx/5xx), o Retrofit lança `HttpException`;
 * sem rede, o OkHttp lança `IOException`. Por isso as chamadas fazem-se sempre dentro de
 * [safeApiCall], que converte essas exceções em [ApiError].
 *
 * As rotas que respondem 204 (sem corpo) devolvem [Unit].
 *
 * Referência: https://square.github.io/retrofit/
 */
interface ApiService {

    // ---- Estado da API ----
    @GET("api/health")
    suspend fun health(): HealthDto

    // ---- Autenticação ----
    @POST("api/auth/register")
    suspend fun register(@Body body: RegisterRequest): AuthResultDto

    @POST("api/auth/login")
    suspend fun login(@Body body: LoginRequest): AuthResultDto

    @GET("api/me")
    suspend fun me(): UserDto

    // ---- Colmeias ----
    @GET("api/hives")
    suspend fun getHives(): List<HiveDto>

    @GET("api/hives/{id}")
    suspend fun getHive(@Path("id") id: Int): HiveDto

    @GET("api/hives/{id}/history")
    suspend fun getHiveHistory(@Path("id") id: Int): HiveHistoryDto

    @POST("api/hives")
    suspend fun createHive(@Body body: HiveRequest): HiveDto

    @PUT("api/hives/{id}")
    suspend fun updateHive(@Path("id") id: Int, @Body body: HiveRequest): HiveDto

    @DELETE("api/hives/{id}")
    suspend fun deleteHive(@Path("id") id: Int)

    // ---- Alertas ----
    @GET("api/alerts")
    suspend fun getAlerts(): List<AlertDto>

    @PATCH("api/alerts/{id}/read")
    suspend fun markAlertRead(@Path("id") id: Int): AlertDto

    @PATCH("api/alerts/read-all")
    suspend fun markAllAlertsRead()

    // ---- Visitas ----
    /** Sem [hiveId], devolve as visitas de todas as colmeias do utilizador. */
    @GET("api/visits")
    suspend fun getVisits(@Query("hiveId") hiveId: Int? = null): List<VisitDto>

    @GET("api/visits/{id}")
    suspend fun getVisit(@Path("id") id: Int): VisitDto

    @POST("api/hives/{hiveId}/visits")
    suspend fun createVisit(@Path("hiveId") hiveId: Int, @Body body: VisitRequest): VisitDto

    @PUT("api/visits/{id}")
    suspend fun updateVisit(@Path("id") id: Int, @Body body: VisitRequest): VisitDto

    @DELETE("api/visits/{id}")
    suspend fun deleteVisit(@Path("id") id: Int)

    /** O corpo é a fotografia em JPEG (ver [jpegBody]). */
    @POST("api/visits/{id}/photos")
    suspend fun uploadVisitPhoto(@Path("id") visitId: Int, @Body jpeg: RequestBody): PhotoIdDto

    // ---- Fotografias ----
    @GET("api/photos/{id}")
    suspend fun getPhoto(@Path("id") id: Int): ResponseBody

    @DELETE("api/photos/{id}")
    suspend fun deletePhoto(@Path("id") id: Int)

    // ---- Avistamentos de vespas ----
    /** Com [lat] e [lng], devolve os avistamentos dentro do raio, do mais perto para o mais longe. */
    @GET("api/sightings")
    suspend fun getSightings(
        @Query("lat") lat: Double? = null,
        @Query("lng") lng: Double? = null,
        @Query("radiusKm") radiusKm: Double? = null,
    ): List<WaspSightingDto>

    @GET("api/sightings/{id}")
    suspend fun getSighting(@Path("id") id: Int): WaspSightingDto

    @POST("api/sightings")
    suspend fun createSighting(@Body body: SightingRequest): WaspSightingDto

    @PUT("api/sightings/{id}")
    suspend fun updateSighting(@Path("id") id: Int, @Body body: SightingRequest): WaspSightingDto

    @DELETE("api/sightings/{id}")
    suspend fun deleteSighting(@Path("id") id: Int)

    @POST("api/sightings/{id}/photo")
    suspend fun uploadSightingPhoto(@Path("id") sightingId: Int, @Body jpeg: RequestBody): PhotoIdDto
}
