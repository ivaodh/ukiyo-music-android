package app.ukiyo.music.ui.theme

import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val UkiyoBlack = Color(0xFF0A0A0A)
val UkiyoSurface = Color(0xFF141414)
val UkiyoSurfaceRaised = Color(0xFF1D1D1D)
val UkiyoGold = Color(0xFFE3CAA5)
val UkiyoPeach = Color(0xFFE8A87C)
val UkiyoText = Color(0xFFF4F1EA)
val UkiyoMuted = Color(0xFFA9A49A)

private val UkiyoColors = darkColorScheme(
    primary = UkiyoGold,
    onPrimary = UkiyoBlack,
    secondary = UkiyoPeach,
    onSecondary = UkiyoBlack,
    background = UkiyoBlack,
    onBackground = UkiyoText,
    surface = UkiyoSurface,
    onSurface = UkiyoText,
    surfaceVariant = UkiyoSurfaceRaised,
    onSurfaceVariant = UkiyoMuted,
    outline = Color(0xFF333333),
    error = Color(0xFFFF8A80)
)

private val UkiyoTypography = Typography(
    displaySmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Black,
        fontSize = 32.sp,
        lineHeight = 36.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 30.sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = 26.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 22.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp
    )
)

@Composable
fun UkiyoTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = UkiyoColors,
        typography = UkiyoTypography
    ) {
        CompositionLocalProvider(
            LocalContentColor provides UkiyoColors.onBackground,
            content = content
        )
    }
}
