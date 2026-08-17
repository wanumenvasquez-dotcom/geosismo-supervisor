package co.edu.udistrital.geosismo.supervisor

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import co.edu.udistrital.geosismo.supervisor.databinding.ActivityGuiaTecnicaBinding
import java.io.File
import java.io.FileOutputStream

/**
 * Muestra o comparte la "Guía Técnica para Inspección de Edificaciones
 * después de un Sismo" (IDIGER/AIS/Alcaldía Mayor de Bogotá), incluida
 * en la app como recurso — no necesita internet para verla.
 */
class GuiaTecnicaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGuiaTecnicaBinding
    private val nombreAsset = "guia_tecnica_inspeccion.pdf"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGuiaTecnicaBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.toolbar.setNavigationOnClickListener { finish() }

        binding.btnVer.setOnClickListener { abrirPdf(paraCompartir = false) }
        binding.btnEnviar.setOnClickListener { abrirPdf(paraCompartir = true) }
    }

    /** Copia el PDF de assets a caché (assets no se puede abrir directo con FileProvider) y lo abre o comparte. */
    private fun abrirPdf(paraCompartir: Boolean) {
        try {
            val carpeta = File(cacheDir, "guias")
            if (!carpeta.exists()) carpeta.mkdirs()
            val destino = File(carpeta, nombreAsset)

            if (!destino.exists() || destino.length() == 0L) {
                assets.open(nombreAsset).use { entrada ->
                    FileOutputStream(destino).use { salida -> entrada.copyTo(salida) }
                }
            }

            val uri: Uri = FileProvider.getUriForFile(this, "$packageName.fileprovider", destino)

            val intent = if (paraCompartir) {
                Intent(Intent.ACTION_SEND).apply {
                    type = "application/pdf"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
            } else {
                Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, "application/pdf")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            }

            startActivity(if (paraCompartir) Intent.createChooser(intent, "Enviar guía técnica") else intent)
        } catch (e: Exception) {
            Toast.makeText(
                this,
                "No se pudo abrir la guía. Verifica que tengas una app para ver archivos PDF instalada.",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}
