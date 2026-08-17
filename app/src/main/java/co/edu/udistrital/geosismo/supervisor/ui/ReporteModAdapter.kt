package co.edu.udistrital.geosismo.supervisor.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import co.edu.udistrital.geosismo.supervisor.R
import co.edu.udistrital.geosismo.supervisor.network.model.ReporteAdminDto

class ReporteModAdapter(
    private val onClick: (ReporteAdminDto) -> Unit
) : ListAdapter<ReporteAdminDto, ReporteModAdapter.ViewHolder>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_reporte_mod, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), onClick)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val txtCodigo: TextView = itemView.findViewById(R.id.txtCodigo)
        private val txtTitulo: TextView = itemView.findViewById(R.id.txtTitulo)
        private val txtMeta: TextView = itemView.findViewById(R.id.txtMeta)
        private val pillEstado: TextView = itemView.findViewById(R.id.pillEstado)

        fun bind(r: ReporteAdminDto, onClick: (ReporteAdminDto) -> Unit) {
            txtCodigo.text = r.codigo
            txtTitulo.text = r.titulo
            txtMeta.text = "${r.direccion ?: "Sin dirección"} · ${r.reportante} · ${r.num_evidencias ?: 0} evidencia(s)"

            val estadoLabel = when (r.estado) {
                "nuevo" -> "Nuevo"; "en_revision" -> "En revisión"; "evaluado" -> "Evaluado"; "cerrado" -> "Cerrado"; else -> r.estado
            }
            pillEstado.text = estadoLabel

            itemView.setOnClickListener { onClick(r) }
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<ReporteAdminDto>() {
            override fun areItemsTheSame(a: ReporteAdminDto, b: ReporteAdminDto) = a.id == b.id
            override fun areContentsTheSame(a: ReporteAdminDto, b: ReporteAdminDto) = a == b
        }
    }
}
