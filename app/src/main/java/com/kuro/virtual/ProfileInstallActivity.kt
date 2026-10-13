package com.kuro.virtual

import android.content.pm.PackageInstaller
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.kuro.core.VaultInstaller
import com.kuro.virtual.ui.theme.KuroTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Runs in the work-profile user: receives staged APK content URIs from the
 * personal side and installs them into this profile. Each install still asks
 * the user once through the system prompt.
 */
class ProfileInstallActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val base: Uri? = intent.data
        val splits: List<Uri> = intent
            .getStringArrayExtra(VaultInstaller.EXTRA_SPLITS)
            ?.mapNotNull { runCatching { Uri.parse(it) }.getOrNull() }
            .orEmpty()
        val pkg = intent.getStringExtra(VaultInstaller.EXTRA_PACKAGE).orEmpty()
        val label = intent.getStringExtra(VaultInstaller.EXTRA_LABEL).ifNullOrEmpty(pkg)
        val mode = intent.getStringExtra(VaultInstaller.EXTRA_MODE) ?: VaultInstaller.MODE_INSTALL
        setContent {
            KuroTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    if (mode == VaultInstaller.MODE_UNINSTALL) {
                        UninstallPanel(pkg, label, onClose = { finish() })
                    } else {
                        InstallPanel(base, splits, pkg, label, onClose = { finish() })
                    }
                }
            }
        }
    }

    private fun String?.ifNullOrEmpty(fallback: String): String =
        if (this.isNullOrEmpty()) fallback else this
}

@Composable
private fun UninstallPanel(pkg: String, label: String, onClose: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var message by remember { mutableStateOf("Remove the work-profile clone of $label? The device copy stays.") }
    var busy by remember { mutableStateOf(false) }
    var finished by remember { mutableStateOf(false) }

    fun uninstall() {
        if (pkg.isEmpty()) {
            message = "Package missing."
            return
        }
        busy = true
        message = "Removing $label…"
        scope.launch(Dispatchers.IO) {
            val future = com.kuro.core.InstallEvents.expect(pkg)
            try {
                val installer = context.packageManager.packageInstaller
                val result = android.content.Intent(VaultInstaller.ACTION_INSTALL_RESULT).apply {
                    setClassName(context.packageName, "com.kuro.virtual.InstallResultReceiver")
                    putExtra(VaultInstaller.EXTRA_PACKAGE, pkg)
                }
                var flags = android.app.PendingIntent.FLAG_UPDATE_CURRENT
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                    flags = flags or android.app.PendingIntent.FLAG_MUTABLE
                }
                val sender = android.app.PendingIntent.getBroadcast(
                    context, pkg.hashCode(), result, flags,
                )
                installer.uninstall(pkg, sender.intentSender)
                val status = future.get()
                withContext(Dispatchers.Main) {
                    busy = false
                    finished = status == android.content.pm.PackageInstaller.STATUS_SUCCESS
                    message = if (finished) {
                        "Clone removed. Its work data is gone; the device copy stays."
                    } else {
                        "Uninstall failed (status $status)."
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    busy = false
                    message = "Uninstall failed: ${e.message}"
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("Kuro work uninstall", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(12.dp))
        Text(message, style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(16.dp))
        if (busy) {
            LinearProgressIndicator()
        } else if (!finished) {
            Button(onClick = { uninstall() }) { Text("Uninstall clone") }
        }
        Spacer(Modifier.height(8.dp))
        Button(onClick = onClose) { Text("Close") }
    }
}

@Composable
private fun InstallPanel(
    base: Uri?,
    splits: List<Uri>,
    pkg: String,
    label: String,
    onClose: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var message by remember { mutableStateOf("Install $label as a work-profile clone?") }
    var busy by remember { mutableStateOf(false) }
    var finished by remember { mutableStateOf(false) }

    fun part(name: String, uri: Uri): VaultInstaller.ApkPart {
        var length = -1L
        try {
            context.contentResolver.query(uri, arrayOf(OpenableColumns.SIZE), null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) length = cursor.getLong(0)
            }
        } catch (_: Exception) {
        }
        return VaultInstaller.ApkPart(name, length) {
            try {
                context.contentResolver.openInputStream(uri)
            } catch (_: Exception) {
                null
            }
        }
    }

    fun install() {
        if (base == null || pkg.isEmpty()) {
            message = "Install data missing."
            return
        }
        busy = true
        message = "Installing $label…"
        scope.launch(Dispatchers.IO) {
            try {
                val parts = mutableListOf(part("base.apk", base))
                splits.forEachIndexed { index, uri -> parts += part("split_$index.apk", uri) }
                val status = VaultInstaller.startInstall(context, pkg, parts).get()
                withContext(Dispatchers.Main) {
                    busy = false
                    finished = status == PackageInstaller.STATUS_SUCCESS
                    message = if (finished) {
                        "$label installed. Open the clone from Kuro."
                    } else {
                        "Install failed (status $status)."
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    busy = false
                    message = "Install failed: ${e.message}"
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("Kuro work install", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(12.dp))
        Text(message, style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(16.dp))
        if (busy) {
            LinearProgressIndicator()
        } else if (!finished) {
            Button(onClick = { install() }) { Text("Install clone") }
        }
        Spacer(Modifier.height(8.dp))
        Button(onClick = onClose) { Text("Close") }
    }
}
