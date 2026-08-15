package co.edu.udistrital.geosismo.supervisor.network

import co.edu.udistrital.geosismo.supervisor.network.model.ConteoPendientesResponse
import co.edu.udistrital.geosismo.supervisor.network.model.LoginResponse
import co.edu.udistrital.geosismo.supervisor.network.model.ResolverVoluntarioResponse
import co.edu.udistrital.geosismo.supervisor.network.model.VoluntariosPendientesResponse
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * Coincide con los endpoints existentes de GeoSismo UD:
 * api/auth.php (login, ya existente) y api/admin.php (nuevo, Fase 1).
 * No requiere ningún otro cambio en el servidor.
 */
interface ApiService {

    @FormUrlEncoded
    @POST("api/auth.php")
    suspend fun login(
        @Field("accion") accion: String = "login",
        @Field("email") email: String,
        @Field("password") password: String
    ): Response<LoginResponse>

    @GET("api/admin.php")
    suspend fun voluntariosPendientes(
        @Query("accion") accion: String = "voluntarios_pendientes"
    ): Response<VoluntariosPendientesResponse>

    @FormUrlEncoded
    @POST("api/admin.php")
    suspend fun resolverVoluntario(
        @Field("accion") accion: String = "resolver_voluntario",
        @Field("usuario_id") usuarioId: Int,
        @Field("decision") decision: String
    ): Response<ResolverVoluntarioResponse>

    @GET("api/admin.php")
    suspend fun conteoPendientes(
        @Query("accion") accion: String = "conteo_pendientes"
    ): Response<ConteoPendientesResponse>
}
