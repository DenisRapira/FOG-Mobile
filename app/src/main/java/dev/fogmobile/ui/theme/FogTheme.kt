package dev.fogmobile.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val FogPink = Color(0xFFF50DB4)
val FogBlack = Color(0xFF000000)
val FogPanel = Color(0xFF111111)
val FogWire = Color(0xFF2A2A2A)
val FogText = Color(0xFFFFFFFF)
val FogMuted = Color(0xFF9A9A9A)
val FogSuccess = Color(0xFF1FD67A)
val FogWarning = Color(0xFFFFC857)
val FogError = Color(0xFFFF4D6D)

private val FogScheme: ColorScheme = darkColorScheme(
    primary = FogPink,
    background = FogBlack,
    surface = FogPanel,
    onPrimary = Color.White,
    onBackground = FogText,
    onSurface = FogText,
    secondary = FogWarning,
    error = FogError,
)

@Composable
fun FogTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = FogScheme,
        typography = MaterialTheme.typography,
        content = content,
    )
}
