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
import co.edu.udistrital.geosismo.supervisor.network.model.EvaluacionAdminDto

/**
 * Lista de evaluaciones para auditoría. A propósito NO tiene botones de
 * aprobar/objetar aquí — el administrador siempre debe entrar a ver el
 * trabajo completo (fotos + inspección técnica) antes de poder decidir.
 */
class EvaluacionAdapter(
    private val onClick: (EvaluacionAdminDto) -> Unit
) : ListAdapter<EvaluacionAdminDto, EvaluacionAdapter.ViewHolder>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_evaluacion, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), onClick)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tagColor: TextView = itemView.findViewById(R.id.tagColor)
        private val pillAuditoria: TextView = itemView.findViewById(R.id.pillAuditoria)
        private val txtReporte: TextView = itemView.findViewById(R.id.txtReporte)
        private val txtIngeniero: TextView = itemView.findViewById(R.id.txtIngeniero)
        private val txtObservaciones: TextView = itemView.findViewById(R.id.txtObservaciones)
        private val txtComentarioAuditoria: TextView = itemView.findViewById(R.id.txtComentarioAuditoria)

        fun bind(e: EvaluacionAdminDto, onClick: (EvaluacionAdminDto) -> Unit) {
            val colorHex = when (e.color_clasificacion) { "verde" -> "#3F9142"; "rojo" -> "#C94A3C"; else -> "#D8A521" }
            tagColor.text = "  ${e.color_clasificacion.uppercase()} · ${e.nivel_dano}  "
            tagColor.setBackgroundColor(Color.parseColor(colorHex))

            val (auditLabel, auditColor) = when (e.estado_auditoria) {
                "certificada" -> "Certificada" to "#3F9142"
                "objetada" -> "Objetada" to "#C94A3C"
                "aplazada" -> "Aplazada" to "#D8A521"
                else -> "Sin revisar" to "#4B5361"
            }
            pillAuditoria.text = auditLabel
            pillAuditoria.setTextColor(Color.parseColor(auditColor))

            txtReporte.text = "${e.reporte_codigo} — ${e.reporte_titulo}"
            txtIngeniero.text = "Evaluado por ${e.ingeniero_nombre} · ${e.fecha}"
            txtObservaciones.text = e.observaciones

            if (!e.comentario_auditoria.isNullOrBlank()) {
                txtComentarioAuditoria.visibility = View.VISIBLE
                txtComentarioAuditoria.text = "Auditoría (${e.auditor_nombre ?: "—"}): ${e.comentario_auditoria}"
            } else {
                txtComentarioAuditoria.visibility = View.GONE
            }

            itemView.setOnClickListener { onClick(e) }
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<EvaluacionAdminDto>() {
            override fun areItemsTheSame(a: EvaluacionAdminDto, b: EvaluacionAdminDto) = a.id == b.id
            override fun areContentsTheSame(a: EvaluacionAdminDto, b: EvaluacionAdminDto) = a == b
        }
    }
}
