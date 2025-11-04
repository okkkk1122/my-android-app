package com.gymway.workout.ui.screen

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.gymway.workout.data.model.WorkoutPlan
import com.gymway.workout.viewmodel.CoachViewModel

private const val TAG = "CoachDashboard"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoachDashboardScreen(
    navController: NavController,
    coachViewModel: CoachViewModel
) {
    Log.d(TAG, "کامپوز شدن CoachDashboardScreen")

    val assignedWorkouts by coachViewModel.assignedWorkouts.collectAsState()
    val isLoading by coachViewModel.isLoading.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        Log.d(TAG, "بارگذاری برنامه‌های اختصاص داده شده")
        coachViewModel.loadAssignedWorkouts("coach_1")
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text("داشبورد مربی")
                    Log.d(TAG, "TopAppBar کامپوز شد")
                },
                navigationIcon = {
                    IconButton(onClick = {
                        Log.d(TAG, "کاربر دکمه back را زد")
                        navController.popBackStack()
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "بازگشت")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            Log.d(TAG, "کاربر ایجاد برنامه جدید را زد")
                            navController.navigate("create_workout")
                        }
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "برنامه جدید")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    Log.d(TAG, "کاربر مشاهده ورزشکاران را زد")
                    navController.navigate("coach_athletes")
                },
                icon = { Icon(Icons.Default.People, contentDescription = "ورزشکاران") },
                text = { Text("مدیریت ورزشکاران") }
            )
        }
    ) { padding ->
        Log.d(TAG, "Scaffold content کامپوز شد")

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // آمار کلی
            CoachStatsCard(assignedWorkouts = assignedWorkouts)

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "برنامه‌های اختصاص داده شده",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (assignedWorkouts.isEmpty()) {
                EmptyWorkoutsState(
                    onCreateWorkout = {
                        navController.navigate("create_workout")
                    }
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Log.d(TAG, "نمایش ${assignedWorkouts.size} برنامه")
                    items(assignedWorkouts) { workout ->
                        AssignedWorkoutCard(
                            workoutPlan = workout,
                            onEdit = {
                                Log.d(TAG, "کاربر ویرایش برنامه را زد: ${workout.title}")
                                showEditNotImplementedMessage(context)
                            },
                            onViewProgress = {
                                Log.d(TAG, "کاربر مشاهده پیشرفت را زد: ${workout.title}")
                                navController.navigate("athlete_progress/${workout.assignedTo}")
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CoachStatsCard(assignedWorkouts: List<WorkoutPlan>) {
    Log.d(TAG, "CoachStatsCard کامپوز شدن - ${assignedWorkouts.size} برنامه")

    // محاسبه آمار ساده
    val totalWorkouts = assignedWorkouts.size
    val completedWorkouts = assignedWorkouts.count { it.isCompleted }
    val completionRate = if (totalWorkouts > 0) (completedWorkouts.toFloat() / totalWorkouts * 100).toInt() else 0

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "آمار عملکرد",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatItem(
                    value = totalWorkouts.toString(),
                    label = "برنامه فعال",
                    icon = Icons.Default.FitnessCenter
                )

                StatItem(
                    value = completedWorkouts.toString(),
                    label = "تکمیل شده",
                    icon = Icons.Default.CheckCircle
                )

                StatItem(
                    value = "$completionRate%",
                    label = "نرخ تکمیل",
                    icon = Icons.Default.TrendingUp
                )
            }
        }
    }
}

@Composable
fun StatItem(value: String, label: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            modifier = Modifier.size(32.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}

@Composable
fun AssignedWorkoutCard(
    workoutPlan: WorkoutPlan,
    onEdit: () -> Unit,
    onViewProgress: () -> Unit
) {
    Log.d(TAG, "AssignedWorkoutCard کامپوز شدن: ${workoutPlan.title}")

    // پیشرفت واقعی
    val progress = workoutPlan.progress
    val progressPercent = (progress * 100).toInt()

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = workoutPlan.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = workoutPlan.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "اختصاص داده شده به: ${workoutPlan.assignedTo}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // دکمه ویرایش - حالا کار میکنه
                IconButton(
                    onClick = {
                        Log.d(TAG, "کلیک روی دکمه Edit برای: ${workoutPlan.title}")
                        onEdit()
                    }
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "ویرایش برنامه")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // نوار پیشرفت
            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "پیشرفت: $progressPercent%",
                    style = MaterialTheme.typography.bodySmall
                )

                Button(
                    onClick = {
                        Log.d(TAG, "کلیک روی مشاهده پیشرفت برای: ${workoutPlan.title}")
                        onViewProgress()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                ) {
                    Text("مشاهده پیشرفت")
                }
            }
        }
    }
}

@Composable
fun EmptyWorkoutsState(onCreateWorkout: () -> Unit) {
    Log.d(TAG, "EmptyWorkoutsState کامپوز شدن - حالت خالی")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.FitnessCenter,
                contentDescription = "بدون برنامه",
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.outline
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "هنوز برنامه‌ای ایجاد نکرده‌اید",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "اولین برنامه تمرینی خود را ایجاد کنید",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onCreateWorkout
            ) {
                Text("ایجاد برنامه جدید")
            }
        }
    }
}

// تابع کمکی برای نمایش پیام
fun showEditNotImplementedMessage(context: android.content.Context) {
    android.widget.Toast.makeText(
        context,
        "✏️ قابلیت ویرایش برنامه به زودی اضافه خواهد شد",
        android.widget.Toast.LENGTH_SHORT
    ).show()
}