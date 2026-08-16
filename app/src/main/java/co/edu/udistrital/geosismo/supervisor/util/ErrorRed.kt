package co.edu.udistrital.geosismo.supervisor.util

import retrofit2.Response
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLException

/**
 * Convierte una excepción de red en un mensaje ESPECÍFICO y accionable,
 * en vez del genérico "sin conexión" que podía significar cualquier cosa.
 */
object ErrorRed {
    fun explicar(e: Exception): String = when (e) {
        is UnknownHostException ->
            "No se pudo encontrar el servidor. Revisa que la URL sea correcta y que el dominio exista."
        is SSLException ->
            "El servidor tiene un problema de certificado SSL/HTTPS. Contacta a quien administra el hosting."
        is SocketTimeoutException ->
            "El servidor tardó demasiado en responder (tiempo de espera agotado)."
        is ConnectException ->
            "No se pudo establecer conexión con el servidor (puerto cerrado o servidor caído)."
        else ->
            "Error de red inesperado: ${e.javaClass.simpleName}${e.message?.let { " — $it" } ?: ""}"
    }
}

/**
 * Retrofit deja `response.body()` en null para CUALQUIER código fuera de
 * 200-299, aunque el servidor sí haya mandado un JSON válido con el
 * motivo. Esta función lee el contenido real desde `errorBody()`.
 */
fun <T> Response<T>.mensajeDeError(): String? {
    return try {
        val texto = errorBody()?.string() ?: return null
        val json = com.google.gson.JsonParser.parseString(texto).asJsonObject
        if (json.has("error") && !json.get("error").isJsonNull) json.get("error").asString else null
    } catch (e: Exception) {
        null
    }
}
