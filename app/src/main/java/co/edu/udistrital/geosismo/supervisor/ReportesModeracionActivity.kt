package co.edu.udistrital.geosismo.supervisor

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AdapterView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import co.edu.udistrital.geosismo.supervisor.databinding.ActivityListaGenericaBinding
import co.edu.udistrital.geosismo.supervisor.network.model.ReporteAdminDto
import co.edu.udistrital.geosismo.supervisor.repository.AdminRepository
import co.edu.udistrital.geosismo.supervisor.repository.Resultado
import co.edu.udistrital.geosismo.supervisor.ui.ReporteModAdapter
import kotlinx.coroutines.launch

class ReportesModeracionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityListaGenericaBinding
    private lateinit var repo: AdminRepository
    private lateinit var adapter: ReporteModAdapter

    private val filtros = listOf(
        "" to "Todos los estados", "nuevo" to "Nuevo", "en_revision" to "En revisión",
        "evaluado" to "Evaluado", "cerrado" to "Cerrado"
    )
    private val estadosCambio = listOf(
        "nuevo" to "Nuevo", "en_revision" to "En revisión", "evaluado" to "Evaluado", "cerrado" to "Cerrado"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityListaGenericaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.title = getString(R.string.reportes_mod_titulo)
        binding.toolbar.setNavigationOnClickListener { finish() }
        repo = AdminRepository(this)

        adapter = ReporteModAdapter { reporte -> mostrarOpciones(reporte) }
        binding.recyclerLista.layoutManager = LinearLayoutManager(this)
        binding.recyclerLista.adapter = adapter
        binding.txtVacio.text = getString(R.string.reportes_mod_vacio)

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
            when (val resultado = repo.reportesTodos(filtro)) {
                is Resultado.Exito -> {
                    adapter.submitList(resultado.datos)
                    binding.txtVacio.text = getString(R.string.reportes_mod_vacio)
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

    private fun mostrarOpciones(reporte: ReporteAdminDto) {
        val opciones = arrayOf("Cambiar estado", getString(R.string.reportes_mod_eliminar))
        AlertDialog.Builder(this)
            .setTitle("${reporte.codigo} — ${reporte.titulo}")
            .setItems(opciones) { _, which ->
                if (which == 0) mostrarCambioEstado(reporte) else confirmarEliminar(reporte)
            }
            .setNegativeButton(getString(R.string.cancelar), null)
            .show()
    }

    private fun mostrarCambioEstado(reporte: ReporteAdminDto) {
        val opciones = estadosCambio.map { it.second }.toTypedArray()
        AlertDialog.Builder(this)
            .setTitle("Nuevo estado para ${reporte.codigo}")
            .setItems(opciones) { _, which ->
                val (nuevoEstado, _) = estadosCambio[which]
                lifecycleScope.launch {
                    when (val resultado = repo.cambiarEstadoReporte(reporte.id, nuevoEstado)) {
                        is Resultado.Exito -> { Toast.makeText(this@ReportesModeracionActivity, resultado.datos, Toast.LENGTH_LONG).show(); cargar() }
                        is Resultado.Fallo -> Toast.makeText(this@ReportesModeracionActivity, resultado.mensaje, Toast.LENGTH_LONG).show()
                    }
                }
            }
            .setNegativeButton(getString(R.string.cancelar), null)
            .show()
    }

    private fun confirmarEliminar(reporte: ReporteAdminDto) {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.reportes_mod_eliminar))
            .setMessage(getString(R.string.reportes_mod_confirmar_eliminar, reporte.codigo))
            .setPositiveButton(getString(R.string.confirmar)) { _, _ ->
                lifecycleScope.launch {
                    when (val resultado = repo.eliminarReporte(reporte.id)) {
                        is Resultado.Exito -> { Toast.makeText(this@ReportesModeracionActivity, resultado.datos, Toast.LENGTH_LONG).show(); cargar() }
                        is Resultado.Fallo -> Toast.makeText(this@ReportesModeracionActivity, resultado.mensaje, Toast.LENGTH_LONG).show()
                    }
                }
            }
            .setNegativeButton(getString(R.string.cancelar), null)
            .show()
    }
}
