package com.example.ui.theme

import androidx.compose.foundation.Canvas
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.model.CalcoraThemeId

@Immutable
data class CalcoraPalette(
  val lightScheme: ColorScheme,
  val darkScheme: ColorScheme,
  val scienceAccent: Color,
  val commerceAccent: Color,
  val artsAccent: Color,
  val universalAccent: Color,
  val previewBg: Color,
  val previewAccent: Color,
)

object CalcoraThemePalettes {

  fun forTheme(themeId: CalcoraThemeId): CalcoraPalette =
    when (themeId) {
      CalcoraThemeId.COSMO -> CalcoraPalette(
        lightScheme = lightColorScheme(
          primary = Color(0xFF4338CA),
          onPrimary = Color.White,
          primaryContainer = Color(0xFFE0E7FF),
          onPrimaryContainer = Color(0xFF1E1B4B),
          secondary = Color(0xFF0891B2),
          onSecondary = Color.White,
          secondaryContainer = Color(0xFFCFFAFE),
          onSecondaryContainer = Color(0xFF083344),
          tertiary = Color(0xFF7C3AED),
          onTertiary = Color.White,
          background = Color(0xFFF6F7FC),
          onBackground = Color(0xFF11142D),
          surface = Color(0xFFFFFFFF),
          onSurface = Color(0xFF11142D),
          surfaceVariant = Color(0xFFEBEFF9),
          onSurfaceVariant = Color(0xFF4A506F),
          outline = Color(0xFFC6CCE3),
        ),
        darkScheme = darkColorScheme(
          primary = Color(0xFF818CF8),
          onPrimary = Color(0xFF0F0D35),
          primaryContainer = Color(0xFF2B276B),
          onPrimaryContainer = Color(0xFFE0E7FF),
          secondary = Color(0xFF22D3EE),
          onSecondary = Color(0xFF042F3B),
          secondaryContainer = Color(0xFF134555),
          onSecondaryContainer = Color(0xFFCFFAFE),
          tertiary = Color(0xFFA78BFA),
          onTertiary = Color(0xFF1E1042),
          background = Color(0xFF0B0E1E),
          onBackground = Color(0xFFEDF0FF),
          surface = Color(0xFF141830),
          onSurface = Color(0xFFEDF0FF),
          surfaceVariant = Color(0xFF1E2444),
          onSurfaceVariant = Color(0xFFB5BDD9),
          outline = Color(0xFF333B63),
        ),
        scienceAccent = Color(0xFF6366F1),
        commerceAccent = Color(0xFF0284C7),
        artsAccent = Color(0xFFEA580C),
        universalAccent = Color(0xFF059669),
        previewBg = Color(0xFF1E1B4B),
        previewAccent = Color(0xFF22D3EE),
      )

      CalcoraThemeId.BYTE -> CalcoraPalette(
        lightScheme = lightColorScheme(
          primary = Color(0xFF0F766E),
          onPrimary = Color.White,
          primaryContainer = Color(0xFFCCFBF1),
          onPrimaryContainer = Color(0xFF042F2E),
          secondary = Color(0xFF2563EB),
          onSecondary = Color.White,
          secondaryContainer = Color(0xFFDBEAFE),
          onSecondaryContainer = Color(0xFF172554),
          tertiary = Color(0xFF0D9488),
          onTertiary = Color.White,
          background = Color(0xFFF3F8F8),
          onBackground = Color(0xFF0D1F22),
          surface = Color(0xFFFFFFFF),
          onSurface = Color(0xFF0D1F22),
          surfaceVariant = Color(0xFFE3F0F0),
          onSurfaceVariant = Color(0xFF3F5B5E),
          outline = Color(0xFFB8D4D4),
        ),
        darkScheme = darkColorScheme(
          primary = Color(0xFF2DD4BF),
          onPrimary = Color(0xFF022C29),
          primaryContainer = Color(0xFF114D49),
          onPrimaryContainer = Color(0xFFCCFBF1),
          secondary = Color(0xFF60A5FA),
          onSecondary = Color(0xFF0A214A),
          secondaryContainer = Color(0xFF1E3A6E),
          onSecondaryContainer = Color(0xFFDBEAFE),
          tertiary = Color(0xFF5EEAD4),
          onTertiary = Color(0xFF032C28),
          background = Color(0xFF091215),
          onBackground = Color(0xFFE4F6F6),
          surface = Color(0xFF101D22),
          onSurface = Color(0xFFE4F6F6),
          surfaceVariant = Color(0xFF192B32),
          onSurfaceVariant = Color(0xFFA7C4C7),
          outline = Color(0xFF2B464E),
        ),
        scienceAccent = Color(0xFF0D9488),
        commerceAccent = Color(0xFF2563EB),
        artsAccent = Color(0xFFD97706),
        universalAccent = Color(0xFF10B981),
        previewBg = Color(0xFF0F292E),
        previewAccent = Color(0xFF2DD4BF),
      )

      CalcoraThemeId.LABBY -> CalcoraPalette(
        lightScheme = lightColorScheme(
          primary = Color(0xFF15803D),
          onPrimary = Color.White,
          primaryContainer = Color(0xFFDCFCE7),
          onPrimaryContainer = Color(0xFF052E16),
          secondary = Color(0xFF0284C7),
          onSecondary = Color.White,
          secondaryContainer = Color(0xFFE0F2FE),
          onSecondaryContainer = Color(0xFF082F49),
          tertiary = Color(0xFF65A30D),
          onTertiary = Color.White,
          background = Color(0xFFF5FAF6),
          onBackground = Color(0xFF102315),
          surface = Color(0xFFFFFFFF),
          onSurface = Color(0xFF102315),
          surfaceVariant = Color(0xFFE6F3EA),
          onSurfaceVariant = Color(0xFF425C49),
          outline = Color(0xFFBED8C4),
        ),
        darkScheme = darkColorScheme(
          primary = Color(0xFF4ADE80),
          onPrimary = Color(0xFF052E16),
          primaryContainer = Color(0xFF14532D),
          onPrimaryContainer = Color(0xFFDCFCE7),
          secondary = Color(0xFF38BDF8),
          onSecondary = Color(0xFF082F49),
          secondaryContainer = Color(0xFF0C4A6E),
          onSecondaryContainer = Color(0xFFE0F2FE),
          tertiary = Color(0xFFA3E635),
          onTertiary = Color(0xFF1A2E05),
          background = Color(0xFF0A140D),
          onBackground = Color(0xFFE8F7EC),
          surface = Color(0xFF122117),
          onSurface = Color(0xFFE8F7EC),
          surfaceVariant = Color(0xFF1B3022),
          onSurfaceVariant = Color(0xFFACC9B3),
          outline = Color(0xFF2E4D37),
        ),
        scienceAccent = Color(0xFF16A34A),
        commerceAccent = Color(0xFF0284C7),
        artsAccent = Color(0xFFE11D48),
        universalAccent = Color(0xFF65A30D),
        previewBg = Color(0xFF14532D),
        previewAccent = Color(0xFFA3E635),
      )

      CalcoraThemeId.PANDA_MATH -> CalcoraPalette(
        lightScheme = lightColorScheme(
          primary = Color(0xFF2F6B4F),
          onPrimary = Color.White,
          primaryContainer = Color(0xFFDCEEE4),
          onPrimaryContainer = Color(0xFF0F2B1D),
          secondary = Color(0xFF334155),
          onSecondary = Color.White,
          secondaryContainer = Color(0xFFE2E8F0),
          onSecondaryContainer = Color(0xFF0F172A),
          tertiary = Color(0xFFB45309),
          onTertiary = Color.White,
          background = Color(0xFFFAF9F5),
          onBackground = Color(0xFF1C1F1D),
          surface = Color(0xFFFFFFFF),
          onSurface = Color(0xFF1C1F1D),
          surfaceVariant = Color(0xFFEFEDE6),
          onSurfaceVariant = Color(0xFF535752),
          outline = Color(0xFFD2CEC3),
        ),
        darkScheme = darkColorScheme(
          primary = Color(0xFF6EE7B7),
          onPrimary = Color(0xFF092E1E),
          primaryContainer = Color(0xFF1B4332),
          onPrimaryContainer = Color(0xFFDCEEE4),
          secondary = Color(0xFFCBD5E1),
          onSecondary = Color(0xFF1E293B),
          secondaryContainer = Color(0xFF334155),
          onSecondaryContainer = Color(0xFFF1F5F9),
          tertiary = Color(0xFFFBBF24),
          onTertiary = Color(0xFF381E00),
          background = Color(0xFF121413),
          onBackground = Color(0xFFF1F2EE),
          surface = Color(0xFF1A1E1C),
          onSurface = Color(0xFFF1F2EE),
          surfaceVariant = Color(0xFF252A27),
          onSurfaceVariant = Color(0xFFBCC2BC),
          outline = Color(0xFF3D443F),
        ),
        scienceAccent = Color(0xFF2F6B4F),
        commerceAccent = Color(0xFF1D4ED8),
        artsAccent = Color(0xFFB45309),
        universalAccent = Color(0xFF0F766E),
        previewBg = Color(0xFF1E2923),
        previewAccent = Color(0xFF6EE7B7),
      )

      CalcoraThemeId.FOX_FINANCE -> CalcoraPalette(
        lightScheme = lightColorScheme(
          primary = Color(0xFFC2410C),
          onPrimary = Color.White,
          primaryContainer = Color(0xFFFFEDD5),
          onPrimaryContainer = Color(0xFF431407),
          secondary = Color(0xFF1E3A5F),
          onSecondary = Color.White,
          secondaryContainer = Color(0xFFDBEAFE),
          onSecondaryContainer = Color(0xFF0F1E33),
          tertiary = Color(0xFFD97706),
          onTertiary = Color.White,
          background = Color(0xFFFCF8F5),
          onBackground = Color(0xFF231812),
          surface = Color(0xFFFFFFFF),
          onSurface = Color(0xFF231812),
          surfaceVariant = Color(0xFFF5EAE2),
          onSurfaceVariant = Color(0xFF634E42),
          outline = Color(0xFFDEC8B8),
        ),
        darkScheme = darkColorScheme(
          primary = Color(0xFFFB923C),
          onPrimary = Color(0xFF3D1300),
          primaryContainer = Color(0xFF6B2406),
          onPrimaryContainer = Color(0xFFFFEDD5),
          secondary = Color(0xFF93C5FD),
          onSecondary = Color(0xFF0C2340),
          secondaryContainer = Color(0xFF1E3A5F),
          onSecondaryContainer = Color(0xFFDBEAFE),
          tertiary = Color(0xFFFCD34D),
          onTertiary = Color(0xFF3A2500),
          background = Color(0xFF16100C),
          onBackground = Color(0xFFFDF2EA),
          surface = Color(0xFF211813),
          onSurface = Color(0xFFFDF2EA),
          surfaceVariant = Color(0xFF2F221B),
          onSurfaceVariant = Color(0xFFD4BEB0),
          outline = Color(0xFF4D392E),
        ),
        scienceAccent = Color(0xFF2563EB),
        commerceAccent = Color(0xFFC2410C),
        artsAccent = Color(0xFFD97706),
        universalAccent = Color(0xFF059669),
        previewBg = Color(0xFF431407),
        previewAccent = Color(0xFFFB923C),
      )

      CalcoraThemeId.DOODLE -> CalcoraPalette(
        lightScheme = lightColorScheme(
          primary = Color(0xFFE11D48),
          onPrimary = Color.White,
          primaryContainer = Color(0xFFFFE4E6),
          onPrimaryContainer = Color(0xFF4C0519),
          secondary = Color(0xFF0284C7),
          onSecondary = Color.White,
          secondaryContainer = Color(0xFFE0F2FE),
          onSecondaryContainer = Color(0xFF0C4A6E),
          tertiary = Color(0xFFCA8A04),
          onTertiary = Color.White,
          background = Color(0xFFFFFDF7),
          onBackground = Color(0xFF221F1D),
          surface = Color(0xFFFFFFFF),
          onSurface = Color(0xFF221F1D),
          surfaceVariant = Color(0xFFF6F1E7),
          onSurfaceVariant = Color(0xFF5D564E),
          outline = Color(0xFFDAD0C1),
        ),
        darkScheme = darkColorScheme(
          primary = Color(0xFFFB7185),
          onPrimary = Color(0xFF4C0519),
          primaryContainer = Color(0xFF7A122E),
          onPrimaryContainer = Color(0xFFFFE4E6),
          secondary = Color(0xFF38BDF8),
          onSecondary = Color(0xFF082F49),
          secondaryContainer = Color(0xFF0C4A6E),
          onSecondaryContainer = Color(0xFFE0F2FE),
          tertiary = Color(0xFFFACC15),
          onTertiary = Color(0xFF3B2E00),
          background = Color(0xFF171413),
          onBackground = Color(0xFFFBF7F2),
          surface = Color(0xFF221D1B),
          onSurface = Color(0xFFFBF7F2),
          surfaceVariant = Color(0xFF302926),
          onSurfaceVariant = Color(0xFFD1C5BC),
          outline = Color(0xFF4B403B),
        ),
        scienceAccent = Color(0xFF7C3AED),
        commerceAccent = Color(0xFF0284C7),
        artsAccent = Color(0xFFE11D48),
        universalAccent = Color(0xFF16A34A),
        previewBg = Color(0xFF4C0519),
        previewAccent = Color(0xFFFB7185),
      )

      CalcoraThemeId.DREAMY -> CalcoraPalette(
        lightScheme = lightColorScheme(
          primary = Color(0xFF7E22CE),
          onPrimary = Color.White,
          primaryContainer = Color(0xFFF3E8FF),
          onPrimaryContainer = Color(0xFF3B0764),
          secondary = Color(0xFF4F46E5),
          onSecondary = Color.White,
          secondaryContainer = Color(0xFFE0E7FF),
          onSecondaryContainer = Color(0xFF1E1B4B),
          tertiary = Color(0xFFDB2777),
          onTertiary = Color.White,
          background = Color(0xFFFAF7FD),
          onBackground = Color(0xFF1E142C),
          surface = Color(0xFFFFFFFF),
          onSurface = Color(0xFF1E142C),
          surfaceVariant = Color(0xFFF1E9FA),
          onSurfaceVariant = Color(0xFF5A4C6E),
          outline = Color(0xFFD4C4E8),
        ),
        darkScheme = darkColorScheme(
          primary = Color(0xFFC084FC),
          onPrimary = Color(0xFF2B074D),
          primaryContainer = Color(0xFF4C1D7A),
          onPrimaryContainer = Color(0xFFF3E8FF),
          secondary = Color(0xFFA5B4FC),
          onSecondary = Color(0xFF19164D),
          secondaryContainer = Color(0xFF2E2A70),
          onSecondaryContainer = Color(0xFFE0E7FF),
          tertiary = Color(0xFFF472B6),
          onTertiary = Color(0xFF4D0A2E),
          background = Color(0xFF110B1C),
          onBackground = Color(0xFFF5EEFF),
          surface = Color(0xFF1A1229),
          onSurface = Color(0xFFF5EEFF),
          surfaceVariant = Color(0xFF261C3B),
          onSurfaceVariant = Color(0xFFC8B9DE),
          outline = Color(0xFF40315E),
        ),
        scienceAccent = Color(0xFF7E22CE),
        commerceAccent = Color(0xFF4F46E5),
        artsAccent = Color(0xFFDB2777),
        universalAccent = Color(0xFF0D9488),
        previewBg = Color(0xFF2E1065),
        previewAccent = Color(0xFFFDE047),
      )
    }
}

