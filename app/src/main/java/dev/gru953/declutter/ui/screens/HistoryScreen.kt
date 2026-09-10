// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Aninda Sundar Howlader (GRU953)

package dev.gru953.declutter.ui.screens

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import dev.gru953.declutter.R
import dev.gru953.declutter.data.HistoryBatch
import dev.gru953.declutter.data.formatStamp
import dev.gru953.declutter.ui.UiState
import dev.gru953.declutter.ui.components.MonoText
import dev.gru953.declutter.ui.components.Notice
import dev.gru953.declutter.ui.components.NoticeTone
import dev.gru953.declutter.ui.theme.GruSpace
import dev.gru953.declutter.ui.theme.LocalGruColours

/**
 * Every change, with the means to undo it.
 *
 * The undo button carries an honest caveat, because it has a real limit: it only works
 * while the phone still works well enough to open this app. That is stated here rather
 * than discovered later.
 */
@Composable
fun HistoryScreen(
    state: UiState,
    onUndoBatch: (HistoryBatch) -> Unit,
    onRestoreEverything: () -> Unit,
    historyText: () -> String,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val gru = LocalGruColours.current
    val anythingChanged = state.rows.any {
        it.state != dev.gru953.declutter.domain.PackageState.ACTIVE
    }

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            start = GruSpace.lg, end = GruSpace.lg, top = GruSpace.sm, bottom = 96.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(GruSpace.md),
    ) {
        item {
            Notice(
                tone = NoticeTone.INFO,
                title = "What undo can and cannot do",
                body = "Every removal here can be undone, because the copy your phone " +
                    "was built with is still on it. Two honest limits. Undo runs from " +
                    "inside this app, so if a change ever left the phone unusable, this " +
                    "button is exactly what you could not reach -- which is why Declutter " +
                    "refuses to touch anything load-bearing and stops a batch at the " +
                    "first failure. And an app that had been updated through the Play " +
                    "Store comes back as the version the phone shipped with.",
            )
        }

        if (anythingChanged) {
            item {
                Button(onClick = onRestoreEverything, modifier = Modifier.fillMaxWidth()) {
                    Text("Put everything back")
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(GruSpace.sm),
            ) {
                OutlinedButton(
                    onClick = {
                        runCatching {
                            val send = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TITLE, "Declutter change log")
                                putExtra(Intent.EXTRA_TEXT, historyText())
                            }
                            context.startActivity(
                                Intent.createChooser(send, "Share the change log")
                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                            )
                        }
                    },
                    modifier = Modifier.weight(1f),
                ) { Text("Share the log") }
            }
        }

        if (state.history.isEmpty()) {
            item {
                Text(
                    "Nothing has been changed yet.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = gru.inkMuted,
                    modifier = Modifier.padding(top = GruSpace.md),
                )
            }
        }

        items(state.history, key = { it.id }) { batch ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.surfaceContainerLow,
                        RoundedCornerShape(GruSpace.md),
                    )
                    .padding(GruSpace.md),
                verticalArrangement = Arrangement.spacedBy(GruSpace.xs),
            ) {
                Text(batch.title, style = MaterialTheme.typography.titleSmall)
                Text(
                    formatStamp(batch.at),
                    style = MaterialTheme.typography.bodySmall,
                    color = gru.inkSubtle,
                )
                batch.items.forEach { item ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(GruSpace.sm),
                        modifier = Modifier.padding(top = GruSpace.xs),
                    ) {
                        Icon(
                            painter = if (item.succeeded) {
                                painterResource(R.drawable.ic_ok)
                            } else {
                                painterResource(R.drawable.ic_failed)
                            },
                            contentDescription = if (item.succeeded) "Done" else "Failed",
                            tint = if (item.succeeded) gru.success else gru.danger,
                            modifier = Modifier.size(16.dp),
                        )
                        Column(Modifier.weight(1f)) {
                            Text(
                                "${item.parsedAction?.pastTense ?: "Changed"}: ${item.displayName}",
                                style = MaterialTheme.typography.bodyMedium,
                            )
                            MonoText(item.pkg)
                            item.detail?.let {
                                Text(
                                    it,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = gru.danger,
                                )
                            }
                        }
                    }
                }
                if (batch.restorable.isNotEmpty()) {
                    OutlinedButton(
                        onClick = { onUndoBatch(batch) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = GruSpace.sm),
                    ) { Text("Undo this batch") }
                }
            }
        }
    }
}
