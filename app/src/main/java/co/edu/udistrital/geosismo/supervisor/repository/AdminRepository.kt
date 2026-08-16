package co.edu.udistrital.geosismo.supervisor.repository

import android.content.Context
import co.edu.udistrital.geosismo.supervisor.data.SessionManager
import co.edu.udistrital.geosismo.supervisor.network.ApiClient
import co.edu.udistrital.geosismo.supervisor.util.ErrorRed
import co.edu.udistrital.geosismo.supervisor.util.mensajeDeError
import co.edu.udistrital.geosismo.supervisor.network.model.VoluntarioPendienteDto
import co.edu.udistrital.geosismo.supervisor.network.model.SolicitudDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

sealed class Resultado<out T> {
    data class Exito<T>(val datos: T) : Resultado<T>()
    data class Fallo(val mensaje: String) : Resultado<Nothing>()
}

sealed class ResultadoLogin {
    data class Exito(val nombre: String, val rol: String) : ResultadoLogin()
    data class Fallo(val mensaje: String) : ResultadoLogin()
}

class AdminRepository(private val context: Context) {

    private val api get() = ApiClient.getApiService(context)
    private val session = SessionManager(context)

    suspend fun login(email: String, password: String): ResultadoLogin = withContext(Dispatchers.IO) {
        try {
            val resp = api.login(email = email, password = password)
            val body = resp.body()
            if (resp.isSuccessful && body?.ok == true && body.usuario != null) {
                if (body.usuario.rol != "admin") {
                    // Se guarda igual el intento para que loadForRequest tenga cookie,
                    // pero se informa que no tiene permisos y se cierra la sesión.
                    session.cerrarSesion()
                    return@withContext ResultadoLogin.Fallo(
                        "Esta cuenta (${body.usuario.rol}) no tiene permisos de administrador."
                    )
                }
                session.nombreUsuario = body.usuario.nombre
                session.rolUsuario = body.usuario.rol
                ResultadoLogin.Exito(body.usuario.nombre, body.usuario.rol)
            } else {
                ResultadoLogin.Fallo(resp.mensajeDeError() ?: body?.error ?: "No se pudo iniciar sesión (código ${resp.code()}).")
            }
        } catch (e: Exception) {
            ResultadoLogin.Fallo(ErrorRed.explicar(e))
        }
    }

    suspend fun voluntariosPendientes(): Resultado<List<VoluntarioPendienteDto>> = withContext(Dispatchers.IO) {
        try {
            val resp = api.voluntariosPendientes()
            val body = resp.body()
            when {
                resp.isSuccessful && body?.ok == true -> Resultado.Exito(body.voluntarios)
                resp.code() == 401 -> Resultado.Fallo("Tu sesión expiró. Cierra sesión y vuelve a iniciar.")
                resp.code() == 403 -> Resultado.Fallo("Esta cuenta no tiene permisos de administrador.")
                else -> Resultado.Fallo(resp.mensajeDeError() ?: body?.error ?: "No se pudo cargar la lista.")
            }
        } catch (e: Exception) {
            Resultado.Fallo(ErrorRed.explicar(e))
        }
    }

    suspend fun resolverVoluntario(usuarioId: Int, decision: String): Resultado<String> = withContext(Dispatchers.IO) {
        try {
            val resp = api.resolverVoluntario(usuarioId = usuarioId, decision = decision)
            val body = resp.body()
            when {
                resp.isSuccessful && body?.ok == true -> Resultado.Exito(body.mensaje ?: "Listo.")
                resp.code() == 409 -> Resultado.Fallo("Esta postulación ya fue resuelta por otra persona.")
                resp.code() == 401 || resp.code() == 403 -> Resultado.Fallo("No tienes permisos, o tu sesión expiró.")
                else -> Resultado.Fallo(resp.mensajeDeError() ?: body?.error ?: "No se pudo procesar la decisión.")
            }
        } catch (e: Exception) {
            Resultado.Fallo(ErrorRed.explicar(e))
        }
    }

    // ------------------------------------------------------------------
    // Solicitudes de contacto (bandeja del administrador)
    // ------------------------------------------------------------------
    suspend fun todasLasSolicitudes(estado: String? = null): Resultado<List<SolicitudDto>> = withContext(Dispatchers.IO) {
        try {
            val resp = api.todasLasSolicitudes(estado = estado)
            val body = resp.body()
            when {
                resp.isSuccessful && body?.ok == true -> Resultado.Exito(body.solicitudes)
                resp.code() == 403 -> Resultado.Fallo("Esta cuenta no tiene permisos de administrador.")
                else -> Resultado.Fallo(resp.mensajeDeError() ?: body?.error ?: "No se pudieron cargar las solicitudes.")
            }
        } catch (e: Exception) {
            Resultado.Fallo(ErrorRed.explicar(e))
        }
    }

    suspend fun responderSolicitud(id: Int, respuesta: String): Resultado<String> = withContext(Dispatchers.IO) {
        try {
            val resp = api.responderSolicitud(id = id, respuesta = respuesta)
            val body = resp.body()
            if (resp.isSuccessful && body?.ok == true) Resultado.Exito(body.mensaje ?: "Respuesta enviada.")
            else Resultado.Fallo(resp.mensajeDeError() ?: body?.error ?: "No se pudo enviar la respuesta.")
        } catch (e: Exception) {
            Resultado.Fallo(ErrorRed.explicar(e))
        }
    }

    suspend fun cerrarSolicitud(id: Int): Resultado<String> = withContext(Dispatchers.IO) {
        try {
            val resp = api.cerrarSolicitud(id = id)
            val body = resp.body()
            if (resp.isSuccessful && body?.ok == true) Resultado.Exito(body.mensaje ?: "Solicitud cerrada.")
            else Resultado.Fallo(resp.mensajeDeError() ?: body?.error ?: "No se pudo cerrar la solicitud.")
        } catch (e: Exception) {
            Resultado.Fallo(ErrorRed.explicar(e))
        }
    }
}
