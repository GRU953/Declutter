// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Aninda Sundar Howlader (GRU953)

package dev.gru953.declutter.domain

import dev.gru953.declutter.catalogue.CatalogueEntry
import dev.gru953.declutter.catalogue.Confidence
import dev.gru953.declutter.catalogue.Risk
import dev.gru953.declutter.catalogue.Vendor

/** What the phone currently thinks of a package. */
enum class PackageState {
    /** Installed and usable. */
    ACTIVE,

    /** Turned off for this user. Still on the phone; re-enabling is instant. */
    DISABLED,

    /** Removed for this user. The factory copy is still on the phone, so it can come back. */
    REMOVED,
}

/** Why the app will not act on a package. */
enum class Block {
    NONE,

    /** On the hard blocklist. No override exists. */
    PROTECTED,

    /** It is the launcher, keyboard, dialler, SMS app or WebView provider in use right now. */
    IN_USE,

    /** Not a system app the phone came with -- remove it the ordinary way, in Settings. */
    USER_INSTALLED,
}

/**
 * One row in the list: what the phone reports, joined to what the catalogue knows.
 * [entry] is null for a package the catalogue has never seen -- which is [Risk.UNKNOWN],
 * never "probably fine".
 */
data class AppRow(
    val pkg: String,
    /** The name in the app drawer, or the package name if it has no label. */
    val displayName: String,
    val versionName: String?,
    val state: PackageState,
    val isSystem: Boolean,
    /** Started at boot and kept running by the system. A boot-integrity risk. */
    val isPersistent: Boolean,
    /** The system refuses to disable it. Strong evidence it should be left alone. */
    val nonDisableable: Boolean,
    val installedSizeBytes: Long,
    val entry: CatalogueEntry?,
    val block: Block,
    val blockReason: String?,
) {
    val risk: Risk get() = entry?.risk ?: Risk.UNKNOWN
    val vendor: Vendor get() = entry?.vendor ?: guessVendor(pkg)
    val confidence: Confidence? get() = entry?.confidence

    /** True only for a package the app is willing to act on. */
    val actionable: Boolean get() = block == Block.NONE && risk != Risk.UNSAFE

    /** The tier the one-tap sweep is allowed to touch, and nothing else. */
    val sweepable: Boolean
        get() = actionable && risk == Risk.SAFE && state == PackageState.ACTIVE

    companion object {
        fun guessVendor(pkg: String): Vendor = when {
            pkg.startsWith("com.motorola") || pkg.startsWith("com.moto") -> Vendor.MOTOROLA
            pkg.startsWith("com.lenovo") || pkg.startsWith("com.tblenovo") -> Vendor.LENOVO
            pkg.startsWith("com.google.android") -> Vendor.GOOGLE
            pkg.startsWith("com.android") || pkg == "android" -> Vendor.ANDROID
            pkg.startsWith("com.qualcomm") || pkg.startsWith("com.qti") ||
                pkg.startsWith("vendor.qti") -> Vendor.QUALCOMM
            else -> Vendor.UNKNOWN
        }
    }
}

/** One thing the app did, kept so it can be undone. */
data class Operation(
    val pkg: String,
    val displayName: String,
    val action: Action,
    val succeeded: Boolean,
    val detail: String?,
)

enum class Action {
    /** Removed for this user. Reversible while the phone still works. */
    REMOVE,

    /** Turned off for this user. */
    DISABLE,

    /** Put back. */
    RESTORE,

    /** Turned back on. */
    ENABLE,
    ;

    val pastTense: String
        get() = when (this) {
            REMOVE -> "Removed"
            DISABLE -> "Turned off"
            RESTORE -> "Put back"
            ENABLE -> "Turned back on"
        }
}
