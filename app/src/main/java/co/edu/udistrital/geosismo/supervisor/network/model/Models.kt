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
