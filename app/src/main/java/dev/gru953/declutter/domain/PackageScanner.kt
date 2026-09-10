// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Aninda Sundar Howlader (GRU953)

package dev.gru953.declutter.domain

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.util.Log
import dev.gru953.declutter.catalogue.CatalogueEntry
import java.io.File

/**
 * Reads what is on the phone.
 *
 * This whole path is ordinary public API plus `QUERY_ALL_PACKAGES`: no Shizuku is needed to
 * *look*. That matters, because it means the list, the risk ratings and the explanations all
 * work before the user has set anything up -- they can see what the app would do, and read
 * the consequences, before granting it any power at all.
 */
class PackageScanner(
    private val context: Context,
    private val catalogue: Map<String, CatalogueEntry>,
) {

    fun scan(): List<AppRow> {
        val pm = context.packageManager
        val guards = LiveGuards.resolve(context)
        val nonDisableable = readMotorolaNonDisableableList()

        val packages: List<PackageInfo> = runCatching {
            pm.getInstalledPackages(
                PackageManager.MATCH_UNINSTALLED_PACKAGES or
                    PackageManager.MATCH_DISABLED_COMPONENTS or
                    PackageManager.MATCH_DISABLED_UNTIL_USED_COMPONENTS,
            )
        }.getOrElse {
            Log.e(TAG, "could not list packages", it)
            emptyList()
        }

        return packages.mapNotNull { info ->
            val app = info.applicationInfo ?: return@mapNotNull null
            val pkg = info.packageName
            val entry = catalogue[pkg]

            val installed = app.flags and FLAG_INSTALLED != 0
            val enabledSetting = runCatching { pm.getApplicationEnabledSetting(pkg) }
                .getOrDefault(PackageManager.COMPONENT_ENABLED_STATE_DEFAULT)
            val state = when {
                !installed -> PackageState.REMOVED
                enabledSetting == PackageManager.COMPONENT_ENABLED_STATE_DISABLED ||
                    enabledSetting == PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER ->
                    PackageState.DISABLED
                !app.enabled -> PackageState.DISABLED
                else -> PackageState.ACTIVE
            }

            val isSystem = app.flags and ApplicationInfo.FLAG_SYSTEM != 0 ||
                app.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP != 0
            val isPersistent = app.flags and ApplicationInfo.FLAG_PERSISTENT != 0

            val (block, reason) = decideBlock(
                pkg = pkg,
                isSystem = isSystem,
                isPersistent = isPersistent,
                guards = guards,
            )

            AppRow(
                pkg = pkg,
                displayName = runCatching { pm.getApplicationLabel(app).toString() }
                    .getOrDefault(entry?.label ?: pkg)
                    .ifBlank { entry?.label ?: pkg },
                versionName = info.versionName,
                state = state,
                isSystem = isSystem,
                isPersistent = isPersistent,
                nonDisableable = pkg in nonDisableable,
                installedSizeBytes = apkSize(app),
                entry = entry,
                block = block,
                blockReason = reason,
            )
        }
    }

    private fun decideBlock(
        pkg: String,
        isSystem: Boolean,
        isPersistent: Boolean,
        guards: LiveGuards,
    ): Pair<Block, String?> {
        Protection.refusalReason(pkg)?.let { return Block.PROTECTED to it }
        guards.reasonFor(pkg)?.let { return Block.IN_USE to it }
        if (isPersistent) {
            return Block.PROTECTED to
                "Android starts this one at boot and keeps it running. Removing that " +
                "kind of app risks a phone that will not start."
        }
        if (!isSystem) {
            return Block.USER_INSTALLED to
                "You installed this one yourself, so you can remove it the normal way: " +
                "press and hold its icon, then Uninstall."
        }
        return Block.NONE to null
    }

    private fun apkSize(app: ApplicationInfo): Long = runCatching {
        val source = app.sourceDir ?: return@runCatching 0L
        File(source).length()
    }.getOrDefault(0L)

    /**
     * Motorola ships an explicit list of packages the system refuses to disable. A refusal
     * is itself strong evidence a package should be left alone, so it is worth surfacing --
     * and it explains, in advance, why a "turn off" attempt would be rejected.
     */
    private fun readMotorolaNonDisableableList(): Set<String> = runCatching {
        NON_DISABLEABLE_DIRS
            .map(::File)
            .filter { it.isDirectory }
            .flatMap { dir -> dir.listFiles()?.toList().orEmpty() }
            .mapNotNull { file ->
                file.name.removeSuffix(".xml").takeIf { it.contains('.') }
            }
            .toSet()
    }.getOrDefault(emptySet())

    private companion object {
        const val TAG = "PackageScanner"

        /** ApplicationInfo.FLAG_INSTALLED -- hidden, but a fixed value since API 24. */
        const val FLAG_INSTALLED = 1 shl 23

        val NON_DISABLEABLE_DIRS = listOf(
            "/system_ext/etc/nondisable",
            "/product/etc/nondisable",
            "/system/etc/nondisable",
        )
    }
}
