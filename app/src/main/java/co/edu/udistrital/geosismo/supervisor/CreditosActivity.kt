package co.edu.udistrital.geosismo.supervisor

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import co.edu.udistrital.geosismo.supervisor.databinding.ActivityCreditosBinding

class CreditosActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityCreditosBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.toolbar.setNavigationOnClickListener { finish() }
    }
}
