
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.gymway.workout.viewmodel.CoachViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoachAthletesScreen(
    navController: NavController,
    coachViewModel: CoachViewModel
) {
    println("🔄 [CoachAthletesScreen] کامپوز شدن")

    val coaches by coachViewModel.coaches.collectAsState()
    val isLoading by coachViewModel.isLoading.collectAsState()

    // ورزشکاران نمونه - در نسخه واقعی از Firebase می‌گیریم
    val sampleAthletes = listOf(
        AthleteInfo("user_123", "محمد احمدی", "۳ برنامه فعال", "۸۵% پیشرفت"),
        AthleteInfo("user_456", "فاطمه زارعی", "۲ برنامه فعال", "۷۲% پیشرفت"),
        AthleteInfo("user_789", "علی رضایی", "۱ برنامه فعال", "۹۰% پیشرفت")
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text("مدیریت ورزشکاران")
                    println("🎯 [CoachAthletesScreen] TopAppBar کامپوز شد")
                },
                navigationIcon = {
                    IconButton(onClick = {
                        println("🔙 [CoachAthletesScreen] کاربر دکمه back را زد")
                        navController.popBackStack()
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "بازگشت")
                    }
                }
            )
        }
    ) { padding ->
        println("🎨 [CoachAthletesScreen] Scaffold content کامپوز شد")

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // آمار کلی
            AthletesStatsCard(athletesCount = sampleAthletes.size)

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "لیست ورزشکاران",
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
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    println("👥 [CoachAthletesScreen] نمایش ${sampleAthletes.size} ورزشکار")
                    items(sampleAthletes) { athlete ->
                        AthleteCard(
                            athlete = athlete,
                            onViewProgress = {
                                println("🔍 [CoachAthletesScreen-NAV] کلیک مشاهده پیشرفت")
                                val targetRoute = "athlete_progress/${athlete.id}"
                                println("🔍 [CoachAthletesScreen-NAV] targetRoute: $targetRoute")

                                val currentRoute = navController.currentBackStackEntry?.destination?.route
                                println("🔍 [CoachAthletesScreen-NAV] currentRoute: $currentRoute")

                                if (currentRoute != targetRoute) {
                                    println("🔍 [CoachAthletesScreen-NAV] انجام navigate")
                                    navController.navigate(targetRoute)
                                } else {
                                    println("⚠️ [CoachAthletesScreen-NAV] قبلاً در این صفحه هستیم - navigate نکن")
                                }
                            },
                            onAssignWorkout = {
                                println("🎯 [CoachAthletesScreen] اختصاص برنامه: ${athlete.name}")
                                // اصلاح: استفاده از route درست
                                navController.navigate("create_workout")
                            }
                        )
                    }
                }
            }
        }
    }
}

data class AthleteInfo(
    val id: String,
    val name: String,
    val activeWorkouts: String,
    val progress: String
)

@Composable
fun AthletesStatsCard(athletesCount: Int) {
    println("📊 [AthletesStatsCard] کامپوز شدن - $athletesCount ورزشکار")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            AthleteStatItem(
                value = athletesCount.toString(),
                label = "ورزشکار فعال",
                icon = Icons.Default.People
            )

            AthleteStatItem(
                value = "${athletesCount * 2}",
                label = "برنامه فعال",
                icon = Icons.Default.FitnessCenter
            )

            AthleteStatItem(
                value = "۸۲%",
                label = "میانگین پیشرفت",
                icon = Icons.Default.TrendingUp
            )
        }
    }
}

@Composable
fun AthleteStatItem(value: String, label: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            modifier = Modifier.size(28.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}

@Composable
fun AthleteCard(
    athlete: AthleteInfo,
    onViewProgress: () -> Unit,
    onAssignWorkout: () -> Unit
) {
    println("🎨 [AthleteCard] کامپوز شدن: ${athlete.name}")

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
                        text = athlete.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "ID: ${athlete.id}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }

                // Avatar or initial
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = athlete.name.take(1),
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // آمار ورزشکار
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                AthleteDetailItem(
                    value = athlete.activeWorkouts,
                    label = "برنامه فعال"
                )

                AthleteDetailItem(
                    value = athlete.progress,
                    label = "پیشرفت"
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // دکمه‌های action
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onViewProgress,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                ) {
                    Icon(Icons.Default.Analytics, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("پیشرفت")
                }

                Button(
                    onClick = onAssignWorkout,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Assignment, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("اختصاص برنامه")
                }
            }
        }
    }
}

@Composable
fun AthleteDetailItem(value: String, label: String) {
    Column {
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}
