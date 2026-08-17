package co.edu.udistrital.geosismo.supervisor.ui

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import co.edu.udistrital.geosismo.supervisor.R
import co.edu.udistrital.geosismo.supervisor.network.model.EvaluacionAdminDto

class EvaluacionAdapter(
    private val onCertificar: (EvaluacionAdminDto, String) -> Unit,
    private val onObjetar: (EvaluacionAdminDto, String) -> Unit
) : ListAdapter<EvaluacionAdminDto, EvaluacionAdapter.ViewHolder>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_evaluacion, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), onCertificar, onObjetar)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tagColor: TextView = itemView.findViewById(R.id.tagColor)
        private val pillAuditoria: TextView = itemView.findViewById(R.id.pillAuditoria)
        private val txtReporte: TextView = itemView.findViewById(R.id.txtReporte)
        private val txtIngeniero: TextView = itemView.findViewById(R.id.txtIngeniero)
        private val txtObservaciones: TextView = itemView.findViewById(R.id.txtObservaciones)
        private val txtComentarioAuditoria: TextView = itemView.findViewById(R.id.txtComentarioAuditoria)
        private val contAcciones: View = itemView.findViewById(R.id.contAcciones)
        private val inputComentario: EditText = itemView.findViewById(R.id.inputComentario)
        private val btnCertificar: Button = itemView.findViewById(R.id.btnCertificar)
        private val btnObjetar: Button = itemView.findViewById(R.id.btnObjetar)

        fun bind(e: EvaluacionAdminDto, onCertificar: (EvaluacionAdminDto, String) -> Unit, onObjetar: (EvaluacionAdminDto, String) -> Unit) {
            val colorHex = when (e.color_clasificacion) { "verde" -> "#3F9142"; "rojo" -> "#C94A3C"; else -> "#D8A521" }
            tagColor.text = "  ${e.color_clasificacion.uppercase()} · ${e.nivel_dano}  "
            tagColor.setBackgroundColor(Color.parseColor(colorHex))

            val (auditLabel, auditColor) = when (e.estado_auditoria) {
                "certificada" -> "Certificada" to "#3F9142"
                "objetada" -> "Objetada" to "#C94A3C"
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

            contAcciones.visibility = if (e.estado_auditoria == "sin_revisar") View.VISIBLE else View.GONE
            inputComentario.setText("")

            btnCertificar.setOnClickListener { onCertificar(e, inputComentario.text.toString().trim()) }
            btnObjetar.setOnClickListener { onObjetar(e, inputComentario.text.toString().trim()) }
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<EvaluacionAdminDto>() {
            override fun areItemsTheSame(a: EvaluacionAdminDto, b: EvaluacionAdminDto) = a.id == b.id
            override fun areContentsTheSame(a: EvaluacionAdminDto, b: EvaluacionAdminDto) = a == b
        }
    }
}
