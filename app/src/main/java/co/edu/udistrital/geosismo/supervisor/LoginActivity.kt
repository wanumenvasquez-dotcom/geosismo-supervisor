package co.edu.udistrital.geosismo.supervisor

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import co.edu.udistrital.geosismo.supervisor.data.SessionManager
import co.edu.udistrital.geosismo.supervisor.databinding.ActivityLoginBinding
import co.edu.udistrital.geosismo.supervisor.repository.AdminRepository
import co.edu.udistrital.geosismo.supervisor.repository.ResultadoLogin
import co.edu.udistrital.geosismo.supervisor.util.ConnectivityUtil
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

        if (session.estaLogueado && session.esAdmin) {
            irAHome()
            return
        }

        binding.inputServidor.setText(
            session.baseUrl.ifBlank { BuildConfig.DEFAULT_BASE_URL }
        )

        binding.btnLogin.setOnClickListener { intentarLogin() }
    }

    private fun intentarLogin() {
        val servidor = binding.inputServidor.text.toString().trim()
        val email = binding.inputEmail.text.toString().trim()
        val password = binding.inputPassword.text.toString()

        ocultarError()

        if (servidor.isBlank() || email.isBlank() || password.isBlank()) {
            mostrarError("Completa la URL del servidor, correo y contraseña.")
            return
        }
        if (!ConnectivityUtil.hayInternet(this)) {
            mostrarError(getString(R.string.login_sin_internet))
            return
        }

        session.baseUrl = servidor
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
