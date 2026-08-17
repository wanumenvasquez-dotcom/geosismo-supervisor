package co.edu.udistrital.geosismo.supervisor

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import co.edu.udistrital.geosismo.supervisor.databinding.ActivityExportarBinding
import co.edu.udistrital.geosismo.supervisor.repository.AdminRepository
import co.edu.udistrital.geosismo.supervisor.repository.Resultado
import kotlinx.coroutines.launch

class ExportarActivity : AppCompatActivity() {

    private lateinit var binding: ActivityExportarBinding
    private lateinit var repo: AdminRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityExportarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener { finish() }
        repo = AdminRepository(this)

        binding.btnExportar.setOnClickListener { exportar() }
    }

    private fun exportar() {
        binding.progressExportar.visibility = View.VISIBLE
        binding.btnExportar.isEnabled = false
        binding.txtEstado.visibility = View.GONE

        lifecycleScope.launch {
            val resultado = repo.exportarCsv()
            binding.progressExportar.visibility = View.GONE
            binding.btnExportar.isEnabled = true

            when (resultado) {
                is Resultado.Exito -> {
                    binding.txtEstado.text = "Archivo generado: ${resultado.datos.name}"
                    binding.txtEstado.setTextColor(getColor(R.color.verde))
                    binding.txtEstado.visibility = View.VISIBLE
                    compartirArchivo(resultado.datos)
                }
                is Resultado.Fallo -> {
                    binding.txtEstado.text = resultado.mensaje
                    binding.txtEstado.setTextColor(getColor(R.color.rojo))
                    binding.txtEstado.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun compartirArchivo(archivo: java.io.File) {
        val uri: Uri = FileProvider.getUriForFile(this, "$packageName.fileprovider", archivo)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(Intent.createChooser(intent, "Compartir CSV"))
    }
}
