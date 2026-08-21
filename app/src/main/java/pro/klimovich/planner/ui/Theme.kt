package pro.klimovich.planner.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val KlimovichTeal = Color(0xFF0E9398)
val KlimovichTealDark = Color(0xFF087277)
val Graphite = Color(0xFF202424)
val WarmBackground = Color(0xFFF7F6F2)
val CardBackground = Color(0xFFFFFFFF)
val Muted = Color(0xFF737B7B)
val Hairline = Color(0xFFE4E5E1)

private val Colors = lightColorScheme(
    primary = KlimovichTeal,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD6F2F1),
    onPrimaryContainer = Graphite,
    background = WarmBackground,
    onBackground = Graphite,
    surface = CardBackground,
    onSurface = Graphite,
    surfaceVariant = Color(0xFFEBEEEA),
    onSurfaceVariant = Muted,
    outline = Hairline
)

@Composable
fun KlimovichPlannerTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = Colors, content = content)
}
