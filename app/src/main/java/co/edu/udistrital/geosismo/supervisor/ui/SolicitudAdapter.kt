package co.edu.udistrital.geosismo.supervisor.ui

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
import co.edu.udistrital.geosismo.supervisor.network.model.SolicitudDto

class SolicitudAdapter(
    private val onResponder: (SolicitudDto, String) -> Unit,
    private val onCerrar: (SolicitudDto) -> Unit,
    private val onVerAdjunto: (SolicitudDto) -> Unit
) : ListAdapter<SolicitudDto, SolicitudAdapter.ViewHolder>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_solicitud, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), onResponder, onCerrar, onVerAdjunto)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val pillEstado: TextView = itemView.findViewById(R.id.pillEstado)
        private val txtAsunto: TextView = itemView.findViewById(R.id.txtAsunto)
        private val txtRemitente: TextView = itemView.findViewById(R.id.txtRemitente)
        private val txtMensaje: TextView = itemView.findViewById(R.id.txtMensaje)
        private val txtAdjunto: TextView = itemView.findViewById(R.id.txtAdjunto)
        private val txtRespuesta: TextView = itemView.findViewById(R.id.txtRespuesta)
        private val contAcciones: View = itemView.findViewById(R.id.contAcciones)
        private val inputRespuesta: EditText = itemView.findViewById(R.id.inputRespuesta)
        private val btnResponder: Button = itemView.findViewById(R.id.btnResponder)
        private val btnCerrar: Button = itemView.findViewById(R.id.btnCerrar)

        fun bind(
            s: SolicitudDto,
            onResponder: (SolicitudDto, String) -> Unit,
            onCerrar: (SolicitudDto) -> Unit,
            onVerAdjunto: (SolicitudDto) -> Unit
        ) {
            pillEstado.text = when (s.estado) {
                "respondido" -> "Respondido"; "cerrado" -> "Cerrado"; else -> "Pendiente"
            }
            txtAsunto.text = s.asunto
            txtRemitente.text = "${s.usuario_nombre ?: "—"} · ${s.usuario_email ?: ""} · ${s.created_at}"
            txtMensaje.text = s.mensaje

            if (!s.archivo_adjunto.isNullOrBlank()) {
                txtAdjunto.visibility = View.VISIBLE
                txtAdjunto.text = "📎 Ver archivo adjunto"
                txtAdjunto.setOnClickListener { onVerAdjunto(s) }
            } else {
                txtAdjunto.visibility = View.GONE
            }

            if (!s.respuesta.isNullOrBlank()) {
                txtRespuesta.visibility = View.VISIBLE
                txtRespuesta.text = "Tu respuesta: ${s.respuesta}"
            } else {
                txtRespuesta.visibility = View.GONE
            }

            // Solo se puede actuar sobre solicitudes pendientes
            contAcciones.visibility = if (s.estado == "pendiente") View.VISIBLE else View.GONE
            inputRespuesta.setText("")

            btnResponder.setOnClickListener {
                val texto = inputRespuesta.text.toString().trim()
                if (texto.isNotBlank()) onResponder(s, texto)
            }
            btnCerrar.setOnClickListener { onCerrar(s) }
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<SolicitudDto>() {
            override fun areItemsTheSame(a: SolicitudDto, b: SolicitudDto) = a.id == b.id
            override fun areContentsTheSame(a: SolicitudDto, b: SolicitudDto) = a == b
        }
    }
}
