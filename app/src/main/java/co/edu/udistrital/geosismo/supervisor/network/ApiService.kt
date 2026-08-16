package co.edu.udistrital.geosismo.supervisor.network

import co.edu.udistrital.geosismo.supervisor.network.model.ConteoPendientesResponse
import co.edu.udistrital.geosismo.supervisor.network.model.ConteoSolicitudesPendientesResponse
import co.edu.udistrital.geosismo.supervisor.network.model.ListaSolicitudesResponse
import co.edu.udistrital.geosismo.supervisor.network.model.LoginResponse
import co.edu.udistrital.geosismo.supervisor.network.model.ResolverVoluntarioResponse
import co.edu.udistrital.geosismo.supervisor.network.model.ResponderSolicitudResponse
import co.edu.udistrital.geosismo.supervisor.network.model.VoluntariosPendientesResponse
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * Coincide con los endpoints existentes de GeoSismo UD:
 * api/auth.php (login), api/admin.php (voluntarios) y api/solicitudes.php
 * (bandeja de contacto). No requiere ningún otro cambio en el servidor.
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

    // ---------------- Solicitudes de contacto ----------------

    @GET("api/solicitudes.php")
    suspend fun todasLasSolicitudes(
        @Query("accion") accion: String = "todas",
        @Query("estado") estado: String? = null
    ): Response<ListaSolicitudesResponse>

    @GET("api/solicitudes.php")
    suspend fun conteoSolicitudesPendientes(
        @Query("accion") accion: String = "conteo_pendientes"
    ): Response<ConteoSolicitudesPendientesResponse>

    @FormUrlEncoded
    @POST("api/solicitudes.php")
    suspend fun responderSolicitud(
        @Field("accion") accion: String = "responder",
        @Field("id") id: Int,
        @Field("respuesta") respuesta: String
    ): Response<ResponderSolicitudResponse>

    @FormUrlEncoded
    @POST("api/solicitudes.php")
    suspend fun cerrarSolicitud(
        @Field("accion") accion: String = "cerrar",
        @Field("id") id: Int
    ): Response<ResponderSolicitudResponse>
}
