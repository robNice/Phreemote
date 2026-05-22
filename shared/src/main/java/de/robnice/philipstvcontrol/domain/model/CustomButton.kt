package de.robnice.philipstvcontrol.domain.model

const val CUSTOM_BUTTON_COUNT = 4

data class CustomButton(
    val command: String = "",
    val label: String = "",
    val enabled: Boolean = false
) {
    val isActive: Boolean get() = enabled && command.isNotBlank() && label.isNotBlank()
}

val ALL_JOINTSPACE_COMMANDS = listOf(
    "Standby",
    "Mute", "VolumeUp", "VolumeDown",
    "ChannelUp", "ChannelDown",
    "CursorUp", "CursorDown", "CursorLeft", "CursorRight", "Confirm", "Back",
    "Home", "Options", "Source", "Info", "WatchTV", "Guide",
    "PlayPause", "Play", "Pause", "Stop", "FastForward", "Rewind", "Record",
    "RedColour", "GreenColour", "YellowColour", "BlueColour",
    "Subtitle", "TeleText",
    "Digit0", "Digit1", "Digit2", "Digit3", "Digit4",
    "Digit5", "Digit6", "Digit7", "Digit8", "Digit9",
    "AmbilightOnOff",
    "Netflix", "Amazon"
)

fun String.toCommandDisplayName(): String =
    replace(Regex("([a-z])([A-Z0-9])"), "$1 $2")
        .replace(Regex("([A-Z])([A-Z][a-z])"), "$1 $2")

val LABEL_PRESETS = listOf(
    "▶", "⏸", "⏹", "⏭", "⏮", "⏺",
    "🔇", "🔊", "📺", "🏠", "⚡", "★", "♥",
    "🔴", "🟢", "🟡", "🔵",
    "ℹ", "⚙", "📡", "☀", "🌙", "🔔",
    "↑", "↓", "←", "→",
    "A","B","C","D","E","F","G","H","I","J",
    "K","L","M","N","O","P","Q","R","S","T",
    "U","V","W","X","Y","Z",
    "0","1","2","3","4","5","6","7","8","9"
)
