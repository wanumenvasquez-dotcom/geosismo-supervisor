package co.edu.udistrital.geosismo.supervisor

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import co.edu.udistrital.geosismo.supervisor.data.SessionManager
import co.edu.udistrital.geosismo.supervisor.databinding.ActivityDetalleEvaluacionBinding
import co.edu.udistrital.geosismo.supervisor.network.model.DetalleEvaluacionResponse
import co.edu.udistrital.geosismo.supervisor.network.model.InspeccionAdminDto
import co.edu.udistrital.geosismo.supervisor.repository.AdminRepository
import co.edu.udistrital.geosismo.supervisor.repository.Resultado
import kotlinx.coroutines.launch

/**
 * Muestra el trabajo COMPLETO de una evaluación (todas las fotos del
 * reporte, y la inspección técnica de 8 pasos si el ingeniero la hizo)
 * antes de permitir certificar, aplazar u objetar. Los botones de
 * decisión están ocultos hasta que la persona termina de revisar todo
 * el contenido — no es posible aprobar "a ciegas".
 */
class DetalleEvaluacionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetalleEvaluacionBinding
    private lateinit var repo: AdminRepository
    private lateinit var session: SessionManager
    private var evaluacionId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetalleEvaluacionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.toolbar.setNavigationOnClickListener { finish() }
        repo = AdminRepository(this)
        session = SessionManager(this)

        evaluacionId = intent.getIntExtra("evaluacion_id", 0)
        if (evaluacionId == 0) { finish(); return }

        cargar()
    }

    private fun cargar() {
        binding.progressCarga.visibility = View.VISIBLE
        lifecycleScope.launch {
            when (val resultado = repo.evaluacionDetalle(evaluacionId)) {
                is Resultado.Exito -> pintar(resultado.datos)
                is Resultado.Fallo -> {
                    Toast.makeText(this@DetalleEvaluacionActivity, resultado.mensaje, Toast.LENGTH_LONG).show()
                    finish()
                }
            }
            binding.progressCarga.visibility = View.GONE
        }
    }

    private fun pintar(detalle: DetalleEvaluacionResponse) {
        val ev = detalle.evaluacion ?: return
        binding.toolbar.title = ev.reporte_codigo
        val c = binding.contDetalle
        c.removeAllViews()

        val colorHex = when (ev.color_clasificacion) { "verde" -> "#3F9142"; "rojo" -> "#C94A3C"; else -> "#D8A521" }
        agregarPill("  ${ev.color_clasificacion.uppercase()} · ${ev.nivel_dano}  ", colorHex)
        agregarTitulo(ev.reporte_titulo)
        agregarTexto("Evaluado por ${ev.ingeniero_nombre} · ${ev.fecha}", esMeta = true)

        seccion("EVALUACIÓN SIMPLE REGISTRADA")
        campo("Observaciones", ev.observaciones)
        campo("Recomendación", ev.recomendacion)
        campo("¿Habitable?", when (ev.habitable) { "si" -> "Sí"; "restringido" -> "Restringido"; else -> "No" })

        // Si el ingeniero hizo la inspección técnica completa de 8 pasos,
        // se muestra entera aquí — es la evidencia principal para decidir.
        val insp = detalle.inspeccion
        if (insp != null) {
            seccion("📋 INSPECCIÓN TÉCNICA COMPLETA (8 PASOS)")
            campo("ID de la edificación", insp.id_edificacion)
            if (!insp.foto_general.isNullOrBlank()) enlaceArchivo("📷 Ver foto general de la edificación", insp.foto_general)
            campo("Dirección verificada", insp.direccion_verificada)
            campo("Uso", insp.uso_edificacion)
            campo("Número de pisos", insp.numero_pisos?.toString())
            campo("Sistema estructural", insp.sistema_estructural)
            campo("Tipo de cubierta", insp.tipo_cubierta)
            campo("Estado de fachada", insp.estado_fachada)
            campo("Daños estructurales visibles", insp.danos_estructurales_ext)
            campo("Grietas, desprendimientos, inclinaciones o colapsos", insp.grietas_desprendimientos)
            campo("¿Acceso seguro?", if (insp.acceso_seguro == "si") "Sí" else "No")
            campo("Daños en columnas", insp.danos_columnas)
            campo("Daños en vigas", insp.danos_vigas)
            campo("Daños en muros", insp.danos_muros)
            campo("Daños en losas", insp.danos_losas)
            campo("Daños en escaleras", insp.estado_escaleras)
            indicador("Riesgo de colapso", insp.riesgo_colapso)
            indicador("Desplazamientos o inclinaciones", insp.desplazamientos_inclinaciones)
            indicador("Elementos sueltos o caída", insp.elementos_sueltos)
            indicador("Daños que comprometen la estabilidad", insp.danos_comprometen_estabilidad)
            indicador("Condiciones que impiden el ingreso", insp.condiciones_impiden_ingreso)
            campo("Recomendación preliminar", insp.recomendacion_preliminar)
            campo("Firma del inspector", insp.firma_inspector)
        } else {
            seccion("📋 INSPECCIÓN TÉCNICA COMPLETA")
            agregarTexto("El ingeniero registró solo la evaluación simple, sin el formulario de 8 pasos.", esMeta = true)
        }

        seccion("📸 EVIDENCIA FOTOGRÁFICA DEL REPORTE (${detalle.evidencias.size})")
        if (detalle.evidencias.isEmpty()) {
            agregarTexto("Este reporte no tiene fotos adjuntas todavía.", esMeta = true)
        } else {
            detalle.evidencias.forEach { evi ->
                val elemento = evi.elemento_estructural?.replaceFirstChar { it.uppercase() } ?: "Sin clasificar"
                enlaceArchivo("📎 ${evi.tipo.replaceFirstChar { it.uppercase() }} — $elemento (${evi.created_at})", evi.ruta_archivo)
            }
        }

        if (!ev.comentario_auditoria.isNullOrBlank()) {
            seccion("AUDITORÍA ANTERIOR")
            campo("Comentario de ${ev.auditor_nombre ?: "—"}", ev.comentario_auditoria)
        }

        // Los botones de decisión solo aparecen si esta evaluación
        // todavía no fue resuelta, y solo se muestran DESPUÉS de haber
        // renderizado todo el contenido de arriba.
        if (ev.estado_auditoria == "sin_revisar" || ev.estado_auditoria == "aplazada") {
            binding.contDecision.visibility = View.VISIBLE
            binding.btnCertificar.setOnClickListener { decidir("certificada") }
            binding.btnAplazar.setOnClickListener { decidir("aplazada") }
            binding.btnObjetar.setOnClickListener { decidir("objetada") }
        } else {
            binding.contDecision.visibility = View.GONE
        }
    }

    private fun decidir(decision: String) {
        val etiquetas = mapOf("certificada" to "certificar", "objetada" to "objetar", "aplazada" to "aplazar")
        AlertDialog.Builder(this)
            .setTitle("¿${etiquetas[decision]?.replaceFirstChar { it.uppercase() }} esta evaluación?")
            .setMessage("Ya revisaste el trabajo completo. Esta acción quedará registrada con tu nombre.")
            .setPositiveButton("Confirmar") { _, _ ->
                val comentario = binding.inputComentario.text.toString().trim()
                lifecycleScope.launch {
                    when (val resultado = repo.certificarEvaluacion(evaluacionId, decision, comentario)) {
                        is Resultado.Exito -> {
                            Toast.makeText(this@DetalleEvaluacionActivity, resultado.datos, Toast.LENGTH_LONG).show()
                            finish()
                        }
                        is Resultado.Fallo -> Toast.makeText(this@DetalleEvaluacionActivity, resultado.mensaje, Toast.LENGTH_LONG).show()
                    }
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun enlaceArchivo(etiqueta: String, ruta: String) {
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
                    Toast.makeText(this@DetalleEvaluacionActivity, "No se pudo abrir el archivo.", Toast.LENGTH_SHORT).show()
                }
            }
        })
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
            text = texto
            setBackgroundColor(Color.parseColor(colorHex))
            setTextColor(Color.WHITE)
            textSize = 12f
            setTypeface(typeface, Typeface.BOLD)
            setPadding(dp(6), dp(4), dp(6), dp(4))
        })
    }

    private fun dp(valor: Int): Int = (valor * resources.displayMetrics.density).toInt()
}
