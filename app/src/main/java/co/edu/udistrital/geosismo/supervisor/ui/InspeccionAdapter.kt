package co.edu.udistrital.geosismo.supervisor.ui

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import co.edu.udistrital.geosismo.supervisor.R
import co.edu.udistrital.geosismo.supervisor.network.model.InspeccionAdminDto

/** Guarda temporalmente la inspección elegida para pasarla a la pantalla de detalle sin repetir la consulta de red. */
object InspeccionSeleccionada {
    var actual: InspeccionAdminDto? = null
}

class InspeccionAdapter(
    private val onClick: (InspeccionAdminDto) -> Unit
) : ListAdapter<InspeccionAdminDto, InspeccionAdapter.ViewHolder>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_inspeccion, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), onClick)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tagColor: TextView = itemView.findViewById(R.id.tagColor)
        private val txtReporte: TextView = itemView.findViewById(R.id.txtReporte)
        private val txtIngeniero: TextView = itemView.findViewById(R.id.txtIngeniero)
        private val txtResumen: TextView = itemView.findViewById(R.id.txtResumen)

        fun bind(insp: InspeccionAdminDto, onClick: (InspeccionAdminDto) -> Unit) {
            val colorHex = when (insp.clasificacion_preliminar) { "verde" -> "#3F9142"; "rojo" -> "#C94A3C"; else -> "#D8A521" }
            tagColor.text = "  ${insp.clasificacion_preliminar.uppercase()}  "
            tagColor.setBackgroundColor(Color.parseColor(colorHex))

            txtReporte.text = "${insp.reporte_codigo} — ${insp.reporte_titulo}"
            txtIngeniero.text = "Inspeccionado por ${insp.ingeniero_nombre} · ${insp.fecha_inspeccion}"

            val riesgos = mutableListOf<String>()
            if (insp.riesgo_colapso == "si") riesgos.add("riesgo de colapso")
            if (insp.condiciones_impiden_ingreso == "si") riesgos.add("acceso impedido")
            if (insp.danos_comprometen_estabilidad == "si") riesgos.add("compromete estabilidad")
            txtResumen.text = if (riesgos.isEmpty()) "Sin indicadores críticos de peligro reportados."
                else "⚠ " + riesgos.joinToString(", ").replaceFirstChar { it.uppercase() }

            itemView.setOnClickListener { onClick(insp) }
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<InspeccionAdminDto>() {
            override fun areItemsTheSame(a: InspeccionAdminDto, b: InspeccionAdminDto) = a.id == b.id
            override fun areContentsTheSame(a: InspeccionAdminDto, b: InspeccionAdminDto) = a == b
        }
    }
}
