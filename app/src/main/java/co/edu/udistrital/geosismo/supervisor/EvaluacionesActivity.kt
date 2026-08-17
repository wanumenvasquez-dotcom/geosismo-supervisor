package co.edu.udistrital.geosismo.supervisor

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AdapterView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import co.edu.udistrital.geosismo.supervisor.databinding.ActivityListaGenericaBinding
import co.edu.udistrital.geosismo.supervisor.network.model.EvaluacionAdminDto
import co.edu.udistrital.geosismo.supervisor.repository.AdminRepository
import co.edu.udistrital.geosismo.supervisor.repository.Resultado
import co.edu.udistrital.geosismo.supervisor.ui.EvaluacionAdapter
import kotlinx.coroutines.launch

class EvaluacionesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityListaGenericaBinding
    private lateinit var repo: AdminRepository
    private lateinit var adapter: EvaluacionAdapter

    private val filtros = listOf(
        "" to "Todas", "sin_revisar" to "Sin revisar", "certificada" to "Certificadas", "objetada" to "Objetadas"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityListaGenericaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.title = getString(R.string.evaluaciones_titulo)
        binding.toolbar.setNavigationOnClickListener { finish() }
        repo = AdminRepository(this)

        adapter = EvaluacionAdapter(
            onCertificar = { evaluacion, comentario -> decidir(evaluacion, "certificada", comentario) },
            onObjetar = { evaluacion, comentario -> decidir(evaluacion, "objetada", comentario) }
        )
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

    private fun decidir(evaluacion: EvaluacionAdminDto, decision: String, comentario: String) {
        lifecycleScope.launch {
            when (val resultado = repo.certificarEvaluacion(evaluacion.id, decision, comentario)) {
                is Resultado.Exito -> { Toast.makeText(this@EvaluacionesActivity, resultado.datos, Toast.LENGTH_LONG).show(); cargar() }
                is Resultado.Fallo -> Toast.makeText(this@EvaluacionesActivity, resultado.mensaje, Toast.LENGTH_LONG).show()
            }
        }
    }
}
