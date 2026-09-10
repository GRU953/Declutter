// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Aninda Sundar Howlader (GRU953)

package dev.gru953.declutter

import dev.gru953.declutter.catalogue.Advice
import dev.gru953.declutter.catalogue.CatalogueFile
import dev.gru953.declutter.catalogue.Risk
import dev.gru953.declutter.domain.Protection
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * The catalogue and the hard blocklist have to agree, and this is the test that keeps them
 * agreeing. A package the catalogue calls "never remove" that the blocklist would let
 * through is the exact failure this app exists to prevent, so it is a build failure here.
 */
class CatalogueSafetyTest {

    private val json = Json { ignoreUnknownKeys = true }

    private val catalogue: CatalogueFile by lazy {
        val file = File("src/main/assets/catalogue.json")
        assertTrue("catalogue.json is missing from assets", file.exists())
        json.decodeFromString(file.readText())
    }

    @Test
    fun `every never-remove entry is also hard-blocked in code`() {
        val leaks = catalogue.entries
            .filter { it.advice == Advice.NEVER || it.risk == Risk.UNSAFE }
            .filter { Protection.refusalReason(it.pkg) == null }
            .map { it.pkg }

        assertEquals(
            "these are rated never-remove but the blocklist would allow them through",
            emptyList<String>(),
            leaks,
        )
    }

    @Test
    fun `the app cannot remove its own escape route`() {
        val mustBeBlocked = listOf(
            "dev.gru953.declutter",
            "moe.shizuku.privileged.api",
            "com.android.shell",
            "com.android.settings",
            "com.android.systemui",
            "com.android.vending",
            "com.google.android.packageinstaller",
        )
        mustBeBlocked.forEach { pkg ->
            assertTrue("$pkg must be refused", Protection.isProtected(pkg))
        }
    }

    @Test
    fun `resource overlays are refused by pattern, whatever their name`() {
        val overlays = listOf(
            "com.motorola.actions.overlay",
            "com.android.wifi.resources.overlay.target.sm8635",
            "com.motorola.android.overlay.common",
            "android.auto_generated_rro_product__",
            "com.example.res.overlay.thing",
            "com.android.internal.display.cutout.emulation.corner",
        )
        overlays.forEach { pkg ->
            assertTrue("$pkg must be refused by pattern", Protection.isProtected(pkg))
        }
    }

    @Test
    fun `every entry carries a plain-English description and consequence`() {
        val thin = catalogue.entries.filter {
            it.what.length < 20 || it.loses.length < 15 || it.label.isBlank()
        }.map { it.pkg }
        assertEquals("these entries are too thin to be useful", emptyList<String>(), thin)
    }

    @Test
    fun `ordinary bloatware is not swept up by the blocklist`() {
        // If the guards were too broad the app would be useless, so this is the other half
        // of the safety property: things that genuinely should be removable, are.
        val shouldBeAllowed = listOf(
            "com.facebook.appmanager",
            "com.facebook.services",
            "com.motorola.ccc.notification",
            "com.motorola.motocit",
            "com.google.android.apps.youtube.music",
            "com.netflix.mediaclient",
        )
        shouldBeAllowed.forEach { pkg ->
            assertEquals(
                "$pkg should be removable, but the blocklist refuses it",
                null,
                Protection.refusalReason(pkg),
            )
        }
    }

    @Test
    fun `the catalogue is dated and has no duplicate packages`() {
        assertTrue("the catalogue must record when it was reviewed",
            catalogue.reviewed.matches(Regex("""\d{4}-\d{2}-\d{2}""")))
        val duplicates = catalogue.entries.groupBy { it.pkg }
            .filterValues { it.size > 1 }
            .keys
        assertEquals(emptySet<String>(), duplicates)
    }
}
