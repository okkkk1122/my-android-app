package com.gymway.workout.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.gymway.workout.viewmodel.WorkoutViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class) // این خط رو اضافه کن
@Composable
fun ProgressScreen(
    workoutViewModel: WorkoutViewModel,
    userId: String,
    onBack: () -> Unit
) {
    println("🎯 [ProgressScreen] کامپوز شدن - userId: $userId")

    val progressHistory by workoutViewModel.progressHistory.collectAsState()
    val isLoading by workoutViewModel.isLoading.collectAsState()

    LaunchedEffect(userId) {
        println("🎯 [ProgressScreen] LaunchedEffect اجرا شد")
        workoutViewModel.loadProgressHistory(userId)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("پیشرفت من") },
                navigationIcon = {
                    IconButton(onClick = {
                        println("🔙 [ProgressScreen] بازگشت")
                        onBack()
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "بازگشت")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    println("➕ [ProgressScreen] ایجاد پیشرفت جدید")
                    workoutViewModel.createSampleProgress(userId)
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = "ثبت پیشرفت جدید")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("در حال بارگذاری تاریخچه...")
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // آمار کلی
                    ProgressStats(progressHistory = progressHistory)

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "تاریخچه پیشرفت",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    if (progressHistory.isEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Default.Assessment,
                                contentDescription = "بدون داده",
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.outline
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("داده‌ای برای نمایش وجود ندارد")
                            Text(
                                "اولین ثبت پیشرفت خود را انجام دهید",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(progressHistory.reversed()) { progress ->
                                ProgressItem(progress = progress)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProgressStats(progressHistory: List<com.gymway.workout.data.model.WorkoutProgress>) {
    val latestProgress = progressHistory.lastOrNull()

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "آمار کلی",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ProgressStatItem(
                    value = progressHistory.size.toString(),
                    label = "جلسات ثبت شده"
                )

                ProgressStatItem(
                    value = if (latestProgress?.weight != null) "${latestProgress.weight} kg" else "-",
                    label = "وزن فعلی"
                )

                ProgressStatItem(
                    value = if (latestProgress?.bodyFat != null) "${latestProgress.bodyFat}%" else "-",
                    label = "چربی بدن"
                )
            }
        }
    }
}

@Composable
fun ProgressStatItem(value: String, label: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(80.dp)
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            textAlign = TextAlign.Center
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.outline,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun ProgressItem(progress: com.gymway.workout.data.model.WorkoutProgress) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // تاریخ
            Text(
                text = formatDate(progress.date),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            // آمار تمرین
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ProgressDetailStat(
                    value = "${progress.completedExercises}/${progress.totalExercises}",
                    label = "تمرینات تکمیل شده"
                )

                ProgressDetailStat(
                    value = "${(progress.completionRate * 100).toInt()}%",
                    label = "نرخ تکمیل"
                )
            }

            // وزن و چربی بدن
            if (progress.weight != null || progress.bodyFat != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (progress.weight != null) {
                        ProgressDetailStat(
                            value = "${progress.weight} kg",
                            label = "وزن"
                        )
                    }

                    if (progress.bodyFat != null) {
                        ProgressDetailStat(
                            value = "${progress.bodyFat}%",
                            label = "چربی بدن"
                        )
                    }
                }
            }

            // یادداشت‌ها
            if (progress.notes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = progress.notes,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

@Composable
fun ProgressDetailStat(value: String, label: String) {
    Column {
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.outline
        )
    }
}

fun formatDate(timestamp: com.google.firebase.Timestamp): String {
    return try {
        val date = timestamp.toDate()
        val formatter = SimpleDateFormat("yyyy/MM/dd - HH:mm", Locale("fa", "IR"))
        formatter.format(date)
    } catch (e: Exception) {
        "تاریخ نامعتبر"
    }
}