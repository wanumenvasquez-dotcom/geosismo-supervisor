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
import co.edu.udistrital.geosismo.supervisor.network.model.UsuarioAdminDto
import co.edu.udistrital.geosismo.supervisor.repository.AdminRepository
import co.edu.udistrital.geosismo.supervisor.repository.Resultado
import co.edu.udistrital.geosismo.supervisor.ui.UsuarioAdapter
import kotlinx.coroutines.launch

class UsuariosActivity : AppCompatActivity() {

    private lateinit var binding: ActivityListaGenericaBinding
    private lateinit var repo: AdminRepository
    private lateinit var adapter: UsuarioAdapter

    private val filtros = listOf("" to "Todos los roles", "ciudadano" to "Ciudadanos", "ingeniero" to "Ingenieros", "admin" to "Administradores")
    private val roles = listOf("ciudadano" to "Ciudadano", "ingeniero" to "Ingeniero", "admin" to "Administrador")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityListaGenericaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.title = getString(R.string.usuarios_titulo)
        binding.toolbar.setNavigationOnClickListener { finish() }
        repo = AdminRepository(this)

        adapter = UsuarioAdapter { usuario -> mostrarOpcionesRol(usuario) }
        binding.recyclerLista.layoutManager = LinearLayoutManager(this)
        binding.recyclerLista.adapter = adapter
        binding.txtVacio.text = getString(R.string.usuarios_vacio)

        binding.spinnerFiltro.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, filtros.map { it.second })
        binding.spinnerFiltro.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p: AdapterView<*>?, v: View?, pos: Int, id: Long) = cargar()
            override fun onNothingSelected(p: AdapterView<*>?) {}
        }
        binding.swipeRefresh.setOnRefreshListener { cargar() }

        cargar()
    }

    private fun cargar() {
        val rolFiltro = filtros[binding.spinnerFiltro.selectedItemPosition].first.ifBlank { null }
        binding.progressLista.visibility = View.VISIBLE
        binding.txtVacio.visibility = View.GONE
        lifecycleScope.launch {
            when (val resultado = repo.usuariosTodos(rolFiltro)) {
                is Resultado.Exito -> {
                    adapter.submitList(resultado.datos)
                    binding.txtVacio.text = getString(R.string.usuarios_vacio)
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

    private fun mostrarOpcionesRol(usuario: UsuarioAdminDto) {
        val opciones = roles.map { it.second }.toTypedArray()
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.usuarios_cambiar_rol) + ": ${usuario.nombre}")
            .setItems(opciones) { _, which ->
                val (nuevoRol, nuevoRolLabel) = roles[which]
                if (nuevoRol == usuario.rol) return@setItems
                confirmarCambioRol(usuario, nuevoRol, nuevoRolLabel)
            }
            .setNegativeButton(getString(R.string.cancelar), null)
            .show()
    }

    private fun confirmarCambioRol(usuario: UsuarioAdminDto, nuevoRol: String, nuevoRolLabel: String) {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.usuarios_cambiar_rol))
            .setMessage(getString(R.string.usuarios_confirmar_cambio, usuario.nombre, nuevoRolLabel))
            .setPositiveButton(getString(R.string.confirmar)) { _, _ ->
                lifecycleScope.launch {
                    when (val resultado = repo.cambiarRol(usuario.id, nuevoRol)) {
                        is Resultado.Exito -> { Toast.makeText(this@UsuariosActivity, resultado.datos, Toast.LENGTH_LONG).show(); cargar() }
                        is Resultado.Fallo -> Toast.makeText(this@UsuariosActivity, resultado.mensaje, Toast.LENGTH_LONG).show()
                    }
                }
            }
            .setNegativeButton(getString(R.string.cancelar), null)
            .show()
    }
}
