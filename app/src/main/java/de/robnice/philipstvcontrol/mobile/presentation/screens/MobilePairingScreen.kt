package de.robnice.philipstvcontrol.mobile.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import de.robnice.philipstvcontrol.presentation.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MobilePairingScreen(
    selectedIp: String,
    vm: MainViewModel,
    onBackToSetup: () -> Unit
) {
    val pairingReq by vm.pairingRequest.collectAsState()
    val pairingError by vm.pairingError.collectAsState()

    LaunchedEffect(selectedIp) {
        vm.startPairing()
    }

    var pin by remember { mutableStateOf("") }
    val canSubmit = pin.length == 4 && pairingReq != null

    Scaffold(
        topBar = { TopAppBar(title = { Text("Pairing") }) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Pairing with $selectedIp", style = MaterialTheme.typography.titleMedium)
                    Text("Check your TV for the PIN prompt.", style = MaterialTheme.typography.bodySmall)
                    if (pairingError != null) {
                        Text(
                            "Error: $pairingError",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            val shown = pin.take(4).padEnd(4, '•')
            Text(
                text = shown.chunked(1).joinToString("  "),
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    listOf("1", "2", "3"),
                    listOf("4", "5", "6"),
                    listOf("7", "8", "9")
                ).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        row.forEach { d ->
                            PinKey(label = d) { if (pin.length < 4) pin += d }
                        }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PinKey(label = "⌫") { if (pin.isNotEmpty()) pin = pin.dropLast(1) }
                    PinKey(label = "0") { if (pin.length < 4) pin += "0" }
                    PinKey(label = "OK", enabled = canSubmit) { vm.submitPin(pin) }
                }
            }

            OutlinedButton(
                onClick = onBackToSetup,
                modifier = Modifier.fillMaxWidth()
            ) { Text("Cancel") }
        }
    }
}

@Composable
private fun PinKey(label: String, enabled: Boolean = true, onClick: () -> Unit) {
    val haptic = LocalHapticFeedback.current
    Button(
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onClick()
        },
        enabled = enabled,
        modifier = Modifier.size(64.dp),
        contentPadding = PaddingValues(0.dp),
        shape = CircleShape
    ) {
        Text(label, style = MaterialTheme.typography.titleMedium)
    }
}
