package app.ukiyo.music.ui.glass

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.ExperimentalHazeApi
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import app.ukiyo.music.ui.theme.UkiyoSurface

@OptIn(ExperimentalHazeApi::class)
fun Modifier.ukiyoGlass(
    hazeState: HazeState,
    shape: Shape,
    tint: Color = UkiyoSurface,
    surfaceAlpha: Float = 0.28f,
    blurRadius: Int = 24
): Modifier {
    return this
        .clip(shape)
        .hazeEffect(
            state = hazeState,
            style = HazeStyle(
                backgroundColor = tint.copy(alpha = 0.72f),
                tints = listOf(HazeTint(tint.copy(alpha = 0.55f))),
                blurRadius = blurRadius.dp,
                noiseFactor = 0f,
                fallbackTint = HazeTint(tint.copy(alpha = 0.92f))
            )
        )
        .background(tint.copy(alpha = surfaceAlpha))
        .border(1.dp, Color.White.copy(alpha = 0.08f), shape)
}
