package co.edu.udistrital.geosismo.supervisor

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import co.edu.udistrital.geosismo.supervisor.data.SessionManager
import co.edu.udistrital.geosismo.supervisor.databinding.ActivityLoginBinding
import co.edu.udistrital.geosismo.supervisor.repository.AdminRepository
import co.edu.udistrital.geosismo.supervisor.repository.ResultadoLogin
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var session: SessionManager
    private lateinit var repo: AdminRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        session = SessionManager(this)
        repo = AdminRepository(this)

        // El servidor ya no se pregunta: queda fijo en la URL de producción.
        if (session.baseUrl.isBlank()) {
            session.baseUrl = BuildConfig.DEFAULT_BASE_URL
        }

        if (session.estaLogueado && session.esAdmin) {
            irAHome()
            return
        }

        binding.btnLogin.setOnClickListener { intentarLogin() }
    }

    private fun intentarLogin() {
        val email = binding.inputEmail.text.toString().trim()
        val password = binding.inputPassword.text.toString()

        ocultarError()

        if (email.isBlank() || password.isBlank()) {
            mostrarError("Completa correo y contraseña.")
            return
        }

        // Sin chequeo previo de "hay internet": esa validación de Android
        // puede dar falso negativo. Se intenta el login directamente y se
        // muestra el error real si la conexión de verdad falla.
        mostrarCargando(true)

        lifecycleScope.launch {
            when (val resultado = repo.login(email, password)) {
                is ResultadoLogin.Exito -> {
                    mostrarCargando(false)
                    irAHome()
                }
                is ResultadoLogin.Fallo -> {
                    mostrarCargando(false)
                    mostrarError(resultado.mensaje)
                }
            }
        }
    }

    private fun irAHome() {
        startActivity(Intent(this, HomeActivity::class.java))
        finish()
    }

    private fun mostrarCargando(cargando: Boolean) {
        binding.progressLogin.visibility = if (cargando) android.view.View.VISIBLE else android.view.View.GONE
        binding.btnLogin.isEnabled = !cargando
    }

    private fun mostrarError(mensaje: String) {
        binding.txtError.text = mensaje
        binding.txtError.visibility = android.view.View.VISIBLE
    }

    private fun ocultarError() {
        binding.txtError.visibility = android.view.View.GONE
    }
}
