/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter to find the
 * most up to date changes to the libraries and their usages.
 */

package de.robnice.philipstvcontrol.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.Card
import androidx.wear.compose.material3.CardDefaults
import androidx.wear.compose.material3.ConfirmationDialog
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import de.robnice.philipstvcontrol.R
import de.robnice.philipstvcontrol.presentation.screens.PairingScreen
import de.robnice.philipstvcontrol.presentation.screens.RemoteScreen
import de.robnice.philipstvcontrol.presentation.screens.SetupScreen
import de.robnice.philipstvcontrol.presentation.theme.PhilipsTVControlTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setContent {
            WearApp()
        }
    }
}

@Composable
fun WearApp() {
    PhilipsTVControlTheme {
        val vm: MainViewModel = viewModel()

        val bootstrapped by vm.bootstrapped.collectAsState()
        val selectedIp by vm.selectedIp.collectAsState()
        val selectedPaired by vm.selectedPaired.collectAsState()
        val showSetup by vm.showSetup.collectAsState()
        val tvOnline by vm.tvOnline.collectAsState()
        val demoMode by vm.demoMode.collectAsState()
        var showDemoExitConfirmation by remember { mutableStateOf(false) }

        if (!bootstrapped) {
            ScreenScaffold(scrollState = rememberTransformingLazyColumnState()) { padding ->
                val state = rememberTransformingLazyColumnState()
                TransformingLazyColumn(
                    state = state,
                    contentPadding = padding
                ) {
                    item {
                        Card(
                            onClick = {},
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer
                            )
                        ) {
                            Text(
                                text = "Loading…",
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }
                }
            }
        } else if (demoMode) {
            RemoteScreen(
                onOpenSettings = {
                    showDemoExitConfirmation = true
                    vm.exitDemoMode()
                    vm.openSetup()
                },
                onRemoteAction = { action -> vm.onRemoteAction(action) },
                tvOnline = null
            )
        } else when {
            selectedIp == null -> {
                SetupScreen(vm = vm, onClose = null)
            }
            showSetup -> {
                SetupScreen(vm = vm, onClose = { vm.closeSetup() })
            }
            !selectedPaired -> {
                PairingScreen(
                    selectedIp = selectedIp!!,
                    vm = vm,
                    onBackToSetup = { vm.openSetup() }
                )
            }
            else -> {
                RemoteScreen(
                    onOpenSettings = { vm.openSetup() },
                    onRemoteAction = { action -> vm.onRemoteAction(action) },
                    tvOnline = tvOnline
                )
            }
        }

        ConfirmationDialog(
            visible = showDemoExitConfirmation,
            onDismissRequest = { showDemoExitConfirmation = false },
            text = {
                Text(
                    text = stringResource(R.string.demo_mode_exited),
                    textAlign = TextAlign.Center
                )
            }
        ) {
            Icon(
                imageVector = Icons.Rounded.Check,
                contentDescription = null,
                modifier = Modifier.size(36.dp)
            )
        }
    }
}
