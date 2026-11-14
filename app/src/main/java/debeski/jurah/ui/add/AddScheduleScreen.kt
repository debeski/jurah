package debeski.jurah.ui.add

import android.text.format.DateFormat
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import debeski.jurah.R
import debeski.jurah.domain.model.Schedule
import debeski.jurah.domain.schedule.DoseCalculator
import debeski.jurah.domain.schedule.IntervalPreset
import debeski.jurah.ui.components.GlassCard
import debeski.jurah.ui.components.JurahBackground
import debeski.jurah.ui.components.JurahIcons
import debeski.jurah.ui.components.SectionHeader
import debeski.jurah.ui.format.DoseFormat
import debeski.jurah.ui.rememberContainer
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddScheduleScreen(
    onSaved: () -> Unit,
    onBack: () -> Unit,
) {
    val container = rememberContainer()
    val viewModel: AddScheduleViewModel = viewModel(factory = AddScheduleViewModel.factory(container))
    val context = LocalContext.current

    var name by rememberSaveable { mutableStateOf("") }
    var epochDay by rememberSaveable { mutableLongStateOf(LocalDate.now().toEpochDay()) }
    var hour by rememberSaveable { mutableIntStateOf(LocalTime.now().hour) }
    var minute by rememberSaveable { mutableIntStateOf(0) }
    var presetMinutes by rememberSaveable { mutableIntStateOf(IntervalPreset.EIGHT_HOURLY.minutes) }
    var customHours by rememberSaveable { mutableIntStateOf(8) }
    var useCustom by rememberSaveable { mutableStateOf(false) }
    var ongoing by rememberSaveable { mutableStateOf(false) }
    var durationDays by rememberSaveable { mutableIntStateOf(7) }

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    val intervalMinutes = if (useCustom) customHours * 60 else presetMinutes
    val startAtMillis = remember(epochDay, hour, minute) {
        LocalDate.ofEpochDay(epochDay)
            .atTime(hour, minute)
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
    }
    val preview = Schedule(
        startAtMillis = startAtMillis,
        intervalMinutes = intervalMinutes,
        durationDays = if (ongoing) null else durationDays,
    )

    JurahBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(R.string.add_title)) },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = MaterialTheme.colorScheme.onBackground,
                        navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
                        actionIconContentColor = MaterialTheme.colorScheme.primary,
                    ),
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(JurahIcons.Back, stringResource(R.string.nav_back))
                        }
                    },
                    actions = {
                        IconButton(onClick = {
                            viewModel.create(
                                name = name,
                                startAtMillis = startAtMillis,
                                intervalMinutes = intervalMinutes,
                                durationDays = if (ongoing) null else durationDays,
                                onSaved = onSaved,
                            )
                        }) {
                            Icon(JurahIcons.Check, stringResource(R.string.action_save))
                        }
                    },
                )
            },
        ) { insets ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(
                        start = 16.dp,
                        end = 16.dp,
                        top = insets.calculateTopPadding() + 8.dp,
                        bottom = insets.calculateBottomPadding() + 24.dp,
                    ),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                GlassCard {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text(stringResource(R.string.field_name_label)) },
                        placeholder = { Text(stringResource(R.string.field_name_placeholder)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                SectionHeader(stringResource(R.string.section_first_dose))
                GlassCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        TextButton(onClick = { showDatePicker = true }) {
                            Icon(JurahIcons.Calendar, contentDescription = null)
                            Text(
                                text = DoseFormat.date(context, startAtMillis),
                                modifier = Modifier.padding(start = 8.dp),
                            )
                        }
                        TextButton(onClick = { showTimePicker = true }) {
                            Icon(JurahIcons.Clock, contentDescription = null)
                            Text(
                                text = DoseFormat.time(context, startAtMillis),
                                modifier = Modifier.padding(start = 8.dp),
                            )
                        }
                    }
                }

                SectionHeader(stringResource(R.string.section_interval))
                GlassCard {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IntervalPreset.entries.forEach { preset ->
                            FilterChip(
                                selected = !useCustom && presetMinutes == preset.minutes,
                                onClick = {
                                    useCustom = false
                                    presetMinutes = preset.minutes
                                },
                                label = { Text(DoseFormat.interval(context, preset.minutes)) },
                                colors = brandChipColors(),
                            )
                        }
                        FilterChip(
                            selected = useCustom,
                            onClick = { useCustom = true },
                            label = { Text(stringResource(R.string.interval_custom)) },
                            colors = brandChipColors(),
                        )
                    }
                    if (useCustom) {
                        Stepper(
                            label = stringResource(R.string.custom_hours, customHours),
                            onDecrease = { customHours = (customHours - 1).coerceAtLeast(1) },
                            onIncrease = { customHours = (customHours + 1).coerceAtMost(72) },
                        )
                    }
                }

                SectionHeader(stringResource(R.string.section_duration))
                GlassCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = stringResource(R.string.duration_ongoing),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.weight(1f),
                        )
                        Switch(checked = ongoing, onCheckedChange = { ongoing = it })
                    }
                    if (!ongoing) {
                        Stepper(
                            label = DoseFormat.days(context, durationDays),
                            onDecrease = { durationDays = (durationDays - 1).coerceAtLeast(1) },
                            onIncrease = { durationDays = (durationDays + 1).coerceAtMost(365) },
                        )
                    }
                }

                GlassCard {
                    val total = DoseCalculator.totalDoses(preview)
                    val endAt = DoseCalculator.lastDoseAt(preview)
                    Text(
                        text = if (total != null && endAt != null) {
                            stringResource(
                                R.string.summary_finite,
                                DoseFormat.doses(context, total),
                                DoseFormat.dateTime(context, endAt),
                            )
                        } else {
                            stringResource(R.string.summary_ongoing)
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.secondary,
                    )
                }
            }
        }
    }

    if (showDatePicker) {
        // DatePicker works in UTC, so the chosen day is converted back through UTC
        // before being recombined with the local wall-clock time.
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = LocalDate.ofEpochDay(epochDay)
                .atStartOfDay(ZoneOffset.UTC)
                .toInstant()
                .toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { millis ->
                        epochDay = Instant.ofEpochMilli(millis)
                            .atZone(ZoneOffset.UTC)
                            .toLocalDate()
                            .toEpochDay()
                    }
                    showDatePicker = false
                }) { Text(stringResource(R.string.action_confirm)) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            },
        ) {
            DatePicker(state = pickerState, title = { Text(stringResource(R.string.pick_date), Modifier.padding(16.dp)) })
        }
    }

    if (showTimePicker) {
        val timeState = rememberTimePickerState(
            initialHour = hour,
            initialMinute = minute,
            is24Hour = DateFormat.is24HourFormat(context),
        )
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text(stringResource(R.string.pick_time)) },
            text = { TimePicker(state = timeState) },
            confirmButton = {
                TextButton(onClick = {
                    hour = timeState.hour
                    minute = timeState.minute
                    showTimePicker = false
                }) { Text(stringResource(R.string.action_confirm)) }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            },
        )
    }
}

/** Material defaults a selected chip to secondaryContainer, which is blue here. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun brandChipColors() = FilterChipDefaults.filterChipColors(
    labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
    selectedContainerColor = MaterialTheme.colorScheme.primary,
    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
)

@Composable
private fun Stepper(label: String, onDecrease: () -> Unit, onIncrease: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.weight(1f),
        )
        IconButton(onClick = onDecrease) {
            Icon(JurahIcons.Minus, stringResource(R.string.decrease), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        IconButton(onClick = onIncrease) {
            Icon(JurahIcons.Add, stringResource(R.string.increase), tint = MaterialTheme.colorScheme.primary)
        }
    }
}
