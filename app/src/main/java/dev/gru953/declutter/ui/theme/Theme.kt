// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Aninda Sundar Howlader (GRU953)

package dev.gru953.declutter.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

/**
 * Both themes are built from the same role tokens, so no screen needs a second stylesheet.
 * Material You dynamic colour is deliberately not used: this app's whole job is to tell the
 * user how risky an action is, and a palette borrowed from their wallpaper cannot promise
 * that "safe" and "unsafe" stay far enough apart to be told apart.
 */
@Composable
fun DeclutterTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colours = if (darkTheme) DarkGruColours else LightGruColours
    CompositionLocalProvider(LocalGruColours provides colours) {
        MaterialTheme(
            colorScheme = if (darkTheme) GruDarkScheme else GruLightScheme,
            typography = GruTypography,
            content = content,
        )
    }
}
