// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Aninda Sundar Howlader (GRU953)

package dev.gru953.declutter.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.gru953.declutter.ui.UiState
import dev.gru953.declutter.ui.components.BrandMark
import dev.gru953.declutter.ui.components.Gap
import dev.gru953.declutter.ui.components.SectionHeading
import dev.gru953.declutter.ui.theme.GruSpace
import dev.gru953.declutter.ui.theme.LocalGruColours

@Composable
fun AboutScreen(state: UiState, modifier: Modifier = Modifier) {
    val gru = LocalGruColours.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(GruSpace.lg),
        verticalArrangement = Arrangement.spacedBy(GruSpace.sm),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(GruSpace.md),
        ) {
            BrandMark(size = 40.dp)
            Column {
                Text("Declutter", style = MaterialTheme.typography.headlineMedium)
                Text(
                    "by GRU953",
                    style = MaterialTheme.typography.bodyMedium,
                    color = gru.inkMuted,
                )
            }
        }

        Gap(GruSpace.sm)
        Text(
            "Declutter removes preinstalled apps you did not choose, without root.",
            style = MaterialTheme.typography.bodyLarge,
        )
        Text(
            "সহজ প্রযুক্তি। " +
                "সবার জন্য। · " +
                "Simple technology. For everyone.",
            style = MaterialTheme.typography.bodyMedium,
            color = gru.inkMuted,
        )

        SectionHeading("What it does not do")
        Bullets(
            "It does not need root, and it does not unlock anything permanently.",
            "It does not delete an app from the phone's storage. Removing means removing " +
                "it for you, so the phone can put it back.",
            "It does not touch anything the phone needs to start, make calls, receive " +
                "emergency warnings or install security updates. That refusal cannot be " +
                "overridden.",
            "It does not work in a work profile or a second user, only in the phone's " +
                "main profile.",
            "It does not send anything anywhere. There is no account, no analytics and no " +
                "internet permission.",
        )

        SectionHeading("The catalogue")
        Text(
            "Declutter carries ${state.catalogueSize} researched packages, last reviewed " +
                "on ${state.catalogueReviewed}. The ratings were built by reading the " +
                "Motorola Edge 50 Fusion's own firmware, Android's source, and the reports " +
                "of people who have broken their phones doing this. Package lists change " +
                "with firmware, network and region, so an entry is evidence, not a promise.",
            style = MaterialTheme.typography.bodyLarge,
        )
        Text(
            "Anything the catalogue has not checked is shown as unchecked and is never " +
                "offered as safe. ‘Not checked’ is a more useful thing to be " +
                "told than a guess.",
            style = MaterialTheme.typography.bodyLarge,
        )

        SectionHeading("Why some apps are simply refused")
        Text(
            "Declutter is stricter than the debloating tools written for people who can " +
                "reflash a phone. Google Play services, the Play Store and the component " +
                "that draws web pages inside apps are all things an expert elsewhere is " +
                "allowed to remove at their own risk. Here they are refused outright.",
            style = MaterialTheme.typography.bodyLarge,
        )
        Text(
                "That is a judgement, not a fact anybody else has agreed to. It rests on " +
                "two things: removing them breaks a great deal at once, and losing the " +
                "Play Store takes away the means to undo everything else. If you can put " +
                "a phone back together from a computer, another tool will suit you better.",
            style = MaterialTheme.typography.bodyLarge,
        )

        SectionHeading("After a system update")
        Text(
            "A Motorola system update can quietly put removed apps back. If they " +
                "reappear, the change log is still here, and you can remove them again.",
            style = MaterialTheme.typography.bodyLarge,
        )

        SectionHeading("This phone")
        Text(
            state.securityPatch?.let {
                "Android security patch level: $it. That is the operating system's own " +
                    "patch date. Parts of Android -- including the debugging service " +
                    "Shizuku relies on -- are updated separately through Google Play " +
                    "system updates, so this date alone does not tell you whether " +
                    "everything is up to date. Settings \u203a Security & privacy \u203a " +
                    "System & updates shows both."
            } ?: "This phone does not report a security patch level.",
            style = MaterialTheme.typography.bodyMedium,
            color = gru.inkMuted,
        )

        SectionHeading("Licence")
        Text(
            "Code: Apache-2.0. Use it, change it, sell it, no permission needed.",
            style = MaterialTheme.typography.bodyMedium,
        )
        Text(
            "Not licensed: the name GRU953, the Soaring Bird mark, the app-icon tile and " +
                "the GRU953 wordmark. They identify the studio, so they stay with it.",
            style = MaterialTheme.typography.bodyMedium,
        )
        Text(
            "Shizuku's API is MIT-licensed and AndroidHiddenApiBypass is Apache-2.0. Sora, " +
                "Noto Sans and JetBrains Mono are SIL OFL 1.1, bundled subset to Latin; " +
                "their licences are inside the app under assets/licences.",
            style = MaterialTheme.typography.bodyMedium,
            color = gru.inkMuted,
        )
        Text(
            "Not legal advice.",
            style = MaterialTheme.typography.bodySmall,
            color = gru.inkSubtle,
        )
        Gap(GruSpace.xxl)
    }
}

@Composable
private fun Bullets(vararg lines: String) {
    Column(verticalArrangement = Arrangement.spacedBy(GruSpace.sm)) {
        lines.forEach { line ->
            Row(horizontalArrangement = Arrangement.spacedBy(GruSpace.sm)) {
                Text("•", style = MaterialTheme.typography.bodyLarge)
                Text(line, style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}
