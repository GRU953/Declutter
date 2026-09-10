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
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.gru953.declutter.catalogue.Advice
import dev.gru953.declutter.catalogue.Risk
import dev.gru953.declutter.domain.AppRow
import dev.gru953.declutter.domain.Block
import dev.gru953.declutter.domain.PackageState
import dev.gru953.declutter.ui.components.ConfidenceNote
import dev.gru953.declutter.ui.components.Gap
import dev.gru953.declutter.ui.components.MonoText
import dev.gru953.declutter.ui.components.Notice
import dev.gru953.declutter.ui.components.NoticeTone
import dev.gru953.declutter.ui.components.RiskBadge
import dev.gru953.declutter.ui.components.formatBytes
import dev.gru953.declutter.ui.theme.GruSpace
import dev.gru953.declutter.ui.theme.LocalGruColours

/**
 * One app, in full. The consequence of removing it is shown here, before the action, rather
 * than being filed away in a help page -- which is the only place it is any use.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailSheet(
    row: AppRow,
    onDismiss: () -> Unit,
    onRemove: () -> Unit,
    onDisable: () -> Unit,
    onRestore: () -> Unit,
) {
    val gru = LocalGruColours.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(start = GruSpace.lg, end = GruSpace.lg, bottom = GruSpace.xxl),
            verticalArrangement = Arrangement.spacedBy(GruSpace.md),
        ) {
            Text(row.displayName, style = MaterialTheme.typography.headlineSmall)
            MonoText(row.pkg, maxLines = 2)

            Row(horizontalArrangement = Arrangement.spacedBy(GruSpace.sm)) {
                RiskBadge(row.risk)
            }

            Text(
                text = buildString {
                    append(row.vendor.label)
                    row.versionName?.let { append(" · version "); append(it) }
                    if (row.installedSizeBytes > 0) {
                        append(" · "); append(formatBytes(row.installedSizeBytes))
                    }
                    append(
                        when (row.state) {
                            PackageState.ACTIVE -> ""
                            PackageState.DISABLED -> " · currently turned off"
                            PackageState.REMOVED -> " · currently removed"
                        },
                    )
                },
                style = MaterialTheme.typography.bodySmall,
                color = gru.inkSubtle,
            )

            if (row.entry != null) {
                Text("What it does", style = MaterialTheme.typography.titleMedium)
                Text(row.entry.what, style = MaterialTheme.typography.bodyLarge)

                Text("What you lose", style = MaterialTheme.typography.titleMedium)
                Text(row.entry.loses, style = MaterialTheme.typography.bodyLarge)

                if (row.entry.breaks.isNotEmpty()) {
                    Notice(
                        tone = NoticeTone.CAUTION,
                        title = "Other things depend on it",
                        body = "These stop working properly without it: " +
                            row.entry.breaks.joinToString(", "),
                    )
                }
                ConfidenceNote(row.entry.confidence)
            } else {
                Notice(
                    tone = NoticeTone.CAUTION,
                    title = "Declutter has not checked this one",
                    body = "It is not in the catalogue, so there is nothing reliable to " +
                        "tell you about it. That is not the same as it being safe. If you " +
                        "want to act on it anyway, turning it off is the reversible choice " +
                        "-- but the honest advice is to leave it alone.",
                )
            }

            if (row.nonDisableable) {
                Notice(
                    tone = NoticeTone.INFO,
                    title = "Motorola marks this one ‘must stay on’",
                    body = "Its Settings screen greys out the buttons for this package. " +
                        "That is Motorola's own advice, and worth taking. It is not a " +
                        "safety net though: the greying-out lives in the Settings app, " +
                        "not in Android itself, so a change made this way would go " +
                        "through anyway. Treat it as a reason to leave this one alone.",
                )
            }

            when (row.block) {
                Block.PROTECTED -> Notice(
                    tone = NoticeTone.STOP,
                    title = "Declutter will not change this one",
                    body = (row.blockReason ?: "It is on the protected list.") +
                        " There is no way to override this, on purpose: a phone that will " +
                        "not start cannot be repaired from inside itself.",
                )
                Block.IN_USE -> Notice(
                    tone = NoticeTone.STOP,
                    title = "You are using this one right now",
                    body = row.blockReason.orEmpty(),
                )
                Block.USER_INSTALLED -> Notice(
                    tone = NoticeTone.INFO,
                    title = "You installed this one yourself",
                    body = row.blockReason.orEmpty(),
                )
                Block.NONE -> Unit
            }

            if (row.state != PackageState.ACTIVE) {
                Gap(GruSpace.sm)
                Button(onClick = onRestore, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        if (row.state == PackageState.DISABLED) "Turn back on" else "Put back",
                    )
                }
            } else if (row.actionable) {
                Notice(
                    tone = NoticeTone.INFO,
                    title = "Before you remove it",
                    body = "Removing it takes it away for you only. The copy the phone " +
                        "was built with stays on the phone, so Declutter can put it back " +
                        "-- as long as the phone still works, or a computer you have " +
                        "already authorised is to hand. If this app has been updated " +
                        "through the Play Store, that update is discarded: putting it " +
                        "back gives you the version the phone shipped with.",
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(GruSpace.sm),
                ) {
                    Button(onClick = onRemove, modifier = Modifier.weight(1f)) {
                        Text("Remove")
                    }
                    OutlinedButton(onClick = onDisable, modifier = Modifier.weight(1f)) {
                        Text("Turn off instead")
                    }
                }
                if (row.risk == Risk.EXPERT || row.entry?.advice == Advice.DISABLE) {
                    Text(
                        "For this one, turning it off is the wiser choice. It is instantly " +
                            "reversible, and it keeps any updates it has had.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = gru.inkMuted,
                    )
                }
            }
            Gap(24.dp)
        }
    }
}