/**
 * Custom expressive Jetpack Compose Canvas mascot artwork for each of the 7 themes.
 */
@Composable
fun ThemeMascotArtwork(
  themeId: CalcoraThemeId,
  modifier: Modifier = Modifier,
) {
  val palette = CalcoraThemePalettes.forTheme(themeId)
  Canvas(modifier = modifier) {
    val w = size.width
    val h = size.height
    val cx = w / 2f
    val cy = h / 2f
    val r = minOf(w, h) * 0.44f

    // Background badge circle
    drawCircle(color = palette.previewBg, radius = minOf(w, h) * 0.48f, center = Offset(cx, cy))

    when (themeId) {
      CalcoraThemeId.COSMO -> {
        // Stars & Orbit ring + Astronaut helmet
        drawCircle(color = Color(0xFFFDE047), radius = r * 0.06f, center = Offset(cx - r * 0.72f, cy - r * 0.55f))
        drawCircle(color = Color(0xFF22D3EE), radius = r * 0.05f, center = Offset(cx + r * 0.72f, cy - r * 0.45f))
        drawCircle(color = Color.White, radius = r * 0.56f, center = Offset(cx, cy + r * 0.05f))
        drawRoundRect(
          color = Color(0xFF0F172A),
          topLeft = Offset(cx - r * 0.38f, cy - r * 0.22f),
          size = Size(r * 0.76f, r * 0.46f),
          cornerRadius = CornerRadius(r * 0.22f, r * 0.22f),
        )
        drawCircle(color = Color(0xFF22D3EE), radius = r * 0.08f, center = Offset(cx - r * 0.16f, cy - r * 0.02f))
        drawCircle(color = Color(0xFF22D3EE), radius = r * 0.08f, center = Offset(cx + r * 0.16f, cy - r * 0.02f))
        drawLine(
          color = Color(0xFF818CF8),
          start = Offset(cx - r * 0.85f, cy + r * 0.35f),
          end = Offset(cx + r * 0.85f, cy - r * 0.25f),
          strokeWidth = r * 0.06f,
          cap = StrokeCap.Round,
        )
      }

      CalcoraThemeId.BYTE -> {
        // Futuristic Robot Head + glowing visor + antenna
        drawLine(color = Color(0xFF2DD4BF), start = Offset(cx, cy - r * 0.48f), end = Offset(cx, cy - r * 0.75f), strokeWidth = r * 0.08f, cap = StrokeCap.Round)
        drawCircle(color = Color(0xFF60A5FA), radius = r * 0.10f, center = Offset(cx, cy - r * 0.78f))
        drawRoundRect(
          color = Color(0xFFCBD5E1),
          topLeft = Offset(cx - r * 0.55f, cy - r * 0.45f),
          size = Size(r * 1.10f, r * 0.90f),
          cornerRadius = CornerRadius(r * 0.22f, r * 0.22f),
        )
        drawRoundRect(
          color = Color(0xFF0F172A),
          topLeft = Offset(cx - r * 0.40f, cy - r * 0.24f),
          size = Size(r * 0.80f, r * 0.36f),
          cornerRadius = CornerRadius(r * 0.12f, r * 0.12f),
        )
        drawCircle(color = Color(0xFF2DD4BF), radius = r * 0.09f, center = Offset(cx - r * 0.18f, cy - r * 0.06f))
        drawCircle(color = Color(0xFF2DD4BF), radius = r * 0.09f, center = Offset(cx + r * 0.18f, cy - r * 0.06f))
        drawLine(color = Color(0xFF2DD4BF), start = Offset(cx - r * 0.22f, cy + r * 0.26f), end = Offset(cx + r * 0.22f, cy + r * 0.26f), strokeWidth = r * 0.07f, cap = StrokeCap.Round)
      }

      CalcoraThemeId.LABBY -> {
        // Scientist with safety goggles + bubbling Erlenmeyer flask
        drawCircle(color = Color(0xFFFDE68A), radius = r * 0.46f, center = Offset(cx, cy - r * 0.06f))
        // Goggles
        drawRoundRect(
          color = Color(0xFF0284C7),
          topLeft = Offset(cx - r * 0.44f, cy - r * 0.25f),
          size = Size(r * 0.40f, r * 0.28f),
          cornerRadius = CornerRadius(r * 0.10f, r * 0.10f),
        )
        drawRoundRect(
          color = Color(0xFF0284C7),
          topLeft = Offset(cx + r * 0.04f, cy - r * 0.25f),
          size = Size(r * 0.40f, r * 0.28f),
          cornerRadius = CornerRadius(r * 0.10f, r * 0.10f),
        )
        drawCircle(color = Color(0xFFE0F2FE), radius = r * 0.08f, center = Offset(cx - r * 0.24f, cy - r * 0.11f))
        drawCircle(color = Color(0xFFE0F2FE), radius = r * 0.08f, center = Offset(cx + r * 0.24f, cy - r * 0.11f))
        // Bubbling Flask bottom right
        val flask = Path().apply {
          moveTo(cx + r * 0.18f, cy + r * 0.12f)
          lineTo(cx + r * 0.36f, cy + r * 0.12f)
          lineTo(cx + r * 0.56f, cy + r * 0.62f)
          lineTo(cx - r * 0.02f, cy + r * 0.62f)
          close()
        }
        drawPath(flask, color = Color(0xFFA3E635))
        drawCircle(color = Color(0xFF4ADE80), radius = r * 0.07f, center = Offset(cx + r * 0.27f, cy + r * 0.02f))
      }

      CalcoraThemeId.PANDA_MATH -> {
        // Panda ears & face + study book
        drawCircle(color = Color(0xFF1E293B), radius = r * 0.20f, center = Offset(cx - r * 0.38f, cy - r * 0.38f))
        drawCircle(color = Color(0xFF1E293B), radius = r * 0.20f, center = Offset(cx + r * 0.38f, cy - r * 0.38f))
        drawCircle(color = Color.White, radius = r * 0.48f, center = Offset(cx, cy - r * 0.04f))
        drawCircle(color = Color(0xFF1E293B), radius = r * 0.13f, center = Offset(cx - r * 0.18f, cy - r * 0.08f))
        drawCircle(color = Color(0xFF1E293B), radius = r * 0.13f, center = Offset(cx + r * 0.18f, cy - r * 0.08f))
        drawCircle(color = Color.White, radius = r * 0.045f, center = Offset(cx - r * 0.16f, cy - r * 0.10f))
        drawCircle(color = Color.White, radius = r * 0.045f, center = Offset(cx + r * 0.16f, cy - r * 0.10f))
        drawCircle(color = Color(0xFF1E293B), radius = r * 0.06f, center = Offset(cx, cy + r * 0.08f))
        // Book & Mini Calculator
        drawRoundRect(
          color = Color(0xFF6EE7B7),
          topLeft = Offset(cx - r * 0.44f, cy + r * 0.28f),
          size = Size(r * 0.88f, r * 0.34f),
          cornerRadius = CornerRadius(r * 0.08f, r * 0.08f),
        )
        drawLine(color = Color(0xFF064E3B), start = Offset(cx, cy + r * 0.28f), end = Offset(cx, cy + r * 0.62f), strokeWidth = r * 0.05f)
      }

      CalcoraThemeId.FOX_FINANCE -> {
        // Fox ears, face, snout, and crisp necktie
        val leftEar = Path().apply {
          moveTo(cx - r * 0.46f, cy - r * 0.10f)
          lineTo(cx - r * 0.38f, cy - r * 0.62f)
          lineTo(cx - r * 0.06f, cy - r * 0.32f)
          close()
        }
        val rightEar = Path().apply {
          moveTo(cx + r * 0.46f, cy - r * 0.10f)
          lineTo(cx + r * 0.38f, cy - r * 0.62f)
          lineTo(cx + r * 0.06f, cy - r * 0.32f)
          close()
        }
        drawPath(leftEar, color = Color(0xFFFB923C))
        drawPath(rightEar, color = Color(0xFFFB923C))
        drawCircle(color = Color(0xFFEA580C), radius = r * 0.44f, center = Offset(cx, cy - r * 0.05f))
        drawCircle(color = Color.White, radius = r * 0.18f, center = Offset(cx, cy + r * 0.12f))
        drawCircle(color = Color(0xFF1E293B), radius = r * 0.07f, center = Offset(cx - r * 0.16f, cy - r * 0.12f))
        drawCircle(color = Color(0xFF1E293B), radius = r * 0.07f, center = Offset(cx + r * 0.16f, cy - r * 0.12f))
        drawCircle(color = Color(0xFF0F172A), radius = r * 0.06f, center = Offset(cx, cy + r * 0.08f))
        // Necktie
        val tie = Path().apply {
          moveTo(cx - r * 0.10f, cy + r * 0.32f)
          lineTo(cx + r * 0.10f, cy + r * 0.32f)
          lineTo(cx + r * 0.15f, cy + r * 0.62f)
          lineTo(cx, cy + r * 0.76f)
          lineTo(cx - r * 0.15f, cy + r * 0.62f)
          close()
        }
        drawPath(tie, color = Color(0xFF93C5FD))
      }

      CalcoraThemeId.DOODLE -> {
        // Hand-drawn Notebook Sheet + Pencil & Star
        drawRoundRect(
          color = Color(0xFFFFFBEB),
          topLeft = Offset(cx - r * 0.50f, cy - r * 0.52f),
          size = Size(r * 1.00f, r * 1.04f),
          cornerRadius = CornerRadius(r * 0.14f, r * 0.14f),
        )
        drawRoundRect(
          color = Color(0xFFFB7185),
          topLeft = Offset(cx - r * 0.50f, cy - r * 0.52f),
          size = Size(r * 1.00f, r * 1.04f),
          cornerRadius = CornerRadius(r * 0.14f, r * 0.14f),
          style = Stroke(width = r * 0.08f),
        )
        drawLine(color = Color(0xFF38BDF8), start = Offset(cx - r * 0.30f, cy - r * 0.18f), end = Offset(cx + r * 0.30f, cy - r * 0.18f), strokeWidth = r * 0.07f, cap = StrokeCap.Round)
        drawLine(color = Color(0xFF38BDF8), start = Offset(cx - r * 0.30f, cy + r * 0.06f), end = Offset(cx + r * 0.18f, cy + r * 0.06f), strokeWidth = r * 0.07f, cap = StrokeCap.Round)
        // Pencil stroke
        drawLine(color = Color(0xFFFACC15), start = Offset(cx - r * 0.18f, cy + r * 0.38f), end = Offset(cx + r * 0.36f, cy - r * 0.14f), strokeWidth = r * 0.14f, cap = StrokeCap.Round)
      }

      CalcoraThemeId.DREAMY -> {
        // Crescent Moon + Twinkling Stars
        drawCircle(color = Color(0xFFFDE047), radius = r * 0.44f, center = Offset(cx - r * 0.06f, cy))
        drawCircle(color = palette.previewBg, radius = r * 0.36f, center = Offset(cx + r * 0.14f, cy - r * 0.08f))
        // Twinkling Stars
        drawCircle(color = Color(0xFFC084FC), radius = r * 0.08f, center = Offset(cx + r * 0.38f, cy - r * 0.32f))
        drawCircle(color = Color(0xFFF472B6), radius = r * 0.06f, center = Offset(cx + r * 0.48f, cy + r * 0.18f))
        drawCircle(color = Color(0xFFA5B4FC), radius = r * 0.05f, center = Offset(cx - r * 0.48f, cy - r * 0.40f))
        // Mini star spark
        val spark = Path().apply {
          moveTo(cx + r * 0.24f, cy + r * 0.28f)
          lineTo(cx + r * 0.28f, cy + r * 0.40f)
          lineTo(cx + r * 0.40f, cy + r * 0.44f)
          lineTo(cx + r * 0.28f, cy + r * 0.48f)
          lineTo(cx + r * 0.24f, cy + r * 0.60f)
          lineTo(cx + r * 0.20f, cy + r * 0.48f)
          lineTo(cx + r * 0.08f, cy + r * 0.44f)
          lineTo(cx + r * 0.20f, cy + r * 0.40f)
          close()
        }
        drawPath(spark, color = Color(0xFFFDE047), style = Stroke(width = r * 0.03f, join = StrokeJoin.Round))
      }
    }
  }
}
