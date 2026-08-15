package co.edu.udistrital.geosismo.supervisor.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import co.edu.udistrital.geosismo.supervisor.R
import co.edu.udistrital.geosismo.supervisor.network.model.VoluntarioPendienteDto

class VoluntarioPendienteAdapter(
    private val onAprobar: (VoluntarioPendienteDto) -> Unit,
    private val onRechazar: (VoluntarioPendienteDto) -> Unit
) : ListAdapter<VoluntarioPendienteDto, VoluntarioPendienteAdapter.ViewHolder>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_voluntario_pendiente, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), onAprobar, onRechazar)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val txtNombre: TextView = itemView.findViewById(R.id.txtNombre)
        private val txtEmail: TextView = itemView.findViewById(R.id.txtEmail)
        private val txtProfesion: TextView = itemView.findViewById(R.id.txtProfesion)
        private val txtTarjeta: TextView = itemView.findViewById(R.id.txtTarjeta)
        private val txtTelefono: TextView = itemView.findViewById(R.id.txtTelefono)
        private val txtFecha: TextView = itemView.findViewById(R.id.txtFecha)
        private val btnAprobar: android.widget.Button = itemView.findViewById(R.id.btnAprobar)
        private val btnRechazar: android.widget.Button = itemView.findViewById(R.id.btnRechazar)

        fun bind(
            item: VoluntarioPendienteDto,
            onAprobar: (VoluntarioPendienteDto) -> Unit,
            onRechazar: (VoluntarioPendienteDto) -> Unit
        ) {
            txtNombre.text = item.nombre
            txtEmail.text = item.email
            txtProfesion.text = "🎓 ${item.profesion?.ifBlank { "Sin especificar" } ?: "Sin especificar"}"

            val tarjeta = item.tarjeta_profesional
            txtTarjeta.visibility = if (tarjeta.isNullOrBlank()) View.GONE else View.VISIBLE
            txtTarjeta.text = "Tarjeta profesional: $tarjeta"

            val telefono = item.telefono
            txtTelefono.visibility = if (telefono.isNullOrBlank()) View.GONE else View.VISIBLE
            txtTelefono.text = "📞 $telefono"

            txtFecha.text = "Postulado el ${item.created_at}"

            btnAprobar.setOnClickListener { onAprobar(item) }
            btnRechazar.setOnClickListener { onRechazar(item) }
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<VoluntarioPendienteDto>() {
            override fun areItemsTheSame(a: VoluntarioPendienteDto, b: VoluntarioPendienteDto) = a.id == b.id
            override fun areContentsTheSame(a: VoluntarioPendienteDto, b: VoluntarioPendienteDto) = a == b
        }
    }
}
