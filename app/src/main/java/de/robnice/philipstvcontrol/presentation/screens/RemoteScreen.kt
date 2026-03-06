package de.robnice.philipstvcontrol.presentation.screens

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Input
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.rounded.List
import androidx.compose.material.icons.automirrored.rounded.Reply
import androidx.compose.material.icons.automirrored.rounded.VolumeDown
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import de.robnice.philipstvcontrol.domain.model.RemoteAction
import de.robnice.philipstvcontrol.presentation.ArcTextView
import de.robnice.philipstvcontrol.presentation.theme.PhilipsTVControlTheme
import androidx.core.graphics.createBitmap

@Composable
fun RemoteScreen(
    onOpenSettings: () -> Unit,
    onRemoteAction: (RemoteAction) -> Unit
) {
    val pagerState = rememberPagerState(
        initialPage = 1,
        pageCount = { 3 }
    )

    HorizontalPager(
        state = pagerState,
        modifier = Modifier.fillMaxSize()
    ) { page ->
        when (page) {
            0 -> ExtraFunctionsView(onRemoteAction)
            1 -> MainRemoteView(onOpenSettings, onRemoteAction)
            2 -> NumpadView(onRemoteAction)
        }
    }
}

@SuppressLint("UnusedBoxWithConstraintsScope", "LocalContextResourcesRead")
@Composable
fun MainRemoteView(onOpenSettings: () -> Unit, onRemoteAction: (RemoteAction) -> Unit) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        val width = maxWidth
        val height = maxHeight

        val context = LocalContext.current
        val logoBitmap = remember(context) {
            val drawable = androidx.core.content.ContextCompat.getDrawable(
                context,
                de.robnice.philipstvcontrol.R.drawable.ic_launcher_foreground
            )

            drawable?.let {
                val size = 120
                val bitmap = createBitmap(size, size)
                val canvas = android.graphics.Canvas(bitmap)
                it.setBounds(0, 0, size, size)
                it.draw(canvas)
                bitmap
            }
        }

        AndroidView(
            factory = { ctx ->
                ArcTextView(ctx, null).apply {
                    this.logoBitmap = logoBitmap
                }
            },
            update = { view ->
                if (view.logoBitmap != logoBitmap) {
                    view.logoBitmap = logoBitmap
                }
            },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .height(height * 0.25f)
                .padding(top = 10.dp)
        )

        val dpadYOffset = -height * 0.04f
        val dpadRadius = width * 0.22f
        val arrowSize = width * 0.17f

        WatchButton(
            modifier = Modifier.offset(y = dpadYOffset).size(width * 0.24f),
            icon = Icons.Rounded.Check,
            color = MaterialTheme.colorScheme.primary
        ) { onRemoteAction(RemoteAction.OK) }

        WatchButton(modifier = Modifier.align(Alignment.Center).offset(y = -dpadRadius + dpadYOffset).size(arrowSize), icon = Icons.Rounded.KeyboardArrowUp) { onRemoteAction(RemoteAction.CURSOR_UP) }
        WatchButton(modifier = Modifier.align(Alignment.Center).offset(y = dpadRadius + dpadYOffset).size(arrowSize), icon = Icons.Rounded.KeyboardArrowDown) { onRemoteAction(RemoteAction.CURSOR_DOWN) }
        WatchButton(modifier = Modifier.align(Alignment.Center).offset(x = -dpadRadius, y = dpadYOffset).size(arrowSize), icon = Icons.AutoMirrored.Rounded.KeyboardArrowLeft) { onRemoteAction(RemoteAction.CURSOR_LEFT) }
        WatchButton(modifier = Modifier.align(Alignment.Center).offset(x = dpadRadius, y = dpadYOffset).size(arrowSize), icon = Icons.AutoMirrored.Rounded.KeyboardArrowRight) { onRemoteAction(RemoteAction.CURSOR_RIGHT) }

        val sideBtnSize = width * 0.15f
        val xSide = width * 0.31f
        val ySide = width * 0.21f

        WatchButton(modifier = Modifier.align(Alignment.Center).offset(x = -xSide, y = -ySide + dpadYOffset).size(sideBtnSize), icon = Icons.AutoMirrored.Rounded.VolumeUp, color = Color.White, iconColor = Color.Black) { onRemoteAction(RemoteAction.VOLUME_UP) }
        WatchButton(modifier = Modifier.align(Alignment.Center).offset(x = xSide, y = -ySide + dpadYOffset).size(sideBtnSize), icon = Icons.Rounded.Settings, color = Color(0xFF333333)) { onOpenSettings() }
        WatchButton(modifier = Modifier.align(Alignment.Center).offset(x = -xSide, y = ySide + dpadYOffset).size(sideBtnSize), icon = Icons.AutoMirrored.Rounded.VolumeDown, color = Color.White, iconColor = Color.Black) { onRemoteAction(RemoteAction.VOLUME_DOWN) }
        WatchButton(modifier = Modifier.align(Alignment.Center).offset(x = xSide, y = ySide + dpadYOffset).size(sideBtnSize), icon = Icons.AutoMirrored.Rounded.Reply, color = Color(0xFF442222),iconColor = Color(0xFFFF8888)) { onRemoteAction(RemoteAction.BACK) }

        val mediaY = height * 0.39f
        val mediaX = width * 0.22f
        val mSize = width * 0.15f

        WatchButton(modifier = Modifier.align(Alignment.Center).offset(x = -mediaX, y = mediaY - 7.dp).size(mSize), icon = Icons.Rounded.PlayArrow, color = Color(0xFF2E7D32)) { onRemoteAction(RemoteAction.PLAY_PAUSE) }
        WatchButton(modifier = Modifier.align(Alignment.Center).offset(y = mediaY).size(mSize + 3.dp), icon = Icons.Rounded.Pause, color = Color(0xFF1565C0)) { onRemoteAction(RemoteAction.PAUSE) }
        WatchButton(modifier = Modifier.align(Alignment.Center).offset(x = mediaX, y = mediaY - 7.dp).size(mSize), icon = Icons.Rounded.Stop, color = Color(0xFFC62828)
        ) { onRemoteAction(RemoteAction.STOP) }
    }
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun NumpadView(onRemoteAction: (RemoteAction) -> Unit) {
    BoxWithConstraints(
        modifier = Modifier.fillMaxSize().background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        val width = maxWidth
        val height = maxHeight
        val btnSize = width * 0.20f
        val spacing = 4.dp

        AndroidView(
            factory = { ctx ->
                ArcTextView(ctx, null).apply {
                    this.logoBitmap = null
                }
            },
            update = {  },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .height(height * 0.25f)
                .padding(top = 10.dp)
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(spacing)
        ) {
            val rows = listOf(
                listOf(RemoteAction.DIGIT_1, RemoteAction.DIGIT_2, RemoteAction.DIGIT_3),
                listOf(RemoteAction.DIGIT_4, RemoteAction.DIGIT_5, RemoteAction.DIGIT_6),
                listOf(RemoteAction.DIGIT_7, RemoteAction.DIGIT_8, RemoteAction.DIGIT_9)
            )

            rows.forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(spacing)) {
                    row.forEach { action ->
                        NumpadButton(action.name.takeLast(1), btnSize) { onRemoteAction(action) }
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(spacing)) {
                WatchButton(
                    modifier = Modifier.size(btnSize),
                    icon = Icons.AutoMirrored.Rounded.Reply,
                    color = Color(0xFF442222),
                    iconColor = Color(0xFFFF8888)
                ) { onRemoteAction(RemoteAction.BACK) }
                NumpadButton("0", btnSize) { onRemoteAction(RemoteAction.DIGIT_0) }
                WatchButton(
                    modifier = Modifier.size(btnSize),
                    icon = Icons.Rounded.Check,
                    color = MaterialTheme.colorScheme.primary,
                    iconColor = Color.White
                ) { onRemoteAction(RemoteAction.OK) }
            }
        }
    }
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun ExtraFunctionsView(onRemoteAction: (RemoteAction) -> Unit) {
    BoxWithConstraints(
        modifier = Modifier.fillMaxSize().background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        val width = maxWidth
        val height = maxHeight
        val btnSize = width * 0.20f
        val spacing = 6.dp

        AndroidView(
            factory = { ctx ->
                ArcTextView(ctx, null).apply {
                    this.logoBitmap = null
                }
            },
            update = {  },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .height(height * 0.25f)
                .padding(top = 10.dp)
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(spacing),
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(spacing)) {
                WatchButton(modifier = Modifier.size(btnSize), icon = Icons.Rounded.PowerSettingsNew, color = Color(0xFF441111), iconColor = Color.Red) { onRemoteAction(RemoteAction.STANDBY) }
                WatchButton(modifier = Modifier.size(btnSize), icon = Icons.Rounded.Info) { onRemoteAction(RemoteAction.INFO) }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(spacing)) {
                WatchButton(modifier = Modifier.size(btnSize), icon = Icons.Rounded.Home) { onRemoteAction(RemoteAction.HOME) }
                WatchButton(modifier = Modifier.size(btnSize), icon = Icons.Rounded.LiveTv) { onRemoteAction(RemoteAction.TV) }
                WatchButton(modifier = Modifier.size(btnSize), icon = Icons.AutoMirrored.Rounded.Input) { onRemoteAction(RemoteAction.SOURCE) }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(spacing),
                modifier = Modifier.padding(bottom = 2.dp)
            ) {
                WatchButton(modifier = Modifier.size(btnSize), icon = Icons.AutoMirrored.Rounded.Reply, color = Color(0xFF442222),iconColor = Color(0xFFFF8888)) { onRemoteAction(RemoteAction.BACK) }
                WatchButton(modifier = Modifier.size(btnSize), icon = Icons.Rounded.Check, color = MaterialTheme.colorScheme.primary) { onRemoteAction(RemoteAction.OK) }
                WatchButton(modifier = Modifier.size(btnSize), icon = Icons.AutoMirrored.Rounded.List) { onRemoteAction(RemoteAction.OPTIONS) }
            }

            Row(
                modifier = Modifier.padding(vertical = 0.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                ColorSmallButton(Color.Red) { onRemoteAction(RemoteAction.RED) }
                ColorSmallButton(Color.Green) { onRemoteAction(RemoteAction.GREEN) }
                ColorSmallButton(Color.Yellow) { onRemoteAction(RemoteAction.YELLOW) }
                ColorSmallButton(Color.Blue) { onRemoteAction(RemoteAction.BLUE) }
            }
        }
    }
}


@Composable
fun ColorSmallButton(color: Color, onClick: () -> Unit) {
    val haptic = LocalHapticFeedback.current
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(color.copy(alpha = 0.8f))
            .clickable {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onClick()
            }
    )
}

