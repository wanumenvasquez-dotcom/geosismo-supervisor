package co.edu.udistrital.geosismo.supervisor.network

import android.content.Context
import co.edu.udistrital.geosismo.supervisor.data.SessionManager
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

private class SessionCookieJar(private val session: SessionManager) : CookieJar {

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        val phpSession = cookies.firstOrNull { it.name == "PHPSESSID" }
        if (phpSession != null) {
            session.cookie = "${phpSession.name}=${phpSession.value}"
        }
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        val raw = session.cookie ?: return emptyList()
        val partes = raw.split("=", limit = 2)
        if (partes.size != 2) return emptyList()
        val cookie = Cookie.Builder()
            .name(partes[0])
            .value(partes[1])
            .domain(url.host)
            .path("/")
            .build()
        return listOf(cookie)
    }
}

object ApiClient {

    @Volatile
    private var retrofit: Retrofit? = null

    @Volatile
    private var baseUrlActual: String? = null

    fun getApiService(context: Context): ApiService {
        val session = SessionManager(context)
        var url = session.baseUrl
        if (url.isBlank()) url = "https://tudominio.com/"
        if (!url.endsWith("/")) url += "/"

        if (retrofit == null || baseUrlActual != url) {
            synchronized(this) {
                if (retrofit == null || baseUrlActual != url) {
                    retrofit = construir(context, url)
                    baseUrlActual = url
                }
            }
        }
        return retrofit!!.create(ApiService::class.java)
    }

    private fun construir(context: Context, baseUrl: String): Retrofit {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        val client = OkHttpClient.Builder()
            .cookieJar(SessionCookieJar(SessionManager(context)))
            .addInterceptor(logging)
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}
