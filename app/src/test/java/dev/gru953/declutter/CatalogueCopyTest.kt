// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Aninda Sundar Howlader (GRU953)

package dev.gru953.declutter

import dev.gru953.declutter.catalogue.CatalogueEntry
import dev.gru953.declutter.catalogue.CatalogueFile
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * The catalogue's prose is the app, as far as the reader is concerned: it is what they
 * decide on. This holds it to the rules that matter.
 *
 * Two of these are more than tidiness.
 *
 * The licensing one: the ratings were researched with the help of community debloating
 * lists that carry copyleft licences, and this app is Apache-2.0. Naming those projects in
 * the interface is merely unhelpful; reproducing their wording would make the NOTICE file's
 * claim untrue.
 *
 * The false-refusal one: Motorola's "non-disableable" list is read by Motorola's own
 * Settings code to grey out a button. It is not an Android-level guard, so a change made
 * through Shizuku goes straight through it. Copy that promises the phone will refuse
 * something hands the reader a safety net that does not exist, which is worse than saying
 * nothing.
 *
 * Written deliberately as a rule set rather than a list of known faults: a check that only
 * looks for what was already fixed cannot find what was missed.
 */
class CatalogueCopyTest {

    private val json = Json { ignoreUnknownKeys = true }

    /** A double quote, as a string, so no pattern below has to embed one. */
    private val DOUBLE = '"'.toString()

    private val catalogue: CatalogueFile by lazy {
        val file = File("src/main/assets/catalogue.json")
        assertTrue("catalogue.json is missing from assets", file.exists())
        json.decodeFromString(file.readText())
    }

    private fun prose(entry: CatalogueEntry) = "${entry.label} ${entry.what} ${entry.loses}"

    private fun offenders(pattern: Regex) =
        catalogue.entries.filter { pattern.containsMatchIn(prose(it)) }.map { it.pkg }

    private fun check(name: String, pattern: Regex, why: String) =
        assertEquals("$name -- $why", emptyList<String>(), offenders(pattern))

    @Test
    fun `no entry names or quotes the projects the research drew on`() {
        check(
            "names a source project",
            Regex("""UAD-?NG|Universal Android Debloater|Muntashir|android-debloat-list|XDA""", RegexOption.IGNORE_CASE),
            "attribution belongs in NOTICE, not in the interface",
        )
        // Built by concatenation: a quote character inside a Kotlin string literal that
        // also has to contain quote characters is more trouble than it is worth.
        val quoteClass = "[" + DOUBLE + "'\u2018\u2019]"
        val notQuote = "[^" + DOUBLE + "'\u2018\u2019]"
        check(
            "quotes another source",
            Regex(quoteClass + "[A-Z]" + notQuote + "{25,}" + quoteClass),
            "a quotation from a copyleft source cannot ship in an Apache-2.0 app",
        )
    }

    @Test
    fun `no entry explains how the research was done`() {
        check(
            "sourcing process chatter",
            Regex(
                """other (debloating|removal) tools|was checked against|could not be verified""" +
                    """|circulates in some|debloat guides|privacy lists flag|community reports""" +
                    """|reviews name it|according to \w+ users|the one list""",
                RegexOption.IGNORE_CASE,
            ),
            "the reader wants to know about the package, not about the research",
        )
    }

    @Test
    fun `no entry is addressed to the programmer instead of the reader`() {
        check(
            "instructs the app or the author",
            Regex(
                """\b(never offer|do not offer|do not ship|before shipping""" +
                    """|must never be offered|never remove this|do not assume""" +
                    """|needs re-check|this app must|our app|we rate|our catalogue""" +
                    """|do not present it|do not confuse)\b""",
                RegexOption.IGNORE_CASE,
            ),
            "the app decides what it offers; the reader cannot act on any of this",
        )
        check(
            "developer-only detail",
            Regex("""state_glance_lockscreen|state_space_lockscreen|AndroidManifest|\bAIDL\b"""),
            "the reader cannot reach an internal settings key",
        )
    }

    @Test
    fun `no entry promises the phone will refuse a change`() {
        check(
            "false refusal promise",
            Regex(
                """\b(will be refused|will refuse|is refused|are refused|gets refused""" +
                    """|rejected by|blocked by android|refuses the change|android refuses)\b""",
                RegexOption.IGNORE_CASE,
            ),
            "Motorola's non-disableable list greys out a Settings button and is not an " +
                "Android guard, so nothing may promise a change would be blocked",
        )
    }

