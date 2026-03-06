package de.robnice.philipstvcontrol.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.*
import de.robnice.philipstvcontrol.R
import de.robnice.philipstvcontrol.presentation.MainViewModel

@Composable
fun PairingScreen(
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

    fun addDigit(d: String) {
        if (pin.length < 4) pin += d
    }

    fun backspace() {
        if (pin.isNotEmpty()) pin = pin.dropLast(1)
    }

    val canSubmit = pin.length == 4 && pairingReq != null


    PairingPadScreen(
        pin = pin,
        okEnabled = canSubmit,
        onDigit = { addDigit(it) },
        onBackspace = { backspace() },
        onOk = { vm.submitPin(pin) },
        onCancel = onBackToSetup
    )
}

@Composable
private fun PairingPadOnly(
    pin: String,
    minPinLen: Int,
    onDigit: (String) -> Unit,
    onBackspace: () -> Unit,
    onCancel: () -> Unit,
    onOk: () -> Unit,
    okEnabled: Boolean,
) {
    ScreenScaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Card(
                onClick = {},
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                val shown = pin.padEnd(minPinLen, '•')
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = shown.chunked(1).joinToString(" "),
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )

                    Button(
                        onClick = onBackspace,
                        enabled = pin.isNotEmpty(),
                        modifier = Modifier.size(36.dp),
                        contentPadding = PaddingValues(0.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    ) {
                        Text("⌫", textAlign = TextAlign.Center)
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                KeypadRow(modifier = Modifier.weight(1f), digits = listOf("1", "2", "3"), onDigit = onDigit)
                KeypadRow(modifier = Modifier.weight(1f), digits = listOf("4", "5", "6"), onDigit = onDigit)
                KeypadRow(modifier = Modifier.weight(1f), digits = listOf("7", "8", "9"), onDigit = onDigit)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    KeyButton(
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        onClick = onCancel
                    ) { Text("✕", textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) }

                    KeyButton(
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        onClick = { onDigit("0") }
                    ) { Text("0", textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) }

                    KeyButton(
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        onClick = onOk,
                        enabled = okEnabled
                    ) { Text("OK", textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) }
                }
            }
        }
    }
}

@Composable
private fun KeypadRow(
    modifier: Modifier,
    digits: List<String>,
    onDigit: (String) -> Unit
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        digits.forEach { d ->
            KeyButton(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                onClick = { onDigit(d) }
            ) {
                Text(d, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
private fun KeyButton(
    modifier: Modifier,
    onClick: () -> Unit,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier,
        contentPadding = PaddingValues(0.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                content = content
            )
        }
    }
}