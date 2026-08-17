package co.edu.udistrital.geosismo.supervisor

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import co.edu.udistrital.geosismo.supervisor.data.SessionManager
import co.edu.udistrital.geosismo.supervisor.databinding.ActivitySettingsBinding
import co.edu.udistrital.geosismo.supervisor.repository.AdminRepository
import kotlinx.coroutines.launch

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val session = SessionManager(this)
        val repo = AdminRepository(this)
        binding.toolbar.setNavigationOnClickListener { finish() }

        binding.txtUsuario.text = "${session.nombreUsuario ?: "—"} · ${session.rolUsuario ?: ""}"
        binding.txtServidor.text = session.baseUrl

        binding.btnCerrarSesion.setOnClickListener {
            binding.btnCerrarSesion.isEnabled = false
            lifecycleScope.launch {
                repo.logout()
                session.cerrarSesion()
                val intent = Intent(this@SettingsActivity, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
        }
    }
}
