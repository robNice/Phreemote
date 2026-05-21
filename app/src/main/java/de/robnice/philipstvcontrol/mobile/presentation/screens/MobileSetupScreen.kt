package de.robnice.philipstvcontrol.mobile.presentation.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.LiveTv
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import de.robnice.philipstvcontrol.presentation.MainViewModel
import de.robnice.philipstvcontrol.presentation.SetupStatus

@Composable
fun MobileSetupScreen(
    vm: MainViewModel,
    onClose: (() -> Unit)? = null
) {
    val status by vm.status.collectAsState()
    val isScanning by vm.isScanning.collectAsState()
    val found by vm.foundTvs.collectAsState()
    val selectedIp by vm.selectedIp.collectAsState()
    val selectedPaired by vm.selectedPaired.collectAsState()
    var showForgetConfirm by remember { mutableStateOf(false) }
    var showDemoConfirm by remember { mutableStateOf(false) }

    if (showDemoConfirm) {
        AlertDialog(
            onDismissRequest = { showDemoConfirm = false },
            title = { Text("Demo Mode") },
            text = { Text("Try the remote without pairing a TV. All buttons are visible but no commands are sent to any device.\n\nTap the Settings icon (⚙) in the remote to exit Demo Mode.") },
            confirmButton = {
                Button(onClick = { showDemoConfirm = false; vm.enterDemoMode() }) {
                    Text("Start Demo")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDemoConfirm = false }) { Text("Cancel") }
            }
        )
    }

    if (showForgetConfirm) {
        AlertDialog(
            onDismissRequest = { showForgetConfirm = false },
            title = { Text("Remove TV?") },
            text = { selectedIp?.let { Text(it) } },
            confirmButton = {
                Button(
                    onClick = { showForgetConfirm = false; vm.forgetTvCompletely() },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Remove") }
            },
            dismissButton = {
                TextButton(onClick = { showForgetConfirm = false }) { Text("Cancel") }
            }
        )
    }

    Scaffold(
        topBar = {
            PhreemoteBar {
                if (onClose != null) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clickable(onClick = onClose),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Rounded.Close, contentDescription = null, tint = Color.White)
                    }
                }
            }
        },
        contentWindowInsets = WindowInsets(0)
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Status
            item {
                val (statusText, isError) = when (val s = status) {
                    SetupStatus.Ready -> "Ready" to false
                    SetupStatus.Discovering -> "Searching your network…" to false
                    is SetupStatus.FoundIps -> "Found ${s.count} IPs, probing…" to false
                    SetupStatus.Probing -> "Probing TVs…" to false
                    is SetupStatus.Verified -> "Found ${s.count} TV(s)" to false
                    is SetupStatus.Error -> "Error: ${s.reason ?: "unknown"}" to true
                }
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isError) MaterialTheme.colorScheme.error
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }

            // Connected TV
            if (selectedPaired && selectedIp != null) {
                item {
                    Card(
                        onClick = { showForgetConfirm = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(horizontal = 16.dp, vertical = 14.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                Icons.Rounded.LiveTv,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(24.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "Connected",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                )
                                Text(
                                    selectedIp!!,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                            Icon(
                                Icons.Rounded.Close,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            // Scan / scanning
            item {
                if (!isScanning) {
                    Button(
                        onClick = { vm.runDiscovery() },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Scan LAN") }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Scanning…",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            TextButton(onClick = { vm.cancelDiscovery() }) { Text("Cancel") }
                        }
                    }
                }
            }

            // Found TVs list
            if (found.isNotEmpty()) {
                item {
                    Text(
                        "Found TVs",
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                items(found) { tv ->
                    val baseTitle = if (tv.name != null) "${tv.name} (${tv.ip})" else tv.ip
                    val isSelected = tv.trusted && selectedIp == tv.ip
                    val displayTitle = if (isSelected) "✓ $baseTitle" else baseTitle
                    val api = listOfNotNull(tv.apiMajor, tv.apiMinor).joinToString(".").ifBlank { "?" }

                    Card(
                        onClick = { vm.connectTo(tv) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(horizontal = 16.dp, vertical = 14.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                Icons.Rounded.LiveTv,
                                contentDescription = null,
                                tint = if (isSelected) MaterialTheme.colorScheme.primary
                                       else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(24.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(displayTitle, style = MaterialTheme.typography.bodyMedium)
                                Text(
                                    "API $api · ${tv.basePath ?: "/system"}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            item {
                OutlinedButton(
                    onClick = { showDemoConfirm = true },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Demo Mode") }
            }

            if (onClose != null) {
                item {
                    OutlinedButton(
                        onClick = onClose,
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Back") }
                }
            }
        }
    }
}
