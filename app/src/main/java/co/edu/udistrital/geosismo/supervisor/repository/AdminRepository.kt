package co.edu.udistrital.geosismo.supervisor.repository

import android.content.Context
import co.edu.udistrital.geosismo.supervisor.data.SessionManager
import co.edu.udistrital.geosismo.supervisor.network.ApiClient
import co.edu.udistrital.geosismo.supervisor.util.ErrorRed
import co.edu.udistrital.geosismo.supervisor.util.mensajeDeError
import co.edu.udistrital.geosismo.supervisor.network.model.VoluntarioPendienteDto
import co.edu.udistrital.geosismo.supervisor.network.model.SolicitudDto
import co.edu.udistrital.geosismo.supervisor.network.model.UsuarioAdminDto
import co.edu.udistrital.geosismo.supervisor.network.model.EvaluacionAdminDto
import co.edu.udistrital.geosismo.supervisor.network.model.ReporteAdminDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

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

    // ------------------------------------------------------------------
    // Gestión de usuarios (Fase 2)
    // ------------------------------------------------------------------
    suspend fun usuariosTodos(rol: String? = null): Resultado<List<UsuarioAdminDto>> = withContext(Dispatchers.IO) {
        try {
            val resp = api.usuariosTodos(rol = rol)
            val body = resp.body()
            if (resp.isSuccessful && body?.ok == true) Resultado.Exito(body.usuarios)
            else Resultado.Fallo(resp.mensajeDeError() ?: body?.error ?: "No se pudo cargar la lista de usuarios.")
        } catch (e: Exception) {
            Resultado.Fallo(ErrorRed.explicar(e))
        }
    }

    suspend fun cambiarRol(usuarioId: Int, nuevoRol: String): Resultado<String> = withContext(Dispatchers.IO) {
        try {
            val resp = api.cambiarRol(usuarioId = usuarioId, nuevoRol = nuevoRol)
            val body = resp.body()
            if (resp.isSuccessful && body?.ok == true) Resultado.Exito(body.mensaje ?: "Rol actualizado.")
            else Resultado.Fallo(resp.mensajeDeError() ?: body?.error ?: "No se pudo cambiar el rol.")
        } catch (e: Exception) {
            Resultado.Fallo(ErrorRed.explicar(e))
        }
    }

    // ------------------------------------------------------------------
    // Auditoría de evaluaciones (Fase 2)
    // ------------------------------------------------------------------
    suspend fun evaluacionesTodas(estadoAuditoria: String? = null): Resultado<List<EvaluacionAdminDto>> = withContext(Dispatchers.IO) {
        try {
            val resp = api.evaluacionesTodas(estadoAuditoria = estadoAuditoria)
            val body = resp.body()
            if (resp.isSuccessful && body?.ok == true) Resultado.Exito(body.evaluaciones)
            else Resultado.Fallo(resp.mensajeDeError() ?: body?.error ?: "No se pudieron cargar las evaluaciones.")
        } catch (e: Exception) {
            Resultado.Fallo(ErrorRed.explicar(e))
        }
    }

    suspend fun certificarEvaluacion(evaluacionId: Int, decision: String, comentario: String): Resultado<String> = withContext(Dispatchers.IO) {
        try {
            val resp = api.certificarEvaluacion(evaluacionId = evaluacionId, decision = decision, comentario = comentario)
            val body = resp.body()
            if (resp.isSuccessful && body?.ok == true) Resultado.Exito(body.mensaje ?: "Listo.")
            else Resultado.Fallo(resp.mensajeDeError() ?: body?.error ?: "No se pudo registrar la decisión.")
        } catch (e: Exception) {
            Resultado.Fallo(ErrorRed.explicar(e))
        }
    }

    // ------------------------------------------------------------------
    // Moderación de reportes (Fase 3)
    // ------------------------------------------------------------------
    suspend fun reportesTodos(estado: String? = null): Resultado<List<ReporteAdminDto>> = withContext(Dispatchers.IO) {
        try {
            val resp = api.reportesTodos(estado = estado)
            val body = resp.body()
            if (resp.isSuccessful && body?.ok == true) Resultado.Exito(body.reportes)
            else Resultado.Fallo(resp.mensajeDeError() ?: body?.error ?: "No se pudieron cargar los reportes.")
        } catch (e: Exception) {
            Resultado.Fallo(ErrorRed.explicar(e))
        }
    }

    suspend fun cambiarEstadoReporte(reporteId: Int, nuevoEstado: String): Resultado<String> = withContext(Dispatchers.IO) {
        try {
            val resp = api.cambiarEstadoReporte(reporteId = reporteId, nuevoEstado = nuevoEstado)
            val body = resp.body()
            if (resp.isSuccessful && body?.ok == true) Resultado.Exito(body.mensaje ?: "Estado actualizado.")
            else Resultado.Fallo(resp.mensajeDeError() ?: body?.error ?: "No se pudo cambiar el estado.")
        } catch (e: Exception) {
            Resultado.Fallo(ErrorRed.explicar(e))
        }
    }

    suspend fun eliminarReporte(reporteId: Int): Resultado<String> = withContext(Dispatchers.IO) {
        try {
            val resp = api.eliminarReporte(reporteId = reporteId)
            val body = resp.body()
            if (resp.isSuccessful && body?.ok == true) Resultado.Exito(body.mensaje ?: "Reporte eliminado.")
            else Resultado.Fallo(resp.mensajeDeError() ?: body?.error ?: "No se pudo eliminar el reporte.")
        } catch (e: Exception) {
            Resultado.Fallo(ErrorRed.explicar(e))
        }
    }

    // ------------------------------------------------------------------
    // Exportar CSV (Fase 3) — descarga el archivo al almacenamiento
    // propio de la app y devuelve la ruta local del archivo guardado.
    // ------------------------------------------------------------------
    suspend fun exportarCsv(): Resultado<File> = withContext(Dispatchers.IO) {
        try {
            val resp = api.exportarCsv()
            if (!resp.isSuccessful) {
                return@withContext Resultado.Fallo(resp.mensajeDeError() ?: "No se pudo generar el archivo (código ${resp.code()}).")
            }
            val cuerpo = resp.body() ?: return@withContext Resultado.Fallo("El servidor no devolvió ningún archivo.")

            val carpeta = File(context.getExternalFilesDir(null), "exportados")
            if (!carpeta.exists()) carpeta.mkdirs()
            val nombreArchivo = "geosismo_reportes_${System.currentTimeMillis()}.csv"
            val destino = File(carpeta, nombreArchivo)

            cuerpo.byteStream().use { entrada ->
                FileOutputStream(destino).use { salida -> entrada.copyTo(salida) }
            }
            Resultado.Exito(destino)
        } catch (e: Exception) {
            Resultado.Fallo(ErrorRed.explicar(e))
        }
    }

    // ------------------------------------------------------------------
    // Centro de datos
    // ------------------------------------------------------------------
    suspend fun estadisticas(): Resultado<co.edu.udistrital.geosismo.supervisor.network.model.EstadisticasResponse> = withContext(Dispatchers.IO) {
        try {
            val resp = api.estadisticas()
            val body = resp.body()
            if (resp.isSuccessful && body?.ok == true) Resultado.Exito(body)
            else Resultado.Fallo("No se pudieron cargar las estadísticas.")
        } catch (e: Exception) {
            Resultado.Fallo(ErrorRed.explicar(e))
        }
    }

    // ------------------------------------------------------------------
    // Inspecciones técnicas estructuradas (protocolo de 8 pasos)
    // ------------------------------------------------------------------
    suspend fun todasLasInspecciones(): Resultado<List<co.edu.udistrital.geosismo.supervisor.network.model.InspeccionAdminDto>> = withContext(Dispatchers.IO) {
        try {
            val resp = api.todasLasInspecciones()
            val body = resp.body()
            if (resp.isSuccessful && body?.ok == true) Resultado.Exito(body.inspecciones)
            else Resultado.Fallo(resp.mensajeDeError() ?: body?.error ?: "No se pudieron cargar las inspecciones.")
        } catch (e: Exception) {
            Resultado.Fallo(ErrorRed.explicar(e))
        }
    }
}
