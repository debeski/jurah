package debeski.jurah.ui.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import debeski.jurah.R
import debeski.jurah.ui.components.GlassCard
import debeski.jurah.ui.components.JurahBackground
import debeski.jurah.ui.components.JurahIcons
import debeski.jurah.ui.components.SectionHeader
import debeski.jurah.ui.format.DoseFormat
import debeski.jurah.ui.rememberContainer
import debeski.jurah.ui.theme.GlassShapeSmall
import debeski.jurah.ui.theme.Missed
import debeski.jurah.ui.theme.Taken

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(onBack: () -> Unit) {
    val container = rememberContainer()
    val viewModel: HistoryViewModel = viewModel(factory = HistoryViewModel.factory(container))
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val byDay = remember(state.rows) {
        state.rows.groupBy { DoseFormat.startOfDay(it.scheduledAtMillis) }
    }

    JurahBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(R.string.nav_history)) },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = MaterialTheme.colorScheme.onBackground,
                        navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
                    ),
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(JurahIcons.Back, stringResource(R.string.nav_back))
                        }
                    },
                )
            },
        ) { insets ->
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = insets.calculateTopPadding() + 8.dp,
                    bottom = insets.calculateBottomPadding() + 24.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (state.loaded && state.rows.isEmpty()) {
                    item {
                        GlassCard(modifier = Modifier.padding(top = 48.dp)) {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                Icon(
                                    JurahIcons.History,
                                    contentDescription = null,
                                    modifier = Modifier.size(36.dp),
                                    tint = MaterialTheme.colorScheme.primary,
                                )
                                Text(
                                    text = stringResource(R.string.history_empty_title),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    textAlign = TextAlign.Center,
                                )
                                Text(
                                    text = stringResource(R.string.history_empty_body),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center,
                                )
                            }
                        }
                    }
                }

                byDay.forEach { (day, rows) ->
                    item(key = "day-$day") { SectionHeader(DoseFormat.date(context, day)) }
                    items(rows, key = { it.key }) { row -> HistoryRowItem(row) }
                }
            }
        }
    }
}

@Composable
private fun HistoryRowItem(row: HistoryRow) {
    val context = LocalContext.current
    val scheme = MaterialTheme.colorScheme
    val accent = when (row.status) {
        HistoryStatus.TAKEN -> Taken
        HistoryStatus.MISSED -> Missed
        HistoryStatus.SKIPPED -> scheme.onSurfaceVariant
    }
    val statusLabel = stringResource(
        when (row.status) {
            HistoryStatus.TAKEN -> R.string.status_taken
            HistoryStatus.MISSED -> R.string.status_missed
            HistoryStatus.SKIPPED -> R.string.status_skipped
        }
    )

    GlassCard(shape = GlassShapeSmall) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = if (row.status == HistoryStatus.TAKEN) JurahIcons.Check else JurahIcons.Close,
                contentDescription = statusLabel,
                tint = accent,
                modifier = Modifier.size(20.dp),
            )
            Column(modifier = Modifier.padding(start = 12.dp).fillMaxWidth()) {
                Text(
                    text = row.scheduleName?.takeIf { it.isNotBlank() }
                        ?: stringResource(R.string.unnamed_schedule),
                    style = MaterialTheme.typography.titleSmall,
                    color = scheme.onBackground,
                )
                Text(
                    text = buildString {
                        append(DoseFormat.time(context, row.scheduledAtMillis))
                        append(" · ")
                        append(statusLabel)
                        row.actedAtMillis?.let {
                            append(" · ")
                            append(
                                context.getString(
                                    R.string.history_acted_at,
                                    DoseFormat.time(context, it),
                                )
                            )
                        }
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = scheme.onSurfaceVariant,
                )
            }
        }
    }
}
