package co.edu.udistrital.geosismo.supervisor.network.model

data class UsuarioDto(
    val id: Int,
    val nombre: String,
    val email: String,
    val rol: String,
    val estado_voluntario: String? = null
)

data class LoginResponse(
    val ok: Boolean,
    val usuario: UsuarioDto? = null,
    val error: String? = null
)

data class VoluntarioPendienteDto(
    val id: Int,
    val nombre: String,
    val email: String,
    val telefono: String? = null,
    val profesion: String? = null,
    val tarjeta_profesional: String? = null,
    val created_at: String
)

data class VoluntariosPendientesResponse(
    val ok: Boolean,
    val voluntarios: List<VoluntarioPendienteDto> = emptyList(),
    val error: String? = null
)

data class ResolverVoluntarioResponse(
    val ok: Boolean,
    val mensaje: String? = null,
    val error: String? = null
)

data class ConteoPendientesResponse(
    val ok: Boolean,
    val pendientes: Int = 0,
    val error: String? = null
)

// ------------------------------------------------------------------
// Solicitudes de contacto (recibidas de las apps de ciudadanos/ingenieros)
// ------------------------------------------------------------------

data class SolicitudDto(
    val id: Int,
    val asunto: String,
    val mensaje: String,
    val archivo_adjunto: String? = null,
    val estado: String,
    val respuesta: String? = null,
    val created_at: String,
    val respondida_en: String? = null,
    val usuario_nombre: String? = null,
    val usuario_email: String? = null
)

data class ListaSolicitudesResponse(
    val ok: Boolean,
    val solicitudes: List<SolicitudDto> = emptyList(),
    val error: String? = null
)

data class ConteoSolicitudesPendientesResponse(
    val ok: Boolean,
    val pendientes: Int = 0,
    val error: String? = null
)

data class ResponderSolicitudResponse(
    val ok: Boolean,
    val mensaje: String? = null,
    val error: String? = null
)

// ------------------------------------------------------------------
// Gestión de usuarios
// ------------------------------------------------------------------

data class UsuarioAdminDto(
    val id: Int,
    val nombre: String,
    val email: String,
    val rol: String,
    val estado_voluntario: String,
    val profesion: String? = null,
    val telefono: String? = null,
    val created_at: String
)

data class ListaUsuariosResponse(
    val ok: Boolean,
    val usuarios: List<UsuarioAdminDto> = emptyList(),
    val error: String? = null
)

data class CambiarRolResponse(
    val ok: Boolean,
    val mensaje: String? = null,
    val error: String? = null
)

data class EliminarUsuarioResponse(
    val ok: Boolean,
    val mensaje: String? = null,
    val error: String? = null,
    val reportes_reasignados: Int = 0,
    val reportes_eliminados: Int = 0,
    val archivos_borrados: Int = 0
)

// ------------------------------------------------------------------
// Auditoría de evaluaciones
// ------------------------------------------------------------------

data class EvaluacionAdminDto(
    val id: Int,
    val reporte_id: Int,
    val nivel_dano: String,
    val color_clasificacion: String,
    val observaciones: String,
    val recomendacion: String? = null,
    val habitable: String,
    val estado_auditoria: String,
    val comentario_auditoria: String? = null,
    val auditor_nombre: String? = null,
    val auditado_en: String? = null,
    val fecha: String,
    val reporte_codigo: String,
    val reporte_titulo: String,
    val ingeniero_nombre: String
)

data class ListaEvaluacionesResponse(
    val ok: Boolean,
    val evaluaciones: List<EvaluacionAdminDto> = emptyList(),
    val error: String? = null
)

data class EvidenciaAdminDto(
    val id: Int,
    val tipo: String,
    val ruta_archivo: String,
    val descripcion: String? = null,
    val elemento_estructural: String? = null,
    val created_at: String
)

data class DetalleEvaluacionResponse(
    val ok: Boolean,
    val evaluacion: EvaluacionAdminDto? = null,
    val evidencias: List<EvidenciaAdminDto> = emptyList(),
    val inspeccion: InspeccionAdminDto? = null,
    val error: String? = null
)

data class CertificarEvaluacionResponse(
    val ok: Boolean,
    val mensaje: String? = null,
    val error: String? = null
)

// ------------------------------------------------------------------
// Moderación de reportes
// ------------------------------------------------------------------

data class ReporteAdminDto(
    val id: Int,
    val codigo: String,
    val titulo: String,
    val direccion: String? = null,
    val estado: String,
    val nivel_urgencia: String,
    val tipo_dano: String,
    val created_at: String,
    val reportante: String,
    val num_evidencias: Int? = 0
)

data class ListaReportesAdminResponse(
    val ok: Boolean,
    val reportes: List<ReporteAdminDto> = emptyList(),
    val error: String? = null
)

data class ModeracionResponse(
    val ok: Boolean,
    val mensaje: String? = null,
    val error: String? = null
)

// ------------------------------------------------------------------
// Centro de datos
// ------------------------------------------------------------------

data class ConteoDto(
    val estado: String? = null,
    val nivel_urgencia: String? = null,
    val tipo_dano: String? = null,
    val fecha: String? = null,
    val n: Int
)

data class EstadisticasResponse(
    val ok: Boolean,
    val total_reportes: Int = 0,
    val personas_afectadas: Int = 0,
    val total_evaluaciones: Int = 0,
    val ingenieros_activos: Int = 0,
    val por_estado: List<ConteoDto> = emptyList(),
    val por_urgencia: List<ConteoDto> = emptyList()
)

// ------------------------------------------------------------------
// Inspecciones técnicas estructuradas (protocolo de 8 pasos)
// ------------------------------------------------------------------

data class InspeccionAdminDto(
    val id: Int,
    val reporte_id: Int,
    val ingeniero_id: Int,
    val evaluacion_id: Int? = null,
    val fecha_inspeccion: String,
    val direccion_verificada: String? = null,
    val id_edificacion: String? = null,
    val foto_general: String? = null,
    val uso_edificacion: String? = null,
    val numero_pisos: Int? = null,
    val sistema_estructural: String? = null,
    val edad_aproximada: String? = null,
    val tipo_cubierta: String? = null,
    val ocupacion_momento_sismo: String? = null,
    val estado_fachada: String? = null,
    val danos_estructurales_ext: String? = null,
    val danos_no_estructurales_ext: String? = null,
    val grietas_desprendimientos: String? = null,
    val acceso_seguro: String,
    val danos_columnas: String? = null,
    val danos_vigas: String? = null,
    val danos_muros: String? = null,
    val danos_losas: String? = null,
    val estado_elementos_int: String? = null,
    val estado_escaleras: String? = null,
    val danos_no_estructurales_int: String? = null,
    val riesgo_colapso: String,
    val desplazamientos_inclinaciones: String,
    val elementos_sueltos: String,
    val danos_comprometen_estabilidad: String,
    val condiciones_impiden_ingreso: String,
    val clasificacion_preliminar: String,
    val observaciones_generales: String? = null,
    val anotaciones: String? = null,
    val recomendacion_preliminar: String? = null,
    val firma_inspector: String,
    val created_at: String,
    val reporte_codigo: String,
    val reporte_titulo: String,
    val ingeniero_nombre: String
)

data class ListaInspeccionesResponse(
    val ok: Boolean,
    val inspecciones: List<InspeccionAdminDto> = emptyList(),
    val error: String? = null
)
