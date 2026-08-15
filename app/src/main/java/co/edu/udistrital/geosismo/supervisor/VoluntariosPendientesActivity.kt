package co.edu.udistrital.geosismo.supervisor

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import co.edu.udistrital.geosismo.supervisor.databinding.ActivityVoluntariosPendientesBinding
import co.edu.udistrital.geosismo.supervisor.network.model.VoluntarioPendienteDto
import co.edu.udistrital.geosismo.supervisor.repository.AdminRepository
import co.edu.udistrital.geosismo.supervisor.repository.Resultado
import co.edu.udistrital.geosismo.supervisor.ui.VoluntarioPendienteAdapter
import kotlinx.coroutines.launch

class VoluntariosPendientesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVoluntariosPendientesBinding
    private lateinit var repo: AdminRepository
    private lateinit var adapter: VoluntarioPendienteAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVoluntariosPendientesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener { finish() }
        repo = AdminRepository(this)

        adapter = VoluntarioPendienteAdapter(
            onAprobar = { confirmarDecision(it, "aprobado") },
            onRechazar = { confirmarDecision(it, "rechazado") }
        )
        binding.recyclerLista.layoutManager = LinearLayoutManager(this)
        binding.recyclerLista.adapter = adapter

        binding.swipeRefresh.setOnRefreshListener { cargar() }

        cargar()
    }

    private fun cargar() {
        binding.progressLista.visibility = View.VISIBLE
        binding.txtVacio.visibility = View.GONE
        lifecycleScope.launch {
            when (val resultado = repo.voluntariosPendientes()) {
                is Resultado.Exito -> {
                    adapter.submitList(resultado.datos)
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

    private fun confirmarDecision(voluntario: VoluntarioPendienteDto, decision: String) {
        val mensaje = if (decision == "aprobado")
            getString(R.string.voluntarios_confirmar_aprobar, voluntario.nombre)
        else
            getString(R.string.voluntarios_confirmar_rechazar, voluntario.nombre)

        AlertDialog.Builder(this)
            .setTitle(if (decision == "aprobado") "Aprobar postulación" else "Rechazar postulación")
            .setMessage(mensaje)
            .setPositiveButton(getString(R.string.confirmar)) { _, _ -> ejecutarDecision(voluntario, decision) }
            .setNegativeButton(getString(R.string.cancelar), null)
            .show()
    }

    private fun ejecutarDecision(voluntario: VoluntarioPendienteDto, decision: String) {
        lifecycleScope.launch {
            when (val resultado = repo.resolverVoluntario(voluntario.id, decision)) {
                is Resultado.Exito -> {
                    Toast.makeText(this@VoluntariosPendientesActivity, resultado.datos, Toast.LENGTH_LONG).show()
                    cargar()
                }
                is Resultado.Fallo -> {
                    Toast.makeText(this@VoluntariosPendientesActivity, resultado.mensaje, Toast.LENGTH_LONG).show()
                    cargar() // por si otro admin ya la resolvió, refresca la lista igual
                }
            }
        }
    }
}
