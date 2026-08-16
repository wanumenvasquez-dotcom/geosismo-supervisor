package co.edu.udistrital.geosismo.supervisor

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import co.edu.udistrital.geosismo.supervisor.data.SessionManager
import co.edu.udistrital.geosismo.supervisor.databinding.ActivitySolicitudesBinding
import co.edu.udistrital.geosismo.supervisor.network.model.SolicitudDto
import co.edu.udistrital.geosismo.supervisor.repository.AdminRepository
import co.edu.udistrital.geosismo.supervisor.repository.Resultado
import co.edu.udistrital.geosismo.supervisor.ui.SolicitudAdapter
import kotlinx.coroutines.launch

class SolicitudesActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySolicitudesBinding
    private lateinit var repo: AdminRepository
    private lateinit var session: SessionManager
    private lateinit var adapter: SolicitudAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySolicitudesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener { finish() }
        repo = AdminRepository(this)
        session = SessionManager(this)

        adapter = SolicitudAdapter(
            onResponder = { solicitud, respuesta -> responder(solicitud, respuesta) },
            onCerrar = { solicitud -> cerrar(solicitud) },
            onVerAdjunto = { solicitud -> verAdjunto(solicitud) }
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
            when (val resultado = repo.todasLasSolicitudes()) {
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

    private fun responder(solicitud: SolicitudDto, respuesta: String) {
        lifecycleScope.launch {
            when (val resultado = repo.responderSolicitud(solicitud.id, respuesta)) {
                is Resultado.Exito -> { Toast.makeText(this@SolicitudesActivity, resultado.datos, Toast.LENGTH_LONG).show(); cargar() }
                is Resultado.Fallo -> Toast.makeText(this@SolicitudesActivity, resultado.mensaje, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun cerrar(solicitud: SolicitudDto) {
        lifecycleScope.launch {
            when (val resultado = repo.cerrarSolicitud(solicitud.id)) {
                is Resultado.Exito -> { Toast.makeText(this@SolicitudesActivity, resultado.datos, Toast.LENGTH_LONG).show(); cargar() }
                is Resultado.Fallo -> Toast.makeText(this@SolicitudesActivity, resultado.mensaje, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun verAdjunto(solicitud: SolicitudDto) {
        val ruta = solicitud.archivo_adjunto ?: return
        val base = session.baseUrl.trimEnd('/')
        val url = "$base/${ruta.trimStart('/')}"
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        } catch (e: Exception) {
            Toast.makeText(this, "No se pudo abrir el archivo.", Toast.LENGTH_SHORT).show()
        }
    }
}
