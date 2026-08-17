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
import co.edu.udistrital.geosismo.supervisor.network.model.UsuarioAdminDto

class UsuarioAdapter(
    private val onClick: (UsuarioAdminDto) -> Unit
) : ListAdapter<UsuarioAdminDto, UsuarioAdapter.ViewHolder>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_usuario, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), onClick)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val txtNombre: TextView = itemView.findViewById(R.id.txtNombre)
        private val txtEmail: TextView = itemView.findViewById(R.id.txtEmail)
        private val txtDetalle: TextView = itemView.findViewById(R.id.txtDetalle)
        private val pillRol: TextView = itemView.findViewById(R.id.pillRol)

        fun bind(u: UsuarioAdminDto, onClick: (UsuarioAdminDto) -> Unit) {
            txtNombre.text = u.nombre
            txtEmail.text = u.email

            val rolLabel = when (u.rol) { "admin" -> "Administrador"; "ingeniero" -> "Ingeniero"; else -> "Ciudadano" }
            pillRol.text = rolLabel
            val color = when (u.rol) { "admin" -> "#4C7EBF"; "ingeniero" -> "#3F9142"; else -> "#4B5361" }
            pillRol.setTextColor(Color.parseColor(color))

            val detalle = StringBuilder()
            if (u.rol == "ingeniero") {
                val estadoVol = when (u.estado_voluntario) {
                    "aprobado" -> "Voluntario aprobado"; "pendiente" -> "Voluntario pendiente"; "rechazado" -> "Voluntario rechazado"; else -> ""
                }
                if (estadoVol.isNotBlank()) detalle.append(estadoVol)
                if (!u.profesion.isNullOrBlank()) detalle.append(" · ${u.profesion}")
            }
            detalle.append(if (detalle.isNotEmpty()) " · " else "").append("Registrado ${u.created_at}")
            txtDetalle.text = detalle.toString()

            itemView.setOnClickListener { onClick(u) }
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<UsuarioAdminDto>() {
            override fun areItemsTheSame(a: UsuarioAdminDto, b: UsuarioAdminDto) = a.id == b.id
            override fun areContentsTheSame(a: UsuarioAdminDto, b: UsuarioAdminDto) = a == b
        }
    }
}
