package de.robnice.philipstvcontrol.mobile.presentation.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.LiveTv
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import de.robnice.philipstvcontrol.domain.model.ALL_JOINTSPACE_COMMANDS
import de.robnice.philipstvcontrol.domain.model.CustomButton
import de.robnice.philipstvcontrol.domain.model.LABEL_PRESETS
import de.robnice.philipstvcontrol.domain.model.toCommandDisplayName
import de.robnice.philipstvcontrol.mobile.R
import de.robnice.philipstvcontrol.presentation.MainViewModel
import de.robnice.philipstvcontrol.presentation.SetupStatus

@Composable
private fun commandDisplayName(key: String): String = when (key) {
    "Standby"        -> stringResource(R.string.cmd_standby)
    "Mute"           -> stringResource(R.string.cmd_mute)
    "VolumeUp"       -> stringResource(R.string.cmd_volume_up)
    "VolumeDown"     -> stringResource(R.string.cmd_volume_down)
    "ChannelUp"      -> stringResource(R.string.cmd_channel_up)
    "ChannelDown"    -> stringResource(R.string.cmd_channel_down)
    "CursorUp"       -> stringResource(R.string.cmd_cursor_up)
    "CursorDown"     -> stringResource(R.string.cmd_cursor_down)
    "CursorLeft"     -> stringResource(R.string.cmd_cursor_left)
    "CursorRight"    -> stringResource(R.string.cmd_cursor_right)
    "Confirm"        -> stringResource(R.string.cmd_confirm)
    "Back"           -> stringResource(R.string.cmd_back)
    "Home"           -> stringResource(R.string.cmd_home)
    "Options"        -> stringResource(R.string.cmd_options)
    "Source"         -> stringResource(R.string.cmd_source)
    "Info"           -> stringResource(R.string.cmd_info)
    "WatchTV"        -> stringResource(R.string.cmd_watch_tv)
    "Guide"          -> stringResource(R.string.cmd_guide)
    "PlayPause"      -> stringResource(R.string.cmd_play_pause)
    "Play"           -> stringResource(R.string.cmd_play)
    "Pause"          -> stringResource(R.string.cmd_pause)
    "Stop"           -> stringResource(R.string.cmd_stop)
    "FastForward"    -> stringResource(R.string.cmd_fast_forward)
    "Rewind"         -> stringResource(R.string.cmd_rewind)
    "Record"         -> stringResource(R.string.cmd_record)
    "RedColour"      -> stringResource(R.string.cmd_red_colour)
    "GreenColour"    -> stringResource(R.string.cmd_green_colour)
    "YellowColour"   -> stringResource(R.string.cmd_yellow_colour)
    "BlueColour"     -> stringResource(R.string.cmd_blue_colour)
    "Subtitle"       -> stringResource(R.string.cmd_subtitle)
    "TeleText"       -> stringResource(R.string.cmd_teletext)
    "Digit0"         -> stringResource(R.string.cmd_digit_0)
    "Digit1"         -> stringResource(R.string.cmd_digit_1)
    "Digit2"         -> stringResource(R.string.cmd_digit_2)
    "Digit3"         -> stringResource(R.string.cmd_digit_3)
    "Digit4"         -> stringResource(R.string.cmd_digit_4)
    "Digit5"         -> stringResource(R.string.cmd_digit_5)
    "Digit6"         -> stringResource(R.string.cmd_digit_6)
    "Digit7"         -> stringResource(R.string.cmd_digit_7)
    "Digit8"         -> stringResource(R.string.cmd_digit_8)
    "Digit9"         -> stringResource(R.string.cmd_digit_9)
    "AmbilightOnOff" -> stringResource(R.string.cmd_ambilight_on_off)
    else             -> key.toCommandDisplayName()
}

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
    val customButtons by vm.customButtons.collectAsState()
    var showForgetConfirm by remember { mutableStateOf(false) }
    var showDemoConfirm by remember { mutableStateOf(false) }
    var customButtonsExpanded by remember { mutableStateOf(false) }

    if (showDemoConfirm) {
        AlertDialog(
            onDismissRequest = { showDemoConfirm = false },
            title = { Text(stringResource(R.string.demo_mode)) },
            text = { Text(stringResource(R.string.demo_mode_description)) },
            confirmButton = {
                Button(onClick = { showDemoConfirm = false; vm.enterDemoMode() }) {
                    Text(stringResource(R.string.demo_mode_start))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDemoConfirm = false }) {
                    Text(stringResource(R.string.setup_cancel))
                }
            }
        )
    }

    if (showForgetConfirm) {
        AlertDialog(
            onDismissRequest = { showForgetConfirm = false },
            title = { Text(stringResource(R.string.setup_remove_tv_confirm_title)) },
            text = { selectedIp?.let { Text(it) } },
            confirmButton = {
                Button(
                    onClick = { showForgetConfirm = false; vm.forgetTvCompletely() },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text(stringResource(R.string.setup_remove_tv_confirm_yes)) }
            },
            dismissButton = {
                TextButton(onClick = { showForgetConfirm = false }) {
                    Text(stringResource(R.string.setup_cancel))
                }
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
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Status
            item {
                val (statusText, isError) = when (val s = status) {
                    SetupStatus.Ready -> stringResource(R.string.setup_status_ready) to false
                    SetupStatus.Discovering -> stringResource(R.string.setup_discovering) to false
                    is SetupStatus.FoundIps -> stringResource(R.string.setup_found_ips, s.count) to false
                    SetupStatus.Probing -> stringResource(R.string.setup_probing) to false
                    is SetupStatus.Verified -> stringResource(R.string.setup_verified_tvs, s.count) to false
                    is SetupStatus.Error -> stringResource(R.string.setup_error, s.reason ?: "?") to true
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
                                    stringResource(R.string.setup_connected),
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
                    ) { Text(stringResource(R.string.setup_scan_lan)) }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                stringResource(R.string.setup_scanning),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            TextButton(onClick = { vm.cancelDiscovery() }) {
                                Text(stringResource(R.string.setup_cancel))
                            }
                        }
                    }
                }
            }

            // Found TVs
            if (found.isNotEmpty()) {
                item {
                    Text(
                        stringResource(R.string.setup_found_tvs),
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                items(found) { tv ->
                    val baseTitle = tv.name
                        ?.let { stringResource(R.string.tv_name_with_ip, it, tv.ip) }
                        ?: tv.ip
                    val isSelected = tv.trusted && selectedIp == tv.ip
                    val displayTitle = if (isSelected) "✓ $baseTitle" else baseTitle
                    val api = listOfNotNull(tv.apiMajor, tv.apiMinor).joinToString(".").ifBlank { "?" }
                    val basePath = tv.basePath ?: stringResource(R.string.tv_base_default)

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
                                    stringResource(R.string.tv_api_base, api, basePath),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Custom Buttons — collapsible header
            item {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { customButtonsExpanded = !customButtonsExpanded }
                        .padding(vertical = 10.dp, horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        stringResource(R.string.custom_buttons),
                        style = MaterialTheme.typography.titleSmall
                    )
                    Icon(
                        imageVector = if (customButtonsExpanded) Icons.Rounded.ExpandLess
                                      else Icons.Rounded.ExpandMore,
                        contentDescription = null
                    )
                }
                HorizontalDivider()
            }

            // Custom Buttons — collapsible content
            item {
                AnimatedVisibility(
                    visible = customButtonsExpanded,
                    enter = expandVertically(),
                    exit = shrinkVertically()
                ) {
                    Column(
                        modifier = Modifier.padding(top = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            stringResource(R.string.custom_buttons_hint),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                        customButtons.forEachIndexed { i, btn ->
                            CustomButtonCard(
                                index = i,
                                btn = btn,
                                onUpdate = { vm.updateCustomButton(i, it) }
                            )
                        }
                    }
                }
            }

            // Demo Mode
            item {
                OutlinedButton(
                    onClick = { showDemoConfirm = true },
                    modifier = Modifier.fillMaxWidth()
                ) { Text(stringResource(R.string.demo_mode)) }
            }

            // Back
            if (onClose != null) {
                item {
                    OutlinedButton(
                        onClick = onClose,
                        modifier = Modifier.fillMaxWidth()
                    ) { Text(stringResource(R.string.setup_close)) }
                }
            }
        }
    }
}

@Composable
private fun CustomButtonCard(
    index: Int,
    btn: CustomButton,
    onUpdate: (CustomButton) -> Unit
) {
    var commandDropdownExpanded by remember { mutableStateOf(false) }
    var showLabelPicker by remember { mutableStateOf(false) }

    if (showLabelPicker) {
        LabelPickerDialog(
            onDismiss = { showLabelPicker = false },
            onSelect = { label ->
                onUpdate(btn.copy(label = label))
                showLabelPicker = false
            }
        )
    }

    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    stringResource(R.string.custom_button_title, index + 1),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Switch(
                    checked = btn.enabled,
                    onCheckedChange = { onUpdate(btn.copy(enabled = it)) }
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        stringResource(R.string.custom_button_command),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Box {
                        OutlinedButton(
                            onClick = { commandDropdownExpanded = true },
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(start = 12.dp, end = 8.dp, top = 4.dp, bottom = 4.dp)
                        ) {
                            Text(
                                if (btn.command.isBlank()) "—" else commandDisplayName(btn.command),
                                modifier = Modifier.weight(1f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Icon(Icons.Rounded.ArrowDropDown, contentDescription = null)
                        }
                        DropdownMenu(
                            expanded = commandDropdownExpanded,
                            onDismissRequest = { commandDropdownExpanded = false }
                        ) {
                            ALL_JOINTSPACE_COMMANDS.forEach { cmd ->
                                DropdownMenuItem(
                                    text = { Text(commandDisplayName(cmd)) },
                                    onClick = {
                                        onUpdate(btn.copy(command = cmd))
                                        commandDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        stringResource(R.string.custom_button_label),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedButton(
                        onClick = { showLabelPicker = true },
                        modifier = Modifier.size(52.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(
                            btn.label.ifBlank { "—" },
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LabelPickerDialog(
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.custom_button_pick_icon)) },
        text = {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 40.dp),
                modifier = Modifier.heightIn(max = 280.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(LABEL_PRESETS) { icon ->
                    Box(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                            .clickable { onSelect(icon) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(icon, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.setup_cancel))
            }
        }
    )
}
