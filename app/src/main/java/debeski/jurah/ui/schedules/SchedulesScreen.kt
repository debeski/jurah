package debeski.jurah.ui.schedules

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import debeski.jurah.R
import debeski.jurah.domain.model.ScheduleStatus
import debeski.jurah.ui.components.GlassCard
import debeski.jurah.ui.components.JurahBackground
import debeski.jurah.ui.components.JurahIcons
import debeski.jurah.ui.components.SectionHeader
import debeski.jurah.ui.format.DoseFormat
import debeski.jurah.ui.rememberContainer
import debeski.jurah.ui.theme.GlassShapeSmall
import debeski.jurah.ui.theme.Missed
import debeski.jurah.ui.theme.Taken
import debeski.jurah.ui.theme.glass

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SchedulesScreen(
    onAddSchedule: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenAbout: () -> Unit,
) {
    val container = rememberContainer()
    val viewModel: SchedulesViewModel = viewModel(factory = SchedulesViewModel.factory(container))
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var notificationsAllowed by remember {
        mutableStateOf(NotificationManagerCompat.from(context).areNotificationsEnabled())
    }
    val requestNotifications = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> notificationsAllowed = granted }

    // Both grants can change outside the app, and the exact-alarm one silently
    // degrades reminder accuracy, so re-check on every resume and re-arm.
    LifecycleResumeEffect(Unit) {
        notificationsAllowed = NotificationManagerCompat.from(context).areNotificationsEnabled()
        viewModel.reconcileAlarms()
        onPauseOrDispose { }
    }

    var pendingDelete by remember { mutableStateOf<ScheduleCard?>(null) }

    JurahBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(R.string.app_name)) },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = MaterialTheme.colorScheme.onBackground,
                        actionIconContentColor = MaterialTheme.colorScheme.onBackground,
                    ),
                    actions = {
                        IconButton(onClick = onOpenHistory) {
                            Icon(JurahIcons.History, stringResource(R.string.nav_history))
                        }
                        IconButton(onClick = onOpenAbout) {
                            Icon(JurahIcons.Info, stringResource(R.string.nav_about))
                        }
                    },
                )
            },
            floatingActionButton = {
                // Scaffold anchors this to the end edge, so it sits bottom-right in
                // English and mirrors to bottom-left under Arabic without extra code.
                FloatingActionButton(
                    onClick = onAddSchedule,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ) {
                    Icon(JurahIcons.Add, stringResource(R.string.action_add_schedule))
                }
            },
        ) { insets ->
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = insets.calculateTopPadding() + 8.dp,
                    bottom = insets.calculateBottomPadding() + 96.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (!notificationsAllowed) {
                    item {
                        PermissionNotice(
                            icon = JurahIcons.Bell,
                            title = stringResource(R.string.permission_notifications_title),
                            body = stringResource(R.string.permission_notifications_body),
                            action = stringResource(R.string.permission_notifications_action),
                            onClick = {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    requestNotifications.launch(Manifest.permission.POST_NOTIFICATIONS)
                                } else {
                                    context.startActivity(
                                        Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                                            .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                                    )
                                }
                            },
                        )
                    }
                }

                if (!state.exactAlarmsAllowed) {
                    item {
                        PermissionNotice(
                            icon = JurahIcons.Clock,
                            title = stringResource(R.string.permission_exact_title),
                            body = stringResource(R.string.permission_exact_body),
                            action = stringResource(R.string.permission_exact_action),
                            onClick = {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                    context.startActivity(
                                        Intent(
                                            Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                                            Uri.parse("package:${context.packageName}"),
                                        )
                                    )
                                }
                            },
                        )
                    }
                }

                if (state.loaded && state.active.isEmpty() && state.inactive.isEmpty()) {
                    item { EmptyState() }
                }

                if (state.active.isNotEmpty()) {
                    item { SectionHeader(stringResource(R.string.section_active)) }
                    items(state.active, key = { it.schedule.id }) { card ->
                        ScheduleCardItem(
                            card = card,
                            now = state.now,
                            onPause = { viewModel.pause(card) },
                            onResume = { viewModel.resume(card) },
                            onDelete = { pendingDelete = card },
                        )
                    }
                }

                if (state.inactive.isNotEmpty()) {
                    item { SectionHeader(stringResource(R.string.section_inactive)) }
                    items(state.inactive, key = { it.schedule.id }) { card ->
                        ScheduleCardItem(
                            card = card,
                            now = state.now,
                            onPause = { viewModel.pause(card) },
                            onResume = { viewModel.resume(card) },
                            onDelete = { pendingDelete = card },
                        )
                    }
                }
            }
        }
    }

    pendingDelete?.let { card ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text(stringResource(R.string.delete_confirm_title)) },
            text = { Text(stringResource(R.string.delete_confirm_body)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.delete(card)
                    pendingDelete = null
                }) { Text(stringResource(R.string.action_delete)) }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) {
                    Text(stringResource(R.string.action_cancel))
                }
            },
        )
    }
}

@Composable
private fun EmptyState() {
    GlassCard(modifier = Modifier.padding(top = 48.dp)) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Icon(
                JurahIcons.Pill,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = stringResource(R.string.empty_title),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
            )
            Text(
                text = stringResource(R.string.empty_body),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun PermissionNotice(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    body: String,
    action: String,
    onClick: () -> Unit,
) {
    GlassCard(shape = GlassShapeSmall) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
            Column(modifier = Modifier.padding(start = 12.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Text(
                    text = body,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        TextButton(onClick = onClick, modifier = Modifier.padding(top = 4.dp)) { Text(action) }
    }
}

@Composable
private fun ScheduleCardItem(
    card: ScheduleCard,
    now: Long,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onDelete: () -> Unit,
) {
    val context = LocalContext.current
    val schedule = card.schedule
    val scheme = MaterialTheme.colorScheme

    GlassCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .glass(CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    JurahIcons.Pill,
                    contentDescription = null,
                    tint = scheme.primary,
                    modifier = Modifier.size(24.dp),
                )
            }
            Column(modifier = Modifier.padding(start = 12.dp)) {
                Text(
                    text = schedule.name?.takeIf { it.isNotBlank() }
                        ?: stringResource(R.string.unnamed_schedule),
                    style = MaterialTheme.typography.titleMedium,
                    color = scheme.onBackground,
                )
                Text(
                    text = buildString {
                        append(DoseFormat.interval(context, schedule.intervalMinutes))
                        append(" · ")
                        append(
                            schedule.durationDays?.let { DoseFormat.days(context, it) }
                                ?: stringResource(R.string.course_ongoing)
                        )
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = scheme.onSurfaceVariant,
                )
            }
        }

        Text(
            text = when {
                schedule.status == ScheduleStatus.PAUSED -> stringResource(R.string.course_paused)
                card.nextDoseAt == null -> stringResource(R.string.course_finished)
                card.nextDoseAt > now && schedule.startAtMillis > now ->
                    stringResource(R.string.course_starts, DoseFormat.dateTime(context, schedule.startAtMillis))
                else -> DoseFormat.countdown(context, now, card.nextDoseAt)
            },
            style = MaterialTheme.typography.headlineSmall,
            color = if (card.isActive) scheme.primary else scheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 14.dp),
        )

        card.nextDoseAt?.takeIf { card.isActive }?.let { at ->
            Text(
                text = DoseFormat.dateTime(context, at),
                style = MaterialTheme.typography.bodySmall,
                color = scheme.onSurfaceVariant,
            )
        }

        card.totalDoses?.let { total ->
            LinearProgressIndicator(
                progress = { (card.takenCount.toFloat() / total).coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth().padding(top = 14.dp),
                color = scheme.primary,
                trackColor = scheme.surfaceVariant,
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = card.totalDoses?.let {
                    stringResource(R.string.progress_taken, card.takenCount, it)
                } ?: stringResource(R.string.progress_taken_ongoing, card.takenCount),
                style = MaterialTheme.typography.bodySmall,
                color = Taken,
            )
            if (card.missedCount > 0) {
                Text(
                    text = " · " + stringResource(R.string.missed_count, card.missedCount),
                    style = MaterialTheme.typography.bodySmall,
                    color = Missed,
                )
            }
            Box(modifier = Modifier.weight(1f))
            if (schedule.status == ScheduleStatus.ACTIVE) {
                IconButton(onClick = onPause) {
                    Icon(JurahIcons.Pause, stringResource(R.string.action_pause), tint = scheme.onSurfaceVariant)
                }
            } else if (schedule.status == ScheduleStatus.PAUSED) {
                IconButton(onClick = onResume) {
                    Icon(JurahIcons.Play, stringResource(R.string.action_resume), tint = scheme.onSurfaceVariant)
                }
            }
            IconButton(onClick = onDelete) {
                Icon(JurahIcons.Delete, stringResource(R.string.action_delete), tint = scheme.onSurfaceVariant)
            }
        }
    }
}
