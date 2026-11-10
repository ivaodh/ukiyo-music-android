/*
 * Adapted from BitChord's MeshGradient (GPLv3), upstream acf219a7e4a7f52f026d4336d5c7ac5cf7bfafcc.
 * Simplified for the Ukiyo Android spike.
 */
package app.ukiyo.music.ui.visual

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun MeshGradientBackground(
    palette: MeshPalette,
    modifier: Modifier = Modifier,
    trackKey: Any? = null,
    animated: Boolean = true,
    blurRadius: Int = 64
) {
    val colors = (palette.colors + listOf(
        Color(0xFF3A1C71),
        Color(0xFFD76D77),
        Color(0xFF2B5876),
        Color(0xFFFFAF7B)
    )).take(4)

    val spec = if (animated) tween<Color>(1400) else tween<Color>(0)
    val animatedColors = colors.mapIndexed { index, color ->
        animateColorAsState(color, spec, label = "meshColor$index").value
    }
    val baseColor by animateColorAsState(colors.first().copy(alpha = 0.9f), spec, label = "meshBase")

    val phase = remember { Animatable(0f) }
    LaunchedEffect(trackKey, animated) {
        if (!animated) {
            phase.snapTo(0f)
        } else {
            phase.snapTo(0f)
            phase.animateTo(0.7f, tween(9000, easing = FastOutSlowInEasing))
        }
    }

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .clipToBounds()
            .graphicsLayer {
                scaleX = 1.3f
                scaleY = 1.3f
            }
            .background(baseColor)
            .blur(blurRadius.dp)
    ) {
        val anchors = listOf(
            Offset(0.20f, 0.25f),
            Offset(0.80f, 0.20f),
            Offset(0.75f, 0.80f),
            Offset(0.25f, 0.75f)
        )
        val speeds = listOf(1f, -0.7f, 0.85f, -1.15f)
        val drift = phase.value

        animatedColors.forEachIndexed { index, color ->
            val anchor = anchors[index]
            val center = Offset(
                x = (anchor.x + 0.16f * cos(drift * speeds[index] + index * 1.7f)) * size.width,
                y = (anchor.y + 0.16f * sin(drift * speeds[index] * 0.9f + index * 2.3f)) * size.height
            )
            val radius = size.maxDimension * 0.66f
            drawCircle(
                brush = Brush.radialGradient(
                    colorStops = Array(FADE_CURVE_STOPS + 1) { i ->
                        val t = i / FADE_CURVE_STOPS.toFloat()
                        t to color.copy(alpha = 0.58f * (1f - smootherStep(t)))
                    },
                    center = center,
                    radius = radius
                ),
                radius = radius,
                center = center
            )
        }

        drawRect(color = Color.Black.copy(alpha = 0.18f))
    }
}
