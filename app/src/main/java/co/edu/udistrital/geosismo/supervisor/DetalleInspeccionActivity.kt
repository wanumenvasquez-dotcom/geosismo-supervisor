package co.edu.udistrital.geosismo.supervisor

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import co.edu.udistrital.geosismo.supervisor.data.SessionManager
import co.edu.udistrital.geosismo.supervisor.databinding.ActivityDetalleInspeccionBinding
import co.edu.udistrital.geosismo.supervisor.network.model.InspeccionAdminDto
import co.edu.udistrital.geosismo.supervisor.ui.InspeccionSeleccionada

class DetalleInspeccionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetalleInspeccionBinding
    private lateinit var session: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetalleInspeccionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.toolbar.setNavigationOnClickListener { finish() }
        session = SessionManager(this)

        val insp = InspeccionSeleccionada.actual
        if (insp == null) {
            finish()
            return
        }
        binding.toolbar.title = insp.reporte_codigo
        pintar(insp)
    }

    private fun pintar(insp: InspeccionAdminDto) {
        val c = binding.contDetalle
        c.removeAllViews()

        val colorHex = when (insp.clasificacion_preliminar) { "verde" -> "#3F9142"; "rojo" -> "#C94A3C"; else -> "#D8A521" }
        val etiquetaClasif = when (insp.clasificacion_preliminar) {
            "verde" -> "🟢 VERDE — Habitable / sin daño significativo"
            "rojo" -> "🔴 ROJO — No habitable / riesgo evidente"
            else -> "🟡 AMARILLO — Acceso restringido / evaluación adicional"
        }
        agregarPill(etiquetaClasif, colorHex)
        agregarTexto("⚠️ Evaluación PRELIMINAR — no es una certificación estructural definitiva.", esMeta = true)

        agregarTitulo(insp.reporte_titulo)
        agregarTexto("Inspeccionado por ${insp.ingeniero_nombre} · ${insp.fecha_inspeccion}", esMeta = true)

        seccion("1) UBICACIÓN Y DATOS GENERALES")
        campo("ID de la edificación", insp.id_edificacion)
        campo("Dirección verificada", insp.direccion_verificada)
        campo("Firma del inspector", insp.firma_inspector)
        if (!insp.foto_general.isNullOrBlank()) {
            enlaceFoto("📷 Ver foto general de la edificación", insp.foto_general)
        }

        seccion("2) CARACTERIZACIÓN DE LA EDIFICACIÓN")
        campo("Uso", insp.uso_edificacion)
        campo("Número de pisos", insp.numero_pisos?.toString())
        campo("Sistema estructural", insp.sistema_estructural)
        campo("Edad aproximada", insp.edad_aproximada)
        campo("Tipo de cubierta", insp.tipo_cubierta)
        campo("Ocupación al momento del sismo", insp.ocupacion_momento_sismo)

        seccion("3) EVALUACIÓN EXTERIOR")
        campo("Estado general de fachada", insp.estado_fachada)
        campo("Daños estructurales visibles", insp.danos_estructurales_ext)
        campo("Daños no estructurales", insp.danos_no_estructurales_ext)
        campo("Grietas, desprendimientos, inclinaciones o colapsos", insp.grietas_desprendimientos)

        seccion("4) EVALUACIÓN INTERIOR")
        campo("¿Acceso seguro?", if (insp.acceso_seguro == "si") "Sí" else "No")
        // Inspecciones nuevas traen el desglose por elemento; las anteriores
        // a esta actualización solo tienen el campo combinado antiguo.
        if (!insp.danos_columnas.isNullOrBlank() || !insp.danos_vigas.isNullOrBlank() ||
            !insp.danos_muros.isNullOrBlank() || !insp.danos_losas.isNullOrBlank()) {
            campo("Daños en columnas", insp.danos_columnas)
            campo("Daños en vigas", insp.danos_vigas)
            campo("Daños en muros", insp.danos_muros)
            campo("Daños en losas", insp.danos_losas)
        } else {
            campo("Columnas, vigas, muros, losas", insp.estado_elementos_int)
        }
        campo("Escaleras", insp.estado_escaleras)
        campo("Daños no estructurales (interior)", insp.danos_no_estructurales_int)

        seccion("5) INDICADORES DE PELIGRO")
        indicador("Riesgo de colapso", insp.riesgo_colapso)
        indicador("Desplazamientos o inclinaciones", insp.desplazamientos_inclinaciones)
        indicador("Elementos sueltos o caída", insp.elementos_sueltos)
        indicador("Daños que comprometen la estabilidad", insp.danos_comprometen_estabilidad)
        indicador("Condiciones que impiden el ingreso", insp.condiciones_impiden_ingreso)

        seccion("7) EVIDENCIAS")
        campo("Observaciones generales", insp.observaciones_generales)
        campo("Anotaciones", insp.anotaciones)

        seccion("8) CIERRE DE INSPECCIÓN")
        campo("Recomendación preliminar", insp.recomendacion_preliminar)
    }

    private fun seccion(texto: String) {
        binding.contDetalle.addView(TextView(this).apply {
            text = texto
            setTextColor(Color.parseColor("#C17F22"))
            textSize = 14f
            setTypeface(typeface, Typeface.BOLD)
            setPadding(0, dp(20), 0, dp(8))
        })
    }

    private fun campo(etiqueta: String, valor: String?) {
        if (valor.isNullOrBlank()) return
        binding.contDetalle.addView(TextView(this).apply {
            text = etiqueta
            setTextColor(Color.parseColor("#4B5361"))
            textSize = 11f
            setTypeface(typeface, Typeface.BOLD)
            setPadding(0, dp(8), 0, 0)
        })
        binding.contDetalle.addView(TextView(this).apply {
            text = valor
            setTextColor(Color.parseColor("#171B22"))
            textSize = 14f
            setPadding(0, dp(2), 0, 0)
        })
    }

    private fun enlaceFoto(etiqueta: String, ruta: String) {
        binding.contDetalle.addView(TextView(this).apply {
            text = etiqueta
            setTextColor(Color.parseColor("#4C7EBF"))
            textSize = 13f
            setTypeface(typeface, Typeface.BOLD)
            setPadding(0, dp(10), 0, 0)
            setOnClickListener {
                val base = session.baseUrl.trimEnd('/')
                val url = "$base/${ruta.trimStart('/')}"
                try {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                } catch (e: Exception) {
                    Toast.makeText(this@DetalleInspeccionActivity, "No se pudo abrir la foto.", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun indicador(etiqueta: String, valor: String) {
        val esSi = valor == "si"
        binding.contDetalle.addView(TextView(this).apply {
            text = "${if (esSi) "⚠️" else "✓"} $etiqueta: ${if (esSi) "Sí" else "No"}"
            setTextColor(Color.parseColor(if (esSi) "#C94A3C" else "#3F9142"))
            textSize = 13f
            setPadding(0, dp(6), 0, 0)
        })
    }

    private fun agregarTitulo(texto: String) {
        binding.contDetalle.addView(TextView(this).apply {
            text = texto
            setTextColor(Color.parseColor("#171B22"))
            textSize = 18f
            setTypeface(typeface, Typeface.BOLD)
            setPadding(0, dp(10), 0, 0)
        })
    }

    private fun agregarTexto(texto: String, esMeta: Boolean = false) {
        binding.contDetalle.addView(TextView(this).apply {
            text = texto
            setTextColor(Color.parseColor(if (esMeta) "#4B5361" else "#171B22"))
            textSize = if (esMeta) 12f else 14f
            setPadding(0, dp(4), 0, 0)
        })
    }

    private fun agregarPill(texto: String, colorHex: String) {
        binding.contDetalle.addView(TextView(this).apply {
            text = "  $texto  "
            setBackgroundColor(Color.parseColor(colorHex))
            setTextColor(Color.WHITE)
            textSize = 12f
            setTypeface(typeface, Typeface.BOLD)
            setPadding(dp(6), dp(4), dp(6), dp(4))
        })
    }

    private fun dp(valor: Int): Int = (valor * resources.displayMetrics.density).toInt()
}
