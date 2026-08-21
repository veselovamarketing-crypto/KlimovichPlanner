package pro.klimovich.planner

import android.Manifest
import android.os.Bundle
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.viewmodel.compose.viewModel
import pro.klimovich.planner.ui.KlimovichPlannerApp
import pro.klimovich.planner.ui.KlimovichPlannerTheme

class MainActivity : ComponentActivity() {
    private val notificationPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= 33) notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        val app = application as PlannerApplication
        setContent {
            KlimovichPlannerTheme {
                val vm: PlannerViewModel = viewModel(factory = PlannerViewModel.Factory(app.repository))
                KlimovichPlannerApp(vm)
            }
        }
    }
}
