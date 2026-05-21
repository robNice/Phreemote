package de.robnice.philipstvcontrol.mobile.presentation

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import de.robnice.philipstvcontrol.mobile.presentation.screens.MobilePairingScreen
import de.robnice.philipstvcontrol.mobile.presentation.screens.MobileRemoteScreen
import de.robnice.philipstvcontrol.mobile.R
import de.robnice.philipstvcontrol.mobile.presentation.screens.MobileSetupScreen
import de.robnice.philipstvcontrol.mobile.presentation.theme.MobileTheme
import de.robnice.philipstvcontrol.presentation.MainViewModel

class MobileMainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MobileApp()
        }
    }
}

@Composable
fun MobileApp() {
    MobileTheme {
        val vm: MainViewModel = viewModel()

        val bootstrapped by vm.bootstrapped.collectAsState()
        val selectedIp by vm.selectedIp.collectAsState()
        val selectedPaired by vm.selectedPaired.collectAsState()
        val showSetup by vm.showSetup.collectAsState()
        val tvOnline by vm.tvOnline.collectAsState()
        val demoMode by vm.demoMode.collectAsState()
        val context = LocalContext.current

        if (!bootstrapped) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (demoMode) {
            MobileRemoteScreen(
                onOpenSettings = {
                    Toast.makeText(context, context.getString(R.string.demo_mode_exited), Toast.LENGTH_SHORT).show()
                    vm.exitDemoMode()
                    vm.openSetup()
                },
                onRemoteAction = { vm.onRemoteAction(it) },
                tvOnline = null
            )
        } else when {
            selectedIp == null -> MobileSetupScreen(vm = vm)

            showSetup -> MobileSetupScreen(vm = vm, onClose = { vm.closeSetup() })

            !selectedPaired -> MobilePairingScreen(
                selectedIp = selectedIp!!,
                vm = vm,
                onBackToSetup = { vm.openSetup() }
            )

            else -> MobileRemoteScreen(
                onOpenSettings = { vm.openSetup() },
                onRemoteAction = { vm.onRemoteAction(it) },
                tvOnline = tvOnline
            )
        }
    }
}
