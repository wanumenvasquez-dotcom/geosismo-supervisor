package co.edu.udistrital.geosismo.supervisor

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import co.edu.udistrital.geosismo.supervisor.data.SessionManager
import co.edu.udistrital.geosismo.supervisor.databinding.ActivityHomeBinding
import co.edu.udistrital.geosismo.supervisor.network.ApiClient
import kotlinx.coroutines.launch

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        val session = SessionManager(this)
        if (!session.estaLogueado || !session.esAdmin) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        binding.cardVoluntarios.setOnClickListener {
            startActivity(Intent(this, VoluntariosPendientesActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        cargarConteo()
    }

    private fun cargarConteo() {
        lifecycleScope.launch {
            try {
                val resp = ApiClient.getApiService(this@HomeActivity).conteoPendientes()
                val body = resp.body()
                if (resp.isSuccessful && body?.ok == true && body.pendientes > 0) {
                    binding.badgePendientes.text = body.pendientes.toString()
                    binding.badgePendientes.visibility = android.view.View.VISIBLE
                } else {
                    binding.badgePendientes.visibility = android.view.View.GONE
                }
            } catch (e: Exception) {
                binding.badgePendientes.visibility = android.view.View.GONE
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.menuAjustes) {
            startActivity(Intent(this, SettingsActivity::class.java))
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
