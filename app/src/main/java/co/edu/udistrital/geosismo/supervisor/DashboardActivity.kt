package co.edu.udistrital.geosismo.supervisor

import android.graphics.Color
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import co.edu.udistrital.geosismo.supervisor.databinding.ActivityDashboardBinding
import co.edu.udistrital.geosismo.supervisor.network.model.ConteoDto
import co.edu.udistrital.geosismo.supervisor.repository.AdminRepository
import co.edu.udistrital.geosismo.supervisor.repository.Resultado
import kotlinx.coroutines.launch

class DashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardBinding
    private lateinit var repo: AdminRepository

    private val ESTADO_LABEL = mapOf("nuevo" to "Nuevo", "en_revision" to "En revisión", "evaluado" to "Evaluado", "cerrado" to "Cerrado")
    private val URGENCIA_LABEL = mapOf("bajo" to "Bajo", "medio" to "Medio", "alto" to "Alto", "critico" to "Crítico")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener { finish() }
        repo = AdminRepository(this)
        binding.swipeRefresh.setOnRefreshListener { cargar() }

        cargar()
    }

    private fun cargar() {
        lifecycleScope.launch {
            when (val resultado = repo.estadisticas()) {
                is Resultado.Exito -> {
                    val datos = resultado.datos
                    binding.statTotal.text = datos.total_reportes.toString()
                    binding.statPersonas.text = datos.personas_afectadas.toString()
                    binding.statEvaluaciones.text = datos.total_evaluaciones.toString()
                    binding.statIngenieros.text = datos.ingenieros_activos.toString()
                    dibujarBarras(binding.contPorEstado, datos.por_estado) { it.estado?.let { e -> ESTADO_LABEL[e] ?: e } ?: "—" }
                    dibujarBarras(binding.contPorUrgencia, datos.por_urgencia) { it.nivel_urgencia?.let { u -> URGENCIA_LABEL[u] ?: u } ?: "—" }
                }
                is Resultado.Fallo -> {
                    android.widget.Toast.makeText(this@DashboardActivity, resultado.mensaje, android.widget.Toast.LENGTH_LONG).show()
                }
            }
            binding.swipeRefresh.isRefreshing = false
        }
    }

    private fun dibujarBarras(contenedor: LinearLayout, datos: List<ConteoDto>, etiqueta: (ConteoDto) -> String) {
        contenedor.removeAllViews()
        if (datos.isEmpty()) {
            contenedor.addView(TextView(this).apply {
                text = "Sin datos todavía."; setTextColor(Color.parseColor("#4B5361")); textSize = 13f
            })
            return
        }
        val maximo = datos.maxOf { it.n }.coerceAtLeast(1)
        datos.forEach { item ->
            val fila = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(0, 0, 0, dp(10))
            }
            val filaTexto = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
            filaTexto.addView(TextView(this).apply {
                text = etiqueta(item); setTextColor(Color.parseColor("#171B22")); textSize = 13f
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            })
            filaTexto.addView(TextView(this).apply {
                text = item.n.toString(); setTextColor(Color.parseColor("#4B5361")); textSize = 13f
            })
            val barra = ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal).apply {
                max = maximo; progress = item.n
                layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(8)).apply { topMargin = dp(4) }
                progressTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#4C7EBF"))
            }
            fila.addView(filaTexto); fila.addView(barra)
            contenedor.addView(fila)
        }
    }

    private fun dp(valor: Int): Int = (valor * resources.displayMetrics.density).toInt()
}
