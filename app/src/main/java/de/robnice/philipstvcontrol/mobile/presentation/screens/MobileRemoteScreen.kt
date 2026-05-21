package de.robnice.philipstvcontrol.mobile.presentation.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Input
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.rounded.List
import androidx.compose.material.icons.automirrored.rounded.Reply
import androidx.compose.material.icons.automirrored.rounded.VolumeDown
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.LiveTv
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.PowerSettingsNew
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material.icons.rounded.WifiOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import de.robnice.philipstvcontrol.domain.model.RemoteAction
import de.robnice.philipstvcontrol.mobile.R

private val BTN = 52.dp
private val BTN_SM = 36.dp
private val BTN_GAP = 10.dp
private val ROW_GAP = 8.dp

internal val phreemoteGradient = Brush.horizontalGradient(
    colorStops = arrayOf(
        0.00f to Color(0xFF3D5AFE),
        0.35f to Color(0xFF1A237E),
        0.80f to Color(0xFF1A237E),
        1.00f to Color.Transparent
    )
)

@Composable
fun PhreemoteBar(actions: @Composable RowScope.() -> Unit = {}) {
    Column {
        Spacer(Modifier.windowInsetsTopHeight(WindowInsets.statusBars))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(38.dp)
                .background(brush = phreemoteGradient)
                .padding(start = 8.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_launcher_foreground),
                    contentDescription = null,
                    modifier = Modifier.size(32.dp)
                )
                Text(
                    text = "HREEMOTE",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.15.em,
                    style = MaterialTheme.typography.titleMedium
                )
            }
            actions()
        }
    }
}

@Composable
fun MobileRemoteScreen(
    onOpenSettings: () -> Unit,
    onRemoteAction: (RemoteAction) -> Unit,
    tvOnline: Boolean? = null
) {
    var showPrivacy by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val versionName = remember {
        try { context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "?" }
        catch (_: Exception) { "?" }
    }

    if (showPrivacy) {
        val privacyText = remember {
            val lang = context.resources.configuration.locales[0].language
            val supported = setOf("de", "es", "fr", "it", "ja", "ko", "nl", "pl", "pt", "ru", "tr", "uk")
            val file = if (lang in supported) "privacy_policy_$lang.md" else "privacy_policy_en.md"
            try { context.assets.open(file).bufferedReader().readText() } catch (_: Exception) { "" }
        }
        AlertDialog(
            onDismissRequest = { showPrivacy = false },
            title = { Text(stringResource(R.string.privacy_title)) },
            text = {
                Column(
                    modifier = Modifier
                        .heightIn(max = 400.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    privacyText.lines().forEach { line ->
                        when {
                            line.startsWith("## ") -> Text(
                                text = line.removePrefix("## "),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                            line.startsWith("- ") -> Text(
                                text = "• ${line.removePrefix("- ")}",
                                style = MaterialTheme.typography.bodySmall
                            )
                            line.isBlank() -> Spacer(Modifier.height(4.dp))
                            else -> Text(line, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showPrivacy = false }) { Text("OK") }
            }
        )
    }

    Scaffold(
        topBar = {
            PhreemoteBar {
                if (tvOnline == false) {
                    Icon(
                        imageVector = Icons.Rounded.WifiOff,
                        contentDescription = null,
                        tint = Color(0xFFFF7070),
                        modifier = Modifier.padding(end = 4.dp)
                    )
                }
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1A237E))
                        .clickable(onClick = onOpenSettings),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Rounded.Settings, contentDescription = null, tint = Color.White)
                }
            }
        },
        bottomBar = {
            Column {
                HorizontalDivider()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.privacy_footer),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable { showPrivacy = true }
                    )
                    Text(
                        text = "v$versionName",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        contentWindowInsets = WindowInsets(0)
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(ROW_GAP)
        ) {
            // Info | Power | Source
            item {
                CenteredRow(gap = BTN_GAP) {
                    MobileButton(Icons.Rounded.Info) { onRemoteAction(RemoteAction.INFO) }
                    MobileButton(
                        icon = Icons.Rounded.PowerSettingsNew,
                        color = Color(0xFF441111),
                        iconColor = Color(0xFFFF4444)
                    ) { onRemoteAction(RemoteAction.STANDBY) }
                    MobileButton(Icons.AutoMirrored.Rounded.Input) { onRemoteAction(RemoteAction.SOURCE) }
                }
            }

            // D-pad
            item { DPadSection(onRemoteAction) }

            // Back | Home | TV
            item {
                CenteredRow(gap = BTN_GAP) {
                    MobileButton(
                        icon = Icons.AutoMirrored.Rounded.Reply,
                        color = Color(0xFF442222),
                        iconColor = Color(0xFFFF8888)
                    ) { onRemoteAction(RemoteAction.BACK) }
                    MobileButton(Icons.Rounded.Home) { onRemoteAction(RemoteAction.HOME) }
                    MobileButton(Icons.Rounded.LiveTv) { onRemoteAction(RemoteAction.TV) }
                }
            }

            // Stop | Play/Pause | Pause
            item {
                CenteredRow(gap = BTN_GAP) {
                    MobileButton(Icons.Rounded.Stop) { onRemoteAction(RemoteAction.STOP) }
                    MobileButton(Icons.Rounded.PlayArrow) { onRemoteAction(RemoteAction.PLAY_PAUSE) }
                    MobileButton(Icons.Rounded.Pause) { onRemoteAction(RemoteAction.PAUSE) }
                }
            }

            // Vol- | Options | Vol+
            item {
                CenteredRow(gap = BTN_GAP) {
                    MobileButton(Icons.AutoMirrored.Rounded.VolumeDown) { onRemoteAction(RemoteAction.VOLUME_DOWN) }
                    MobileButton(Icons.AutoMirrored.Rounded.List) { onRemoteAction(RemoteAction.OPTIONS) }
                    MobileButton(Icons.AutoMirrored.Rounded.VolumeUp) { onRemoteAction(RemoteAction.VOLUME_UP) }
                }
            }

            // Color buttons (compact)
            item {
                CenteredRow(gap = BTN_GAP) {
                    MobileColorButton(Color(0xFFDD2222), size = BTN_SM) { onRemoteAction(RemoteAction.RED) }
                    MobileColorButton(Color(0xFF22AA22), size = BTN_SM) { onRemoteAction(RemoteAction.GREEN) }
                    MobileColorButton(Color(0xFFCCCC00), size = BTN_SM) { onRemoteAction(RemoteAction.YELLOW) }
                    MobileColorButton(Color(0xFF2244DD), size = BTN_SM) { onRemoteAction(RemoteAction.BLUE) }
                }
            }

            // Numpad 1–9
            listOf(
                listOf(RemoteAction.DIGIT_1, RemoteAction.DIGIT_2, RemoteAction.DIGIT_3),
                listOf(RemoteAction.DIGIT_4, RemoteAction.DIGIT_5, RemoteAction.DIGIT_6),
                listOf(RemoteAction.DIGIT_7, RemoteAction.DIGIT_8, RemoteAction.DIGIT_9),
            ).forEach { row ->
                item {
                    CenteredRow(gap = 8.dp) {
                        row.forEach { action ->
                            MobileNumpadButton(action.name.takeLast(1)) { onRemoteAction(action) }
                        }
                    }
                }
            }

            // 0
            item {
                CenteredRow {
                    MobileNumpadButton("0") { onRemoteAction(RemoteAction.DIGIT_0) }
                }
            }
        }
    }
}

