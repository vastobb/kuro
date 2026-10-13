package dev.kuro.app.logging

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import java.io.File

/**
 * Log file access: list / read / copy / share / export / delete.
 * All callers must run these off the UI thread except [shareFile],
 * which only fires an intent.
 */
object LogFiles {
    const val MAX_VIEW_CHARS = 262_144

    fun isLogFile(f: File): Boolean =
        f.isFile && (f.name.startsWith("kuro_") || f.name.startsWith("crash_")) && f.name.endsWith(".log")

    fun listAll(context: Context): List<File> {
        val dirs = linkedSetOf<File>()
        runCatching { context.getExternalFilesDir(null)?.let { dirs += File(it, "logs") } }
        dirs += File(context.filesDir, "logs")
        return dirs.flatMap { d -> d.listFiles()?.toList().orEmpty() }
            .filter(::isLogFile)
            .sortedByDescending { it.lastModified() }
    }

    fun readCapped(file: File, maxChars: Int = MAX_VIEW_CHARS): String = runCatching {
        file.inputStream().bufferedReader().use { r ->
            val buf = CharArray(minOf(maxChars, 1_048_576))
            val n = r.read(buf)
            if (n <= 0) "" else String(buf, 0, n)
        }
    }.getOrDefault("<unreadable>")

    fun copyToClipboard(context: Context, file: File): Int {
        val text = readCapped(file)
        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        cm.setPrimaryClip(ClipData.newPlainText(file.name, text))
        return text.length
    }

    fun contentUri(context: Context, file: File): Uri =
        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)

    fun shareFile(context: Context, file: File) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Kuro log ${file.name}")
            putExtra(Intent.EXTRA_STREAM, contentUri(context, file))
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(Intent.createChooser(intent, "Share ${file.name}").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
    }

    /** Copy into Documents/Kuro (external) or files/exports fallback. Returns destination. */
    fun exportToDocuments(context: Context, file: File): File {
        val docs = runCatching { context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS) }.getOrNull()
        val destDir = if (docs != null) File(docs, "Kuro").apply { mkdirs() }
        else File(context.filesDir, "exports").apply { mkdirs() }
        return file.copyTo(File(destDir, file.name), overwrite = true)
    }

    fun delete(file: File): Boolean = runCatching { file.delete() }.getOrDefault(false)
}
