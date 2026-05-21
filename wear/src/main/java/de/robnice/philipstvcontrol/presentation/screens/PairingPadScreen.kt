package de.robnice.philipstvcontrol.presentation.screens

import androidx.compose.ui.platform.LocalDensity
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.Card
import androidx.wear.compose.material3.CardDefaults
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.*
import androidx.compose.ui.tooling.preview.Devices
import androidx.wear.tooling.preview.devices.WearDevices

@Preview(
    name = "Round",
    device = WearDevices.LARGE_ROUND,
    showSystemUi = true
)
@Composable
private fun PairingPadScreenPreview() {
    MaterialTheme {
        var pin by remember { mutableStateOf("") }

        PairingPadScreen(
            pin = pin,
            okEnabled = pin.length == 4,
            onDigit = { d -> if (pin.length < 4) pin += d },
            onBackspace = { if (pin.isNotEmpty()) pin = pin.dropLast(1) },
            onOk = {},
            onCancel = { pin = "" }
        )
    }
}

@Composable
fun PairingPadScreen(
    pin: String,
    okEnabled: Boolean,
    onDigit: (String) -> Unit,
    onBackspace: () -> Unit,
    onOk: () -> Unit,
    onCancel: () -> Unit
) {
    val shown = pin.take(4).padEnd(4, '•')
    val shownSpaced = shown.toCharArray().joinToString(" ")

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val totalH = this.maxHeight

        val outerPadV = 0.dp
        val outerPadH = 8.dp
        val gap = 6.dp

        val rows = 4
        val cancelH = 44.dp

        val verticalGaps = gap * (1 + 3 + 1)

        val remaining = totalH - (outerPadV * 2) - cancelH - verticalGaps

        val slot = (remaining / (rows + 1)).coerceIn(34.dp, 52.dp)

        val pinH = (slot * 1.05f).coerceIn(36.dp, 56.dp)
        val keyH = slot.coerceIn(34.dp, 52.dp)

        Column(
            modifier = Modifier

                .padding(horizontal = outerPadH, vertical = outerPadV),
            verticalArrangement = Arrangement.spacedBy(gap)
        ) {
            val pinFontSize = with(LocalDensity.current) { (pinH * 0.55f).toSp() }

            Text(
                text = shownSpaced,
                color = MaterialTheme.colorScheme.background,
                textAlign = TextAlign.Center,
                maxLines = 1,
                softWrap = false,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = pinFontSize,
                    background = MaterialTheme.colorScheme.onSecondaryContainer,
                    platformStyle = PlatformTextStyle(
                        includeFontPadding = false
                    )
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = true),
                verticalArrangement = Arrangement.spacedBy(gap)
            ) {
                DigitRow(listOf("1", "2", "3"), keyH, onDigit)
                DigitRow(listOf("4", "5", "6"), keyH, onDigit)
                DigitRow(listOf("7", "8", "9"), keyH, onDigit)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(gap)
                ) {
                    KeyButton(
                        label = "⌫",
                        height = keyH,
                        onClick = onBackspace,
                        modifier = Modifier.weight(1f)
                    )
                    KeyButton(
                        label = "0",
                        height = keyH,
                        onClick = { onDigit("0") },
                        modifier = Modifier.weight(1f)
                    )
                    KeyButton(
                        label = "OK",
                        height = keyH,
                        onClick = onOk,
                        enabled = okEnabled,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(gap)
                ) {
                    KeyButton(
                        label = "X",
                        height = keyH,
                        onClick = onCancel,
                        modifier = Modifier.weight(1f)
                    )

                }

            }


        }
    }
}

@Composable
private fun DigitRow(
    digits: List<String>,
    height: androidx.compose.ui.unit.Dp,
    onDigit: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        digits.forEach { d ->
            KeyButton(
                label = d,
                height = height,
                onClick = { onDigit(d) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun KeyButton(
    label: String,
    height: androidx.compose.ui.unit.Dp,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(height),
        contentPadding = PaddingValues(0.dp)
    ) {
        Text(
            text = label,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
    }
}