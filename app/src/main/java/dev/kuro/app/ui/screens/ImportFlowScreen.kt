package dev.kuro.app.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import dev.kuro.app.logging.KuroLog
import dev.kuro.app.ui.components.ImportSteps
import dev.kuro.app.ui.components.KuroCard
import dev.kuro.app.ui.components.KuroFullWidthPrimary
import dev.kuro.app.ui.components.KuroGhostButton
import dev.kuro.app.ui.components.KuroScreenTitle
import dev.kuro.app.ui.components.KuroStepper
import dev.kuro.app.ui.theme.KuroMotion
import dev.kuro.app.ui.theme.KuroSpacing

/**
 * Import flow — 5 modern steps: Source / Analyze / Configure / Import / Ready.
 */
@Composable
fun ImportFlowScreen(
    initialSource: String,
    onDone: () -> Unit,
    onBack: () -> Unit
) {
    var step by remember { mutableIntStateOf(0) }
    var envName by remember { mutableStateOf("Default Workspace") }
    var isolate by remember { mutableStateOf(true) }
    var notifications by remember { mutableStateOf(true) }

    LaunchedEffect(initialSource) { KuroLog.importStart(initialSource) }
    LaunchedEffect(step) {
        KuroLog.importStep(step, ImportSteps.size, ImportSteps.getOrElse(step) { "Step" })
    }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .padding(top = KuroSpacing.s20, bottom = KuroSpacing.s32)
    ) {
        KuroScreenTitle(
            title = if (initialSource == "installed") "Import installed app" else "Import APK",
            subtitle = "Step ${step + 1} of 5"
        )
        Spacer(Modifier.height(KuroSpacing.s16))
        KuroStepper(current = step, modifier = Modifier.padding(horizontal = KuroSpacing.s20))
        Spacer(Modifier.height(KuroSpacing.s20))

        AnimatedContent(
            targetState = step,
            label = "importStep"
        ) { s ->
            Column(Modifier.padding(horizontal = KuroSpacing.s20)) {
                when (s) {
                    0 -> KuroCard(Modifier.fillMaxWidth()) {
                        Text("Select source", style = MaterialTheme.typography.titleLarge)
                        Text(
                            if (initialSource == "installed")
                                "Clone keeps the original untouched."
                            else "Any .apk file from storage.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    1 -> KuroCard(Modifier.fillMaxWidth()) {
                        Text("Analyze package", style = MaterialTheme.typography.titleLarge)
                        Text(
                            "Scanning manifest, SDK, ABIs…",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    2 -> KuroCard(Modifier.fillMaxWidth()) {
                        Text("Configure environment", style = MaterialTheme.typography.titleLarge)
                        Spacer(Modifier.height(KuroSpacing.s8))
                        Text("Environment: $envName")
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Isolate storage", Modifier.weight(1f))
                            Switch(
                                checked = isolate,
                                onCheckedChange = {
                                    isolate = it
                                    KuroLog.settingsChange("import.isolateStorage", it.toString())
                                }
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Allow notifications", Modifier.weight(1f))
                            Switch(
                                checked = notifications,
                                onCheckedChange = {
                                    notifications = it
                                    KuroLog.settingsChange("import.notifications", it.toString())
                                }
                            )
                        }
                    }
                    3 -> KuroCard(Modifier.fillMaxWidth()) {
                        Text("Importing…", style = MaterialTheme.typography.titleLarge)
                        Text(
                            "Copying into vault with shimmer — no spinners.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(KuroSpacing.s8))
                        dev.kuro.app.ui.components.SkeletonLine()
                    }
                    else -> KuroCard(Modifier.fillMaxWidth()) {
                        Text("Ready ✓", style = MaterialTheme.typography.titleLarge)
                        Text(
                            "Environment prepared. Launch when ready.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(KuroSpacing.s20))
        Column(
            Modifier.padding(horizontal = KuroSpacing.s20),
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(KuroSpacing.s12)
        ) {
            if (step < 4) {
                KuroFullWidthPrimary(
                    text = if (step == 3) "Finish import" else "Continue",
                    onClick = { step++ }
                )
                KuroGhostButton(
                    text = if (step == 0) "Cancel" else "Back",
                    onClick = { if (step == 0) onBack() else step-- },
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                KuroFullWidthPrimary(text = "Open in Vault", onClick = onDone)
            }
        }
    }
}
