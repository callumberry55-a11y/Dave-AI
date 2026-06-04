package com.example.daveai.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = primaryLight,
    onPrimary = onPrimaryLight,
    primaryContainer = primaryContainerLight,
    onPrimaryContainer = onPrimaryContainerLight,
    secondary = secondaryLight,
    onSecondary = onSecondaryLight,
    secondaryContainer = secondaryContainerLight,
    onSecondaryContainer = onSecondaryContainerLight,
    tertiary = tertiaryLight,
    onTertiary = onTertiaryLight,
    tertiaryContainer = tertiaryContainerLight,
    onTertiaryContainer = onTertiaryContainerLight,
    error = errorLight,
    onError = onErrorLight,
    errorContainer = errorContainerLight,
    onErrorContainer = onErrorContainerLight,
    background = backgroundLight,
    onBackground = onBackgroundLight,
    surface = surfaceLight,
    onSurface = onSurfaceLight,
    surfaceVariant = surfaceVariantLight,
    onSurfaceVariant = onSurfaceVariantLight,
    outline = outlineLight,
)

private val DarkColorScheme = darkColorScheme(
    primary = primaryDark,
    onPrimary = onPrimaryDark,
    primaryContainer = primaryContainerDark,
    onPrimaryContainer = onPrimaryContainerDark,
    secondary = secondaryDark,
    onSecondary = onSecondaryDark,
    secondaryContainer = secondaryContainerDark,
    onSecondaryContainer = onSecondaryContainerDark,
    tertiary = tertiaryDark,
    onTertiary = onTertiaryDark,
    tertiaryContainer = tertiaryContainerDark,
    onTertiaryContainer = onTertiaryContainerDark,
    error = errorDark,
    onError = onErrorDark,
    errorContainer = errorContainerDark,
    onErrorContainer = onErrorContainerDark,
    background = backgroundDark,
    onBackground = onBackgroundDark,
    surface = surfaceDark,
    onSurface = onSurfaceDark,
    surfaceVariant = surfaceVariantDark,
    onSurfaceVariant = onSurfaceVariantDark,
    outline = outlineDark,
)

@Composable
fun DaveAITheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    primaryColorOverride: Color? = null,
    typographyStyle: String = "MODERN",
    content: @Composable () -> Unit
) {
    if (com.example.daveai.BuildConfig.FLAVOR == "developer") {
        MaterialTheme(
            colorScheme = BetaColorScheme,
            typography = BetaTypography,
            content = content
        )
        return
    }

    val baseColorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val colorScheme = if (primaryColorOverride != null) {
        baseColorScheme.copy(
            primary = primaryColorOverride,
            outline = primaryColorOverride.copy(alpha = 0.5f)
        )
    } else {
        baseColorScheme
    }

    // Dynamic Typography (Phase 16)
    val fontFamily = when (typographyStyle) {
        "MONO" -> FontFamily.Monospace
        "SERIF" -> FontFamily.Serif
        else -> FontFamily.SansSerif
    }

    val dynamicTypography = Typography(
        displayLarge = Typography.displayLarge.copy(
            fontFamily = fontFamily,
            fontWeight = FontWeight.ExtraLight,
            letterSpacing = 2.sp
        ),
        headlineLarge = Typography.headlineLarge.copy(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Light,
            letterSpacing = 1.sp
        ),
        titleLarge = Typography.titleLarge.copy(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Normal,
            letterSpacing = 1.sp
        ),
        bodyLarge = Typography.bodyLarge.copy(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Light,
            lineHeight = 28.sp,
            letterSpacing = 0.5.sp
        ),
        labelMedium = Typography.labelMedium.copy(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Medium,
            letterSpacing = 1.5.sp
        ),
        labelSmall = Typography.labelSmall.copy(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Bold,
            letterSpacing = 3.sp
        )
    )

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = android.graphics.Color.TRANSPARENT
            window.navigationBarColor = android.graphics.Color.TRANSPARENT
            WindowCompat.setDecorFitsSystemWindows(window, false)
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = dynamicTypography,
        content = content
    )
}
