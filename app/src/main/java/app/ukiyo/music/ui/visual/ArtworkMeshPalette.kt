package app.ukiyo.music.ui.visual

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.core.graphics.drawable.toBitmap
import androidx.palette.graphics.Palette
import coil.imageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap

@Immutable
data class MeshPalette(
    val colors: List<Color>
)

private val FallbackPalette = MeshPalette(
    listOf(
        Color(0xFF3A1C71),
        Color(0xFFD76D77),
        Color(0xFF2B5876),
        Color(0xFFFFAF7B)
    )
)

private val paletteCache = ConcurrentHashMap<String, MeshPalette>()

@Composable
fun rememberArtworkMeshPalette(url: String?): MeshPalette {
    val context = LocalContext.current
    val cached = remember(url) { url?.let(paletteCache::get) }
    val palette by produceState(initialValue = cached ?: FallbackPalette, url) {
        if (url.isNullOrBlank()) {
            value = FallbackPalette
            return@produceState
        }
        val existing = paletteCache[url]
        if (existing != null) {
            value = existing
            return@produceState
        }
        val loaded = loadPalette(context, url)
        if (loaded != null) {
            paletteCache[url] = loaded
            value = loaded
        }
    }
    return palette
}

private suspend fun loadPalette(context: Context, url: String): MeshPalette? {
    return withContext(Dispatchers.IO) {
        runCatching {
            val request = ImageRequest.Builder(context)
                .data(url)
                .allowHardware(false)
                .build()
            val result = context.imageLoader.execute(request)
            val drawable = (result as? SuccessResult)?.drawable ?: return@runCatching null
            val bitmap = drawable.toBitmap(width = 96, height = 96, config = android.graphics.Bitmap.Config.ARGB_8888)
            val palette = Palette.from(bitmap).maximumColorCount(16).generate()
            val swatches = listOfNotNull(
                palette.vibrantSwatch,
                palette.lightVibrantSwatch,
                palette.darkVibrantSwatch,
                palette.mutedSwatch,
                palette.dominantSwatch
            ).map { it.rgb }.distinct()

            if (swatches.isEmpty()) return@runCatching FallbackPalette
            val colors = swatches.take(4).map { tune(Color(it)) }
            val padded = (colors + FallbackPalette.colors).take(4)
            MeshPalette(padded)
        }.getOrNull()
    }
}

private fun tune(color: Color): Color {
    val hsl = FloatArray(3)
    ColorUtils.colorToHSL(color.toArgb(), hsl)
    hsl[1] = hsl[1].coerceIn(0.35f, 0.9f)
    hsl[2] = hsl[2].coerceIn(0.22f, 0.58f)
    return Color(ColorUtils.HSLToColor(hsl))
}
