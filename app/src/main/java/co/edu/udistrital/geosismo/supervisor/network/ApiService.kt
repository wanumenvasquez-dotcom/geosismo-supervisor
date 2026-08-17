package co.edu.udistrital.geosismo.supervisor.network

import co.edu.udistrital.geosismo.supervisor.network.model.CambiarRolResponse
import co.edu.udistrital.geosismo.supervisor.network.model.CertificarEvaluacionResponse
import co.edu.udistrital.geosismo.supervisor.network.model.ConteoPendientesResponse
import co.edu.udistrital.geosismo.supervisor.network.model.ConteoSolicitudesPendientesResponse
import co.edu.udistrital.geosismo.supervisor.network.model.ListaEvaluacionesResponse
import co.edu.udistrital.geosismo.supervisor.network.model.ListaReportesAdminResponse
import co.edu.udistrital.geosismo.supervisor.network.model.ListaSolicitudesResponse
import co.edu.udistrital.geosismo.supervisor.network.model.ListaUsuariosResponse
import co.edu.udistrital.geosismo.supervisor.network.model.LoginResponse
import co.edu.udistrital.geosismo.supervisor.network.model.ModeracionResponse
import co.edu.udistrital.geosismo.supervisor.network.model.ResolverVoluntarioResponse
import co.edu.udistrital.geosismo.supervisor.network.model.ResponderSolicitudResponse
import co.edu.udistrital.geosismo.supervisor.network.model.VoluntariosPendientesResponse
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.Streaming

/**
 * Coincide con los endpoints existentes de GeoSismo UD: api/auth.php,
 * api/admin.php y api/solicitudes.php. No requiere ningún otro cambio
 * en el servidor.
 */
interface ApiService {

    @FormUrlEncoded
    @POST("api/auth.php")
    suspend fun login(
        @Field("accion") accion: String = "login",
        @Field("email") email: String,
        @Field("password") password: String
    ): Response<LoginResponse>

    // ---------------- Voluntarios (Fase 1) ----------------

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

    // ---------------- Gestión de usuarios (Fase 2) ----------------

    @GET("api/admin.php")
    suspend fun usuariosTodos(
        @Query("accion") accion: String = "usuarios_todos",
        @Query("rol") rol: String? = null
    ): Response<ListaUsuariosResponse>

    @FormUrlEncoded
    @POST("api/admin.php")
    suspend fun cambiarRol(
        @Field("accion") accion: String = "cambiar_rol",
        @Field("usuario_id") usuarioId: Int,
        @Field("nuevo_rol") nuevoRol: String
    ): Response<CambiarRolResponse>

    // ---------------- Auditoría de evaluaciones (Fase 2) ----------------

    @GET("api/admin.php")
    suspend fun evaluacionesTodas(
        @Query("accion") accion: String = "evaluaciones_todas",
        @Query("estado_auditoria") estadoAuditoria: String? = null
    ): Response<ListaEvaluacionesResponse>

    @GET("api/admin.php")
    suspend fun evaluacionDetalle(
        @Query("accion") accion: String = "evaluacion_detalle",
        @Query("id") id: Int
    ): Response<co.edu.udistrital.geosismo.supervisor.network.model.DetalleEvaluacionResponse>

    @FormUrlEncoded
    @POST("api/admin.php")
    suspend fun certificarEvaluacion(
        @Field("accion") accion: String = "certificar_evaluacion",
        @Field("evaluacion_id") evaluacionId: Int,
        @Field("decision") decision: String,
        @Field("comentario") comentario: String
    ): Response<CertificarEvaluacionResponse>

    // ---------------- Moderación de reportes (Fase 3) ----------------

    @GET("api/admin.php")
    suspend fun reportesTodos(
        @Query("accion") accion: String = "reportes_todos",
        @Query("estado") estado: String? = null
    ): Response<ListaReportesAdminResponse>

    @FormUrlEncoded
    @POST("api/admin.php")
    suspend fun cambiarEstadoReporte(
        @Field("accion") accion: String = "cambiar_estado_reporte",
        @Field("reporte_id") reporteId: Int,
        @Field("nuevo_estado") nuevoEstado: String
    ): Response<ModeracionResponse>

    @FormUrlEncoded
    @POST("api/admin.php")
    suspend fun eliminarReporte(
        @Field("accion") accion: String = "eliminar_reporte",
        @Field("reporte_id") reporteId: Int
    ): Response<ModeracionResponse>

    // ---------------- Exportar CSV (Fase 3) ----------------

    @Streaming
    @GET("api/admin.php")
    suspend fun exportarCsv(
        @Query("accion") accion: String = "exportar_csv"
    ): Response<ResponseBody>

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

    // ---------------- Centro de datos ----------------

    @GET("api/estadisticas.php")
    suspend fun estadisticas(): Response<co.edu.udistrital.geosismo.supervisor.network.model.EstadisticasResponse>

    // ---------------- Inspecciones técnicas (protocolo de 8 pasos) ----------------

    @GET("api/inspecciones.php")
    suspend fun todasLasInspecciones(
        @Query("accion") accion: String = "todas"
    ): Response<co.edu.udistrital.geosismo.supervisor.network.model.ListaInspeccionesResponse>
}
