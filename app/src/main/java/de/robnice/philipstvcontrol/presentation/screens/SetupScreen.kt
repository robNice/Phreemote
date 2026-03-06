package de.robnice.philipstvcontrol.presentation.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.Card
import androidx.wear.compose.material3.CardDefaults
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.ButtonDefaults
import de.robnice.philipstvcontrol.R
import de.robnice.philipstvcontrol.presentation.MainViewModel
import de.robnice.philipstvcontrol.presentation.SetupStatus
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.style.TextAlign

@Composable
fun SetupScreen(
    vm: MainViewModel,
    onClose: (() -> Unit)? = null,
    onSelect: (String) -> Unit = {}
) {
    val status by vm.status.collectAsState()
    val isScanning by vm.isScanning.collectAsState()
    val found by vm.foundTvs.collectAsState()
    val selectedIp by vm.selectedIp.collectAsState()
    val listState = rememberTransformingLazyColumnState()
    val selectedPaired by vm.selectedPaired.collectAsState()
    var showForgetConfirm by remember { mutableStateOf(false) }
    ScreenScaffold(scrollState = listState) { contentPadding ->
        TransformingLazyColumn(
            state = listState,
            contentPadding = contentPadding
        ) {

            if (showForgetConfirm) {

                item {
                    Card(
                        onClick = {},
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Text(
                            text = stringResource(R.string.setup_remove_tv_confirm_title),
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(10.dp)
                        )

                        if (selectedIp != null) {
                            Text(
                                text = selectedIp!!,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(start = 10.dp, end = 10.dp, bottom = 10.dp)
                            )
                        }
                    }
                }

                item {
                    Button(
                        onClick = {
                            showForgetConfirm = false
                            vm.forgetTvCompletely()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.setup_remove_tv_confirm_yes))
                    }
                }

                item {
                    Button(
                        onClick = { showForgetConfirm = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.setup_remove_tv_confirm_no))
                    }
                }

            } else {
                item {
                    val statusText = when (val s = status) {
                        SetupStatus.Ready -> stringResource(R.string.setup_status_ready)
                        SetupStatus.Discovering -> stringResource(R.string.setup_discovering)
                        is SetupStatus.FoundIps -> stringResource(R.string.setup_found_ips, s.count)
                        SetupStatus.Probing -> stringResource(R.string.setup_probing)
                        is SetupStatus.Verified -> stringResource(R.string.setup_verified_tvs, s.count)
                        is SetupStatus.Error -> stringResource(R.string.setup_error)
                        else -> stringResource(R.string.setup_status_ready)
                    }

                    Card(
                        onClick = {},
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Text(
                            text = stringResource(R.string.setup_title),
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(10.dp)
                        )
                        Text(
                            text = statusText,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(start = 10.dp, end = 10.dp, bottom = 10.dp),
                            style = MaterialTheme.typography.labelSmall,
                            textAlign = TextAlign.Center,
                        )
                    }
                }

                if (selectedPaired && selectedIp != null) {
                    item {
                        Card(
                            onClick = { showForgetConfirm = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer
                            )
                        ) {
                            Text(
                                text = stringResource(R.string.setup_remove_tv),
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(start = 10.dp, end = 10.dp, bottom = 10.dp)
                            )
                            Text(
                                text = "✓ ${selectedIp!!}",
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(10.dp),
                                style = MaterialTheme.typography.labelSmall,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                }


                if (!isScanning) {
                    item {
                        Button(
                            onClick = { vm.runDiscovery() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(stringResource(R.string.setup_scan_lan))
                        }
                    }
                } else {
                    item {
                        Card(
                            onClick = {},
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                        ) {
                            Text(
                                text = stringResource(R.string.setup_scanning),
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }
                    item {
                        Button(
                            onClick = { vm.cancelDiscovery() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(stringResource(R.string.setup_cancel))
                        }
                    }
                }

                if (found.isNotEmpty()) {
                    item {
                        Card(
                            onClick = {},
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                        ) {
                            Text(
                                text = stringResource(R.string.setup_found_tvs),
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }

                    found.forEach { tv ->
                        item {
                            val baseTitle = tv.name?.let { stringResource(R.string.tv_name_with_ip, it, tv.ip) } ?: tv.ip
                            val title = if (tv.trusted) stringResource(R.string.tv_trusted_check, baseTitle) else baseTitle

                            Button(
                                onClick = { vm.connectTo(tv) },
                                enabled = true,
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            ) {
                                Column {
                                    val shownTitle =
                                        if (tv.trusted && selectedIp == tv.ip) stringResource(R.string.tv_selected_prefix, title)
                                        else title

                                    Text(text = shownTitle)
                                    val api = listOfNotNull(tv.apiMajor, tv.apiMinor)
                                        .joinToString(".")
                                        .ifBlank { stringResource(R.string.tv_unknown_api) }

                                    val base = tv.basePath ?: stringResource(R.string.tv_base_default)
                                    Text(
                                        text = stringResource(R.string.tv_api, api) + "  " + stringResource(R.string.tv_base, base),
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                        }

                    }
                }
                if (onClose != null) {
                    item {
                        Button(
                            onClick = onClose,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(stringResource(R.string.setup_close))
                        }
                    }
                }
            }
        }
    }
}