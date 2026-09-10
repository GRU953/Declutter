// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Aninda Sundar Howlader (GRU953)

package dev.gru953.declutter.data

import android.content.Context
import android.os.Build
import android.util.Log
import dev.gru953.declutter.domain.Action
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

@Serializable
data class HistoryItem(
    @SerialName("pkg") val pkg: String,
    @SerialName("name") val displayName: String,
    @SerialName("action") val action: String,
    @SerialName("ok") val succeeded: Boolean,
    @SerialName("detail") val detail: String? = null,
) {
    val parsedAction: Action?
        get() = Action.entries.firstOrNull { it.name == action }
}

@Serializable
data class HistoryBatch(
    @SerialName("id") val id: String,
    /** Unix milliseconds. Rendered in the phone's own time zone. */
    @SerialName("at") val at: Long,
    @SerialName("title") val title: String,
    @SerialName("items") val items: List<HistoryItem>,
) {
    val restorable: List<HistoryItem>
        get() = items.filter {
            it.succeeded && (it.parsedAction == Action.REMOVE || it.parsedAction == Action.DISABLE)
        }
}

@Serializable
data class HistoryFile(
    @SerialName("version") val version: Int = 1,
    /** Captured before the first change, so a later fault can be dated against the firmware. */
    @SerialName("device") val device: DeviceStamp? = null,
    @SerialName("batches") val batches: List<HistoryBatch> = emptyList(),
)

@Serializable
data class DeviceStamp(
    @SerialName("model") val model: String,
    @SerialName("build") val build: String,
    @SerialName("android") val android: String,
    @SerialName("patch") val patch: String,
    @SerialName("firstRun") val firstRun: Long,
)

/**
 * Every change the app makes, written to disk before it is attempted.
 *
 * Kept on disk rather than in memory for two reasons. It has to survive the app being
 * killed, because Motorola's own battery software kills background apps aggressively; and
 * it has to be exportable, because this handset has a public history of boot failures after
 * the Android 16 update, so a user needs to be able to show what they changed and when.
 */
class HistoryStore(context: Context) {

    private val file = File(context.filesDir, "history.json")
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }
    private val lock = Mutex()

    private val _history = MutableStateFlow(HistoryFile())
    val history: StateFlow<HistoryFile> = _history.asStateFlow()

    suspend fun load() = withContext(Dispatchers.IO) {
        lock.withLock {
            val loaded = runCatching {
                if (file.exists()) json.decodeFromString<HistoryFile>(file.readText())
                else HistoryFile()
            }.getOrElse {
                Log.w(TAG, "history unreadable, starting a fresh one", it)
                HistoryFile()
            }
            _history.value = loaded
        }
    }

    /** Records what the phone was running before anything was changed. */
    suspend fun stampDevice(now: Long) = withContext(Dispatchers.IO) {
        lock.withLock {
            if (_history.value.device != null) return@withLock
            val stamped = _history.value.copy(
                device = DeviceStamp(
                    model = "${Build.MANUFACTURER} ${Build.MODEL}",
                    build = Build.DISPLAY,
                    android = Build.VERSION.RELEASE,
                    patch = Build.VERSION.SECURITY_PATCH,
                    firstRun = now,
                ),
            )
            _history.value = stamped
            write(stamped)
        }
    }

    /** Opens a batch *before* the first change in it is attempted. */
    suspend fun openBatch(id: String, at: Long, title: String) = withContext(Dispatchers.IO) {
        lock.withLock {
            val updated = _history.value.copy(
                batches = listOf(HistoryBatch(id, at, title, emptyList())) + _history.value.batches,
            )
            _history.value = updated
            write(updated)
        }
    }

    suspend fun record(batchId: String, item: HistoryItem) = withContext(Dispatchers.IO) {
        lock.withLock {
            val updated = _history.value.copy(
                batches = _history.value.batches.map { batch ->
                    if (batch.id == batchId) batch.copy(items = batch.items + item) else batch
                },
            )
            _history.value = updated
            write(updated)
        }
    }

    /** Drops batches that recorded nothing, so the list stays honest. */
    suspend fun closeBatch(batchId: String) = withContext(Dispatchers.IO) {
        lock.withLock {
            val updated = _history.value.copy(
                batches = _history.value.batches.filterNot {
                    it.id == batchId && it.items.isEmpty()
                },
            )
            _history.value = updated
            write(updated)
        }
    }

    /** The whole log as text, for sharing with a repair shop or attaching to a bug report. */
    fun asPlainText(): String {
        val h = _history.value
        return buildString {
            appendLine("Declutter by GRU953 - change log")
            h.device?.let {
                appendLine("Phone: ${it.model}")
                appendLine("Build: ${it.build}")
                appendLine("Android ${it.android}, security patch ${it.patch}")
            }
            appendLine()
            if (h.batches.isEmpty()) appendLine("Nothing has been changed.")
            h.batches.forEach { batch ->
                appendLine("${formatStamp(batch.at)} - ${batch.title}")
                batch.items.forEach { item ->
                    val verdict = if (item.succeeded) item.parsedAction?.pastTense ?: "Changed"
                    else "FAILED"
                    appendLine("  $verdict: ${item.displayName} (${item.pkg})")
                    item.detail?.let { appendLine("    $it") }
                }
                appendLine()
            }
        }
    }

    private fun write(value: HistoryFile) {
        runCatching {
            val temp = File(file.parentFile, "${file.name}.tmp")
            temp.writeText(json.encodeToString(value))
            temp.renameTo(file)
        }.onFailure { Log.e(TAG, "could not save the change log", it) }
    }

    private companion object {
        const val TAG = "HistoryStore"
    }
}

/** `yyyy-MM-dd HH:mm` in the phone's own time zone, without pulling in a formatter library. */
fun formatStamp(millis: Long): String = runCatching {
    val instant = java.time.Instant.ofEpochMilli(millis)
    val local = java.time.LocalDateTime.ofInstant(instant, java.time.ZoneId.systemDefault())
    java.time.format.DateTimeFormatter.ofPattern("d MMM yyyy, HH:mm").format(local)
}.getOrDefault(millis.toString())
