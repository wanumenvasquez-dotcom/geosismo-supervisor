package co.edu.udistrital.geosismo.supervisor.data

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("geosismo_supervisor_sesion", Context.MODE_PRIVATE)

    var baseUrl: String
        get() = prefs.getString(KEY_BASE_URL, "") ?: ""
        set(value) = prefs.edit().putString(KEY_BASE_URL, value.trim()).apply()

    var cookie: String?
        get() = prefs.getString(KEY_COOKIE, null)
        set(value) = prefs.edit().putString(KEY_COOKIE, value).apply()

    var nombreUsuario: String?
        get() = prefs.getString(KEY_NOMBRE, null)
        set(value) = prefs.edit().putString(KEY_NOMBRE, value).apply()

    var rolUsuario: String?
        get() = prefs.getString(KEY_ROL, null)
        set(value) = prefs.edit().putString(KEY_ROL, value).apply()

    val estaLogueado: Boolean
        get() = !cookie.isNullOrBlank()

    val esAdmin: Boolean
        get() = rolUsuario == "admin"

    fun cerrarSesion() {
        prefs.edit()
            .remove(KEY_COOKIE)
            .remove(KEY_NOMBRE)
            .remove(KEY_ROL)
            .apply()
    }

    companion object {
        private const val KEY_BASE_URL = "base_url"
        private const val KEY_COOKIE = "cookie"
        private const val KEY_NOMBRE = "nombre_usuario"
        private const val KEY_ROL = "rol_usuario"
    }
}
