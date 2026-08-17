package co.edu.udistrital.geosismo.supervisor

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AdapterView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import co.edu.udistrital.geosismo.supervisor.databinding.ActivityListaGenericaBinding
import co.edu.udistrital.geosismo.supervisor.network.model.UsuarioAdminDto
import co.edu.udistrital.geosismo.supervisor.repository.AdminRepository
import co.edu.udistrital.geosismo.supervisor.repository.Resultado
import co.edu.udistrital.geosismo.supervisor.ui.UsuarioAdapter
import kotlinx.coroutines.launch
import java.io.File

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

        adapter = UsuarioAdapter { usuario -> mostrarOpcionesUsuario(usuario) }
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

    /** Menú al tocar un usuario: cambiar rol, o iniciar el flujo de eliminación segura. */
    private fun mostrarOpcionesUsuario(usuario: UsuarioAdminDto) {
        val opciones = roles.map { "Cambiar rol a ${it.second}" }.toMutableList()
        opciones.add("🗑️  Eliminar usuario…")
        AlertDialog.Builder(this)
            .setTitle(usuario.nombre)
            .setItems(opciones.toTypedArray()) { _, which ->
                if (which < roles.size) {
                    val (nuevoRol, nuevoRolLabel) = roles[which]
                    if (nuevoRol == usuario.rol) return@setItems
                    confirmarCambioRol(usuario, nuevoRol, nuevoRolLabel)
                } else {
                    iniciarEliminacionSegura(usuario)
                }
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

    // ------------------------------------------------------------------
    // Eliminación segura: PASO 1 (explicación) → PASO 2 (exportar y
    // compartir el zip) → PASO 3 (confirmación final, ya con el
    // respaldo en la mano del administrador) → eliminación real.
    // No hay forma de saltarse el paso de exportar desde esta pantalla.
    // ------------------------------------------------------------------

    private fun iniciarEliminacionSegura(usuario: UsuarioAdminDto) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar a ${usuario.nombre}")
            .setMessage(
                "Antes de eliminar esta cuenta, vas a generar un archivo .zip con " +
                "todo lo que ha hecho: sus reportes, fotos, evaluaciones e inspecciones. " +
                "Podrás enviarlo por WhatsApp, correo o guardarlo donde quieras.\n\n" +
                "Solo después de tener ese respaldo podrás confirmar la eliminación."
            )
            .setPositiveButton("Exportar datos") { _, _ -> exportarYCompartir(usuario) }
            .setNegativeButton(getString(R.string.cancelar), null)
            .show()
    }

    private fun exportarYCompartir(usuario: UsuarioAdminDto) {
        val progreso = AlertDialog.Builder(this)
            .setMessage("Generando respaldo de ${usuario.nombre}…")
            .setCancelable(false)
            .show()

        lifecycleScope.launch {
            val resultado = repo.exportarUsuario(usuario.id, usuario.nombre)
            progreso.dismiss()

            when (resultado) {
                is Resultado.Exito -> {
                    compartirZip(resultado.datos)
                    // Solo después de generar (y ofrecer compartir) el
                    // respaldo se habilita la confirmación final de borrado.
                    mostrarConfirmacionFinal(usuario, resultado.datos)
                }
                is Resultado.Fallo -> {
                    Toast.makeText(this@UsuariosActivity, resultado.mensaje, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun compartirZip(archivo: File) {
        val uri: Uri = FileProvider.getUriForFile(this, "$packageName.fileprovider", archivo)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/zip"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(Intent.createChooser(intent, "Enviar respaldo del usuario"))
    }

    private fun mostrarConfirmacionFinal(usuario: UsuarioAdminDto, respaldo: File) {
        AlertDialog.Builder(this)
            .setTitle("¿Eliminar a ${usuario.nombre} definitivamente?")
            .setMessage(
                "El respaldo ya está guardado en:\n${respaldo.name}\n\n" +
                "Esta acción no se puede deshacer. Se borrarán todas sus fotos del " +
                "servidor. Si algún reporte suyo tiene aportes de otras personas " +
                "(fotos o evaluaciones), ese reporte se conserva y se reasigna a " +
                "\"Usuario eliminado\" — nadie más pierde su trabajo."
            )
            .setPositiveButton("Eliminar definitivamente") { _, _ -> confirmarEliminacionDefinitiva(usuario) }
            .setNegativeButton(getString(R.string.cancelar), null)
            .show()
    }

    private fun confirmarEliminacionDefinitiva(usuario: UsuarioAdminDto) {
        lifecycleScope.launch {
            when (val resultado = repo.eliminarUsuarioSeguro(usuario.id)) {
                is Resultado.Exito -> {
                    Toast.makeText(this@UsuariosActivity, resultado.datos, Toast.LENGTH_LONG).show()
                    cargar()
                }
                is Resultado.Fallo -> Toast.makeText(this@UsuariosActivity, resultado.mensaje, Toast.LENGTH_LONG).show()
            }
        }
    }
}