    @Test
    fun `no entry speaks as we, shouts, hypes, or sneers`() {
        check(
            "says we or our",
            Regex("""\b(we|our)\b|(?<![A-Z])\bus\b"""),
            "GRU953 is one person, and the app speaks as neither we nor I",
        )
        check(
            "shouts",
            Regex("""\b(WARNING|CAUTION|IMPORTANT|MUST KEEP|DO NOT|UNVERIFIED|HIGH-VALUE|NOTE|THIS|DANGEROUS)\b"""),
            "the app colours and labels severity itself",
        )
        check(
            "label-style prefix",
            Regex("""(^|[.!?]\s+)(Note|Important|Warning|Caution)\b\s*[:,]""", RegexOption.IGNORE_CASE),
            "a sentence does not need a heading",
        )
        check(
            "forbidden word",
            Regex("""\b(simply|obviously|notorious|nags)\b""", RegexOption.IGNORE_CASE),
            "these make a stuck reader feel stupid, or sneer on their behalf",
        )
        check(
            "hype",
            Regex("""\b(seamless|robust|blazing|revolutionary|game-chang\w+|best-in-class)\b""", RegexOption.IGNORE_CASE),
            "say what it does and let it stand",
        )
    }

    @Test
    fun `no entry capitalises a word for emphasis`() {
        // Real acronyms are the actual names of things and stay. Anything else in capitals
        // is emphasis, which this app does with colour, a shape and a word instead.
        val acronyms = setOf(
            "GPS", "NFC", "SIM", "SMS", "MMS", "APN", "APNS", "USB", "OTA", "RCS", "UPI",
            "GNSS", "CPU", "GPU", "PDF", "US", "UK", "SOS", "IT", "ID", "OS", "PC", "TV",
            "AI", "ML", "FM", "HD", "LTE", "IMS", "VPN", "QDMA", "AT", "T", "A", "I", "OK",
            "AOSP", "ADB", "API", "UI", "RAM", "DRM", "TTS", "SAF", "QPR", "MCC", "PLMN",
            "AR", "VR", "PIN", "QMS", "QCC", "UIM", "XT", "OEM", "HTTPS", "HTTP", "URL",
            "NR", "RF", "SAR", "IR", "LED", "DNS", "IP", "WLAN", "WPS", "BT", "LE", "GSM",
            "CDMA", "WCDMA", "EDGE", "HSPA", "SUPL", "ANT", "FOTA", "MDM", "MMI", "SAP",
            "HAL", "JVT", "SLPC", "PLM", "CCC", "PAKS", "DPM", "EMBMS", "QDCM", "APK",
            "SDK", "IMAP", "GBA", "UICC", "OMA", "CCE", "IKEA", "YT", "UC", "GO", "POP",
            "SMTP", "NTP", "MTP", "DCF", "CABL", "XTRA", "IMEI", "CVE", "UCE", "ICE", "TZ",
            "NV", "QTI", "DUN", "SUW", "OOB",
        )
        val shouted = Regex("""\b[A-Z]{2,}\b""")
        val offending = catalogue.entries.mapNotNull { entry ->
            val words = shouted.findAll(prose(entry)).map { it.value }.filterNot { it in acronyms }.toList()
            if (words.isEmpty()) null else "${entry.pkg} ${words.distinct()}"
        }
        assertEquals(
            "these words are capitalised for emphasis, not because they are acronyms",
            emptyList<String>(),
            offending,
        )
    }

    @Test
    fun `every label fits a list row, every sentence is finished, nothing is unrendered`() {
        assertEquals(
            "a label longer than 46 characters is truncated in a list row",
            emptyList<String>(),
            catalogue.entries.filter { it.label.length > 46 }.map { it.pkg },
        )
        assertEquals(
            emptyList<String>(),
            catalogue.entries
                .filter { entry ->
                    listOf(entry.what, entry.loses).any { field ->
                        val last = field.trimEnd().lastOrNull()
                        last != '.' && last != '?' && last != '!'
                    }
                }
                .map { it.pkg },
        )
        check(
            "escape artefacts",
            Regex("\\\\[nut]"),
            "a backslash escape would render literally",
        )
    }
}