@Composable
private fun CenteredRow(gap: Dp = 0.dp, content: @Composable () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (gap > 0.dp) Arrangement.spacedBy(gap, Alignment.CenterHorizontally)
                                else Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) { content() }
}

@Composable
private fun DPadSection(onRemoteAction: (RemoteAction) -> Unit) {
    val spacing = 4.dp
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(10.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(spacing)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(spacing)) {
                Spacer(Modifier.size(BTN))
                MobileButton(Icons.Rounded.KeyboardArrowUp, size = BTN) { onRemoteAction(RemoteAction.CURSOR_UP) }
                Spacer(Modifier.size(BTN))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(spacing)) {
                MobileButton(Icons.AutoMirrored.Rounded.KeyboardArrowLeft, size = BTN) { onRemoteAction(RemoteAction.CURSOR_LEFT) }
                MobileButton(
                    icon = Icons.Rounded.Check,
                    size = BTN,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    iconColor = MaterialTheme.colorScheme.onPrimaryContainer
                ) { onRemoteAction(RemoteAction.OK) }
                MobileButton(Icons.AutoMirrored.Rounded.KeyboardArrowRight, size = BTN) { onRemoteAction(RemoteAction.CURSOR_RIGHT) }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(spacing)) {
                Spacer(Modifier.size(BTN))
                MobileButton(Icons.Rounded.KeyboardArrowDown, size = BTN) { onRemoteAction(RemoteAction.CURSOR_DOWN) }
                Spacer(Modifier.size(BTN))
            }
        }
    }
}

@Composable
fun MobileButton(
    icon: ImageVector,
    modifier: Modifier = Modifier,
    size: Dp = BTN,
    color: Color = Color.Unspecified,
    iconColor: Color = Color.Unspecified,
    onClick: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val bgColor = if (color == Color.Unspecified) MaterialTheme.colorScheme.surfaceContainerHigh else color
    val fgColor = if (iconColor == Color.Unspecified) MaterialTheme.colorScheme.onSurface else iconColor
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(bgColor)
            .clickable {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = fgColor,
            modifier = Modifier.fillMaxSize(0.55f)
        )
    }
}

@Composable
fun MobileNumpadButton(text: String, size: Dp = BTN, onClick: () -> Unit) {
    val haptic = LocalHapticFeedback.current
    val bgColor = MaterialTheme.colorScheme.surfaceContainerHigh
    val fgColor = MaterialTheme.colorScheme.onSurface
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(bgColor)
            .clickable {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, color = fgColor, style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
fun MobileColorButton(color: Color, size: Dp = BTN, onClick: () -> Unit) {
    val haptic = LocalHapticFeedback.current
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(color)
            .clickable {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onClick()
            }
    )
}
