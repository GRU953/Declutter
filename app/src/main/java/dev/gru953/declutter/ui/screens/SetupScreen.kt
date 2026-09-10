// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Aninda Sundar Howlader (GRU953)

package dev.gru953.declutter.ui.screens

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import dev.gru953.declutter.shizuku.ShizukuState
import dev.gru953.declutter.ui.UiState
import dev.gru953.declutter.ui.components.Gap
import dev.gru953.declutter.ui.components.MonoText
import dev.gru953.declutter.ui.components.Notice
import dev.gru953.declutter.ui.components.NoticeTone
import dev.gru953.declutter.ui.components.SectionHeading
import dev.gru953.declutter.ui.theme.GruSpace
import dev.gru953.declutter.ui.theme.LocalGruColours

/**
 * The setup screen, and the honest one.
 *
 * Shizuku's service stops on every restart -- that is documented behaviour, not a fault --
 * so this screen is not a one-time wizard that disappears. It stays reachable, and it says
 * what to do in the order a person actually has to do it.
 */
@Composable
fun SetupScreen(
    state: UiState,
    onRecheck: () -> Unit,
    onGrant: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val gru = LocalGruColours.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(GruSpace.lg),
        verticalArrangement = Arrangement.spacedBy(GruSpace.md),
    ) {
        when {
            state.debuggingBlockedByPolicy -> Notice(
                tone = NoticeTone.STOP,
                title = "This phone does not allow developer options",
                body = "Something is managing this phone -- an employer, a school, or a " +
                    "device-management profile -- and it has switched developer options " +
                    "off. Declutter cannot work without them, and there is no way round " +
                    "it from inside the phone.",
            )

            state.runningOutsideUserZero -> Notice(
                tone = NoticeTone.STOP,
                title = "This is not the phone's main profile",
                body = "Declutter only changes the main profile, so that it can never " +
                    "touch a work profile somebody else is responsible for. Switch to " +
                    "the main profile and open it again.",
            )

            state.shizuku == ShizukuState.READY -> Notice(
                tone = NoticeTone.GOOD,
                title = "Ready",
                body = "Shizuku is running and Declutter has permission. Remember that " +
                    "you will need to start Shizuku again after the phone restarts.",
            )

            state.shizuku == ShizukuState.NEEDS_PERMISSION -> Notice(
                tone = NoticeTone.CAUTION,
                title = "Shizuku is running -- it just needs your permission",
                body = "Tap below, then tap Allow in the box that appears.",
                action = { Button(onClick = onGrant) { Text("Ask for permission") } },
            )

            state.shizuku == ShizukuState.DENIED -> Notice(
                tone = NoticeTone.STOP,
                title = "Permission was refused",
                body = "Open Shizuku, find Declutter in its list of authorised apps, and " +
                    "allow it there.",
            )

            else -> Notice(
                tone = NoticeTone.CAUTION,
                title = "Shizuku is not running",
                body = "This is normal after a restart. Follow the steps below, then come " +
                    "back and tap ‘Check again’.",
            )
        }

        Notice(
            tone = NoticeTone.CAUTION,
            title = "Do this on a network you trust",
            body = "While wireless debugging is switched on, anyone on the same Wi-Fi can " +
                "reach a port that grants shell-level access to this phone. Android is " +
                "supposed to require pairing first, and in 2026 a flaw was found that let " +
                "that check be bypassed. So: use your own network, not a café's, and " +
                "switch wireless debugging off when you have finished. Step 6 below is " +
                "exactly that.",
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(GruSpace.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Button(onClick = onRecheck) { Text("Check again") }
            OutlinedButton(onClick = {
                runCatching {
                    context.startActivity(
                        Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                    )
                }
            }) { Text("Open developer options") }
        }

        SectionHeading("What Shizuku is, in one paragraph")
        Text(
            "Android will not let an ordinary app remove the apps your phone came with. " +
                "Shizuku is a small free app that borrows the same temporary powers a " +
                "computer has when it is plugged in for development, and lends them to " +
                "apps you approve. No root, no unlocked bootloader, nothing permanent. " +
                "The powers disappear when the phone restarts.",
            style = MaterialTheme.typography.bodyLarge,
        )

        SectionHeading("Step 1 -- install Shizuku")
        Steps(
            "Open the link below. It goes to Shizuku's own releases page.",
            "Download the file whose name ends in .apk.",
            "Open the downloaded file and allow the install when Android asks.",
        )
        Text(
            "Get it from there rather than the Play Store. The Play copy lags behind and " +
                "has been reported not to start on recent Android versions.",
            style = MaterialTheme.typography.bodyMedium,
            color = gru.inkMuted,
        )
        TextButton(onClick = {
            runCatching {
                context.startActivity(
                    Intent(Intent.ACTION_VIEW, SHIZUKU_RELEASES.toUri())
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                )
            }
        }) { Text("Open Shizuku's releases page") }

        SectionHeading("Step 2 -- turn on developer options")
        Steps(
            "Open Settings.",
            "Go to About phone. On some Motorola builds it is Settings › System › About phone.",
            "Tap Build number seven times. A message says you are now a developer.",
        )

        SectionHeading("Step 3 -- turn on wireless debugging")
        Steps(
            "Make sure the phone is connected to Wi-Fi. This will not work on mobile data.",
            "In Settings, find Developer options -- usually under System.",
            "Switch on Wireless debugging. In the box that appears, tick " +
                "‘Always allow on this network’ before you accept -- that tick " +
                "is what lets Shizuku start itself again later.",
        )

        SectionHeading("Step 4 -- pair Shizuku with the phone")
        Steps(
            "In Wireless debugging, tap ‘Pair device with pairing code’. " +
                "A six-digit code and a number appear.",
            "Leave that box open. Do not close it, and do not press Back.",
            "Pull down the notification shade and open Shizuku's pairing notification, " +
                "then type the six-digit code.",
            "When Shizuku says it is paired, go back to Shizuku and tap Start.",
        )
        Notice(
            tone = NoticeTone.CAUTION,
            title = "The one step people get wrong",
            body = "The pairing box has to stay open. Both the six-digit code and the " +
                "number beside it change every time you open it, so closing the box and " +
                "reopening it makes the code you just typed wrong.",
        )

        SectionHeading("Step 5 -- come back here")
        Steps(
            "Return to Declutter and tap ‘Check again’.",
            "Tap ‘Ask for permission’, then Allow.",
        )

        SectionHeading("The other route, if you have a computer")
        Steps(
            "Turn on USB debugging in developer options and connect the phone by cable.",
            "Open Shizuku and tap ‘Start by connecting to a computer’.",
            "Tap ‘View command’, then ‘Copy’.",
            "Paste that command into a terminal on the computer, where ADB is installed.",
        )
        Notice(
            tone = NoticeTone.INFO,
            title = "Why Declutter does not print the command here",
            body = "Shizuku's start command points at a file whose path is different on " +
                "every phone and changes whenever Shizuku updates. Any command written " +
                "into an app like this one would be wrong for you. Shizuku's own Copy " +
                "button always gives the right one.",
        )

        SectionHeading("Step 6 -- when you have finished, switch it back off")
        Steps(
            "Do all your removing first. Switching wireless debugging off can stop " +
                "Shizuku, so make this the last thing you do.",
            "Settings › System › Developer options › Wireless debugging › off.",
            "If you would rather also drop the pairing, open Paired devices in that same " +
                "screen and tap Forget.",
        )

        SectionHeading("After a restart")
        Text(
            "Shizuku stops when the phone restarts. That is Android's doing, not a fault, " +
                "and on a phone without root there is no way round it. You do not have to " +
                "pair again -- the pairing is remembered. Switch Wireless debugging back " +
                "on, open Shizuku, tap Start, and you are done in about fifteen seconds. " +
                "If it will not start, switch Wireless debugging off and on again.",
            style = MaterialTheme.typography.bodyLarge,
        )
        Text(
            "Shizuku can also start itself after a restart on Android 13 and later, if the " +
                "network was ticked as trusted. Whether that works on a Motorola is not " +
                "something anybody has confirmed, so treat it as a bonus rather than the " +
                "plan.",
            style = MaterialTheme.typography.bodyMedium,
            color = gru.inkMuted,
        )

        SectionHeading("Worth doing before you remove anything")
        Text(
            "Plug the phone into a computer once, with USB debugging on, and accept the " +
                "‘Allow debugging?’ box. You may never need it. But if a change " +
                "ever left the phone's screen unusable, an authorised computer is what " +
                "lets you put things back without wiping the phone -- and Android will not " +
                "accept a computer it has not already been shown.",
            style = MaterialTheme.typography.bodyLarge,
        )

        SectionHeading("What ‘reversible’ honestly means")
        Bullets(
            "The app comes back. Removing a preinstalled app takes it away for you; the " +
                "copy the phone was built with is untouched and Declutter can put it back.",
            "Its updates do not come back. If the app had been updated through the Play " +
                "Store, you get the version the phone shipped with.",
            "A few preinstalled apps are not built into the phone at all, and those " +
                "cannot be restored -- only downloaded again from a store.",
            "Undo needs a working phone or an authorised computer. Declutter refuses to " +
                "touch anything load-bearing precisely so that this stays true.",
            "A factory reset brings everything back, at the cost of everything else.",
        )

        SectionHeading("One more thing, on this phone in particular")
        Text(
            "Motorola's own Battery Care software stops background apps even when you " +
                "have told it not to, and it will stop Shizuku. If Shizuku keeps dying " +
                "while you are still using the phone, set Shizuku's battery use to " +
                "Unrestricted in Settings › Apps › Shizuku › Battery.",
            style = MaterialTheme.typography.bodyLarge,
        )
        Gap()
    }
}

@Composable
private fun Bullets(vararg lines: String) {
    Column(verticalArrangement = Arrangement.spacedBy(GruSpace.sm)) {
        lines.forEach { line ->
            Row(horizontalArrangement = Arrangement.spacedBy(GruSpace.sm)) {
                Text("\u2022", style = MaterialTheme.typography.bodyLarge)
                Text(line, style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}

@Composable
private fun Steps(vararg steps: String) {
    Column(verticalArrangement = Arrangement.spacedBy(GruSpace.sm)) {
        steps.forEachIndexed { index, step ->
            Row(horizontalArrangement = Arrangement.spacedBy(GruSpace.md)) {
                Text(
                    text = "${index + 1}",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                    ),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp),
                )
                Text(step, style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}

private const val SHIZUKU_RELEASES = "https://github.com/RikkaApps/Shizuku/releases/latest"
