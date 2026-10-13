package dev.kuro.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import dev.kuro.app.data.AppStatus
import dev.kuro.app.ui.theme.KuroAccent
import dev.kuro.app.ui.theme.KuroSuccess
import dev.kuro.app.ui.theme.KuroWarning

@Composable
fun KuroFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        shape = RoundedCornerShape(12.dp),
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primary,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
        )
    )
}

@Composable
fun EnvironmentBadge(text: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun VersionBadge(text: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHighest
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/** Live status dot + label. Running pulses (caller animates alpha). */
@Composable
fun StatusIndicator(status: AppStatus, pulsing: Boolean = false, modifier: Modifier = Modifier) {
    val dot = when (status) {
        AppStatus.RUNNING -> KuroSuccess
        AppStatus.PAUSED -> KuroWarning
        AppStatus.UPDATING -> MaterialTheme.colorScheme.secondary
        AppStatus.STOPPED -> MaterialTheme.colorScheme.outline
    }
    val label = when (status) {
        AppStatus.RUNNING -> "Running"
        AppStatus.PAUSED -> "Paused"
        AppStatus.UPDATING -> "Updating"
        AppStatus.STOPPED -> "Stopped"
    }
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(dot)
        )
        Text(
            text = "  $label",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (pulsing && status == AppStatus.RUNNING) {
            Box(
                Modifier
                    .padding(start = 6.dp)
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(KuroAccent.copy(alpha = 0.6f))
            )
        }
    }
}
