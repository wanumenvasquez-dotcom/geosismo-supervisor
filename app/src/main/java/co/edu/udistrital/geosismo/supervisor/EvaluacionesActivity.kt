package co.edu.udistrital.geosismo.supervisor

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AdapterView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import co.edu.udistrital.geosismo.supervisor.databinding.ActivityListaGenericaBinding
import co.edu.udistrital.geosismo.supervisor.repository.AdminRepository
import co.edu.udistrital.geosismo.supervisor.repository.Resultado
import co.edu.udistrital.geosismo.supervisor.ui.EvaluacionAdapter
import kotlinx.coroutines.launch

class EvaluacionesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityListaGenericaBinding
    private lateinit var repo: AdminRepository
    private lateinit var adapter: EvaluacionAdapter

    private val filtros = listOf(
        "" to "Todas", "sin_revisar" to "Sin revisar", "aplazada" to "Aplazadas",
        "certificada" to "Certificadas", "objetada" to "Objetadas"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityListaGenericaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.title = getString(R.string.evaluaciones_titulo)
        binding.toolbar.setNavigationOnClickListener { finish() }
        repo = AdminRepository(this)

        adapter = EvaluacionAdapter { evaluacion ->
            startActivity(Intent(this, DetalleEvaluacionActivity::class.java).putExtra("evaluacion_id", evaluacion.id))
        }
        binding.recyclerLista.layoutManager = LinearLayoutManager(this)
        binding.recyclerLista.adapter = adapter
        binding.txtVacio.text = getString(R.string.evaluaciones_vacio)

        binding.spinnerFiltro.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, filtros.map { it.second })
        binding.spinnerFiltro.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p: AdapterView<*>?, v: View?, pos: Int, id: Long) = cargar()
            override fun onNothingSelected(p: AdapterView<*>?) {}
        }
        binding.swipeRefresh.setOnRefreshListener { cargar() }

        cargar()
    }

    override fun onResume() {
        super.onResume()
        // Vuelve a cargar por si se tomó una decisión en la pantalla de detalle
        if (::adapter.isInitialized && adapter.currentList.isNotEmpty()) cargar()
    }

    private fun cargar() {
        val filtro = filtros[binding.spinnerFiltro.selectedItemPosition].first.ifBlank { null }
        binding.progressLista.visibility = View.VISIBLE
        binding.txtVacio.visibility = View.GONE
        lifecycleScope.launch {
            when (val resultado = repo.evaluacionesTodas(filtro)) {
                is Resultado.Exito -> {
                    adapter.submitList(resultado.datos)
                    binding.txtVacio.text = getString(R.string.evaluaciones_vacio)
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