@Composable
fun WatchButton(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    size: Dp = 34.dp,
    color: Color = Color(0xFF2C2C2C),
    iconColor: Color = Color.White,
    onClick: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(color)
            .clickable {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = iconColor, modifier = Modifier.fillMaxSize(0.6f))
    }
}

@Composable
fun NumpadButton(text: String, size: Dp, onClick: () -> Unit) {
    val haptic = LocalHapticFeedback.current
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(Color(0xFF2C2C2C))
            .clickable {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, color = Color.White, style = MaterialTheme.typography.titleMedium)
    }
}


@Preview(device = "id:wearos_small_round", name = "Small Watch (40mm)", showSystemUi = true)
@Composable
fun PreviewSmall() {
    PhilipsTVControlTheme {
        RemoteScreen(onOpenSettings = {}, onRemoteAction = {})
    }
}

@Preview(device = "id:wearos_large_round", name = "Large Watch (46mm)", showSystemUi = true)
@Composable
fun PreviewLarge() {
    PhilipsTVControlTheme {
        RemoteScreen(onOpenSettings = {}, onRemoteAction = {})
    }
}

@Preview(device = "id:wearos_rect", name = "Square Watch", showSystemUi = true)
@Composable
fun PreviewSquare() {
    PhilipsTVControlTheme {
        RemoteScreen(onOpenSettings = {}, onRemoteAction = {})
    }
}