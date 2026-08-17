package co.edu.udistrital.geosismo.supervisor

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import co.edu.udistrital.geosismo.supervisor.databinding.ActivityListaGenericaBinding
import co.edu.udistrital.geosismo.supervisor.repository.AdminRepository
import co.edu.udistrital.geosismo.supervisor.repository.Resultado
import co.edu.udistrital.geosismo.supervisor.ui.InspeccionAdapter
import co.edu.udistrital.geosismo.supervisor.ui.InspeccionSeleccionada
import kotlinx.coroutines.launch

class InspeccionesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityListaGenericaBinding
    private lateinit var repo: AdminRepository
    private lateinit var adapter: InspeccionAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityListaGenericaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.title = getString(R.string.inspecciones_titulo)
        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.spinnerFiltro.visibility = View.GONE
        repo = AdminRepository(this)

        adapter = InspeccionAdapter { inspeccion ->
            InspeccionSeleccionada.actual = inspeccion
            startActivity(Intent(this, DetalleInspeccionActivity::class.java))
        }
        binding.recyclerLista.layoutManager = LinearLayoutManager(this)
        binding.recyclerLista.adapter = adapter
        binding.txtVacio.text = getString(R.string.inspecciones_vacio)
        binding.swipeRefresh.setOnRefreshListener { cargar() }

        cargar()
    }

    private fun cargar() {
        binding.progressLista.visibility = View.VISIBLE
        binding.txtVacio.visibility = View.GONE
        lifecycleScope.launch {
            when (val resultado = repo.todasLasInspecciones()) {
                is Resultado.Exito -> {
                    adapter.submitList(resultado.datos)
                    binding.txtVacio.text = getString(R.string.inspecciones_vacio)
                    binding.txtVacio.visibility = if (resultado.datos.isEmpty()) View.VISIBLE else View.GONE
                }
                is Resultado.Fallo -> {
                    binding.txtVacio.text = resultado.mensaje
                    binding.txtVacio.visibility = View.VISIBLE
                }
            }
            binding.progressLista.visibility = View.GONE
            binding.swipeRefresh.isRefreshing = false
        }
    }
}
