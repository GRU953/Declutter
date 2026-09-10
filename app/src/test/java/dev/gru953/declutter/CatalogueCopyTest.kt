// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Aninda Sundar Howlader (GRU953)

package dev.gru953.declutter

import dev.gru953.declutter.catalogue.CatalogueFile
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * The catalogue's prose is the app, as far as the reader is concerned: it is what they
 * decide on. This test holds it to the rules that matter.
 *
 * The licensing one is not cosmetic. The ratings were researched with the help of community
 * debloating lists that carry copyleft licences, and this app is Apache-2.0. Naming those
 * projects in the interface is merely unhelpful; reproducing their wording would make the
 * NOTICE file's claim untrue. So neither is allowed to reappear.
 */
class CatalogueCopyTest {

    private val json = Json { ignoreUnknownKeys = true }

    private val catalogue: CatalogueFile by lazy {
        val file = File("src/main/assets/catalogue.json")
        assertTrue("catalogue.json is missing from assets", file.exists())
        json.decodeFromString(file.readText())
    }

    private fun prose() = catalogue.entries.map { it to "${it.label} ${it.what} ${it.loses}" }

    private fun offenders(pattern: Regex) =
        prose().filter { (_, text) -> pattern.containsMatchIn(text) }.map { it.first.pkg }

    @Test
    fun `no entry names the projects the research drew on`() {
        val named = Regex(
            """UAD-?NG|Universal Android Debloater|Muntashir|android-debloat-list""",
            RegexOption.IGNORE_CASE,
        )
        assertEquals(
            "attribution belongs in NOTICE, not in the interface",
            emptyList<String>(),
            offenders(named),
        )
    }

    @Test
    fun `no entry reproduces another project's wording`() {
        // A capitalised run of 25-plus characters inside quotation marks is a quotation,
        // and a quotation from a copyleft source cannot ship in an Apache-2.0 app.
        assertEquals(
            "these entries appear to quote another source verbatim",
            emptyList<String>(),
            offenders(Regex("""["'][A-Z][^"']{25,}["']""")),
        )
    }

    @Test
    fun `no entry speaks as we, or talks to the programmer instead of the reader`() {
        // "US" the country is fine; "us" the pronoun is not, because GRU953 is one person.
        val pronouns = Regex("""\b(we|our)\b|(?<![A-Z])\bus\b""")
        assertEquals(emptyList<String>(), offenders(pronouns))

        val toTheProgrammer = Regex(
            """\b(this app must|our app|we rate|our catalogue|never offer any package|do not present it)\b""",
            RegexOption.IGNORE_CASE,
        )
        assertEquals(emptyList<String>(), offenders(toTheProgrammer))
    }

    @Test
    fun `no entry shouts, hypes, or belittles the reader`() {
        assertEquals(
            "the app colours and labels severity itself; copy does not need to shout",
            emptyList<String>(),
            offenders(Regex("""\b(WARNING|CAUTION|NOTE)\b\s*[-—:]""")),
        )
        assertEquals(
            "‘simply’ and ‘obviously’ make a stuck reader feel stupid",
            emptyList<String>(),
            offenders(Regex("""\b(simply|obviously)\b""", RegexOption.IGNORE_CASE)),
        )
        assertEquals(
            emptyList<String>(),
            offenders(Regex("""\b(seamless|robust|blazing|revolutionary|game-chang\w+)\b""", RegexOption.IGNORE_CASE)),
        )
    }

    @Test
    fun `every label fits a list row and every sentence is finished`() {
        val tooLong = catalogue.entries.filter { it.label.length > 46 }.map { it.pkg }
        assertEquals("a label longer than 46 characters is truncated", emptyList<String>(), tooLong)

        val unfinished = catalogue.entries
            .filter { entry ->
                !entry.what.trimEnd().endsWith('.') &&
                    !entry.what.trimEnd().endsWith('?') ||
                    !entry.loses.trimEnd().endsWith('.') &&
                    !entry.loses.trimEnd().endsWith('?')
            }
            .map { it.pkg }
        assertEquals(emptyList<String>(), unfinished)
    }

    @Test
    fun `no entry carries an unrendered escape sequence`() {
        assertEquals(
            emptyList<String>(),
            offenders(Regex("""\\n|\\u|\\t""")),
        )
    }
}
