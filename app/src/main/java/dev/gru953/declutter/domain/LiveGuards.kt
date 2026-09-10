// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Aninda Sundar Howlader (GRU953)

package dev.gru953.declutter.domain

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.provider.Telephony
import android.telecom.TelecomManager
import android.util.Log
import android.view.inputmethod.InputMethodManager

/**
 * The guards that cannot be written down in advance, because the answers live on the phone.
 *
 * A static risk rating cannot protect the launcher: on this very handset the Motorola
 * launcher is rated only "expert" by the community lists. So the packages holding a job the
 * user needs *right now* are resolved on every scan and merged into the blocklist, and
 * never cached to disk -- the user may change their keyboard between two scans.
 */
class LiveGuards private constructor(
    private val holders: Map<String, String>,
) {

    /** The reason this package is in use, or null. */
    fun reasonFor(pkg: String): String? = holders[pkg]

    companion object {
        private const val TAG = "LiveGuards"

        fun resolve(context: Context): LiveGuards {
            val pm = context.packageManager
            val holders = mutableMapOf<String, String>()

            fun claim(pkg: String?, reason: String) {
                if (!pkg.isNullOrBlank()) holders.putIfAbsent(pkg, reason)
            }

            // Our own package, whatever it has been renamed to.
            claim(context.packageName, "This is Declutter itself.")

            // The launcher. Only protected while it is the only one that would answer Home.
            runCatching {
                val home = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
                val candidates = pm.queryIntentActivities(home, PackageManager.MATCH_DEFAULT_ONLY)
                    .map { it.activityInfo.packageName }
                    .distinct()
                val current = pm.resolveActivity(home, PackageManager.MATCH_DEFAULT_ONLY)
                    ?.activityInfo?.packageName
                if (candidates.size <= 1) {
                    claim(
                        current ?: candidates.firstOrNull(),
                        "It is your only home screen. Removing it would leave the Home " +
                            "button doing nothing. Install another launcher first.",
                    )
                } else {
                    claim(current, "It is your home screen right now. Change your default launcher first.")
                }
            }.onFailure { Log.w(TAG, "launcher guard failed", it) }

            // The keyboard. Losing the only one means the user cannot even type a PIN.
            runCatching {
                val imm = context.getSystemService(InputMethodManager::class.java)
                val enabled = imm?.enabledInputMethodList.orEmpty()
                val current = Settings.Secure.getString(
                    context.contentResolver,
                    Settings.Secure.DEFAULT_INPUT_METHOD,
                )?.substringBefore('/')
                if (enabled.size <= 1) {
                    val only = enabled.firstOrNull()?.packageName ?: current
                    claim(
                        only,
                        "It is your only keyboard. Without it you could not type at all -- " +
                            "not even to install a replacement. Add another keyboard first.",
                    )
                } else {
                    claim(current, "It is your keyboard right now. Switch keyboards first.")
                }
                enabled.forEach { info ->
                    if (enabled.size <= 1) claim(info.packageName, "It is your only keyboard.")
                }
            }.onFailure { Log.w(TAG, "keyboard guard failed", it) }

            // The dialler and the SMS app. Losing SMS means losing one-time passcodes.
            runCatching {
                val telecom = context.getSystemService(TelecomManager::class.java)
                claim(
                    telecom?.defaultDialerPackage,
                    "It is the app that makes your phone calls.",
                )
            }.onFailure { Log.w(TAG, "dialler guard failed", it) }
            runCatching {
                claim(
                    Telephony.Sms.getDefaultSmsPackage(context),
                    "It is the app that receives your text messages, including one-time " +
                        "passcodes from your bank.",
                )
            }.onFailure { Log.w(TAG, "sms guard failed", it) }

            // The WebView provider. If it cannot be read, protect every candidate.
            runCatching {
                val provider = pm.getPackageInfo("com.google.android.webview", 0)
                claim(provider.packageName, "It draws web pages inside other apps.")
            }.onFailure { /* absent is fine -- Protection covers the family by pattern */ }

            // Anything running an accessibility service the user has switched on. For a
            // screen-reader user this is a total lock-out they could not undo unaided.
            runCatching {
                Settings.Secure.getString(
                    context.contentResolver,
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES,
                )?.split(':')?.forEach { component ->
                    claim(
                        component.substringBefore('/').takeIf { it.isNotBlank() },
                        "You have an accessibility service switched on in this app.",
                    )
                }
            }.onFailure { Log.w(TAG, "accessibility guard failed", it) }

            // The live wallpaper in use.
            runCatching {
                secureSetting(context, "wallpaper_component")?.let {
                    claim(it.substringBefore('/'), "It is drawing your wallpaper.")
                }
            }.onFailure { /* optional */ }

            return LiveGuards(holders)
        }

        /** Failure-tolerant read of a Settings.Secure key that has no public constant. */
        private fun secureSetting(context: Context, key: String): String? = runCatching {
            Settings.Secure.getString(context.contentResolver, key)
        }.getOrNull()
    }
}

/** True when the phone forbids debugging altogether, so the whole Shizuku route is closed. */
fun Context.debuggingIsBlockedByPolicy(): Boolean = runCatching {
    val um = getSystemService(android.os.UserManager::class.java)
    um?.hasUserRestriction(android.os.UserManager.DISALLOW_DEBUGGING_FEATURES) == true
}.getOrDefault(false)

/**
 * The phone's OS security patch level, as neutral information -- never as a verdict.
 *
 * It is tempting to compare this against the date a wireless-debugging flaw was fixed and
 * warn on it. That would be wrong: the ADB daemon ships as a Google Play system update, so
 * it can be patched while this string stays older, and no public API exposes that level.
 * The string also defaults to empty when unset, so blank means "unknown", not "old".
 */
fun securityPatchLevel(): String? =
    Build.VERSION.SECURITY_PATCH.takeIf { it.isNotBlank() }
