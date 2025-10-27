package com.gymway.workout.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.gymway.workout.data.model.WorkoutPlan
import com.gymway.workout.viewmodel.WorkoutViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutHomeScreen(
    navController: NavController,
    workoutViewModel: WorkoutViewModel
) {
    println("🔄 [WorkoutHomeScreen] کامپوز شدن")

    val workoutPlans by workoutViewModel.workoutPlans.collectAsState()
    val isLoading by workoutViewModel.isLoading.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    println("📊 [WorkoutHomeScreen] وضعیت: isLoading=$isLoading, plans=${workoutPlans.size}")

    LaunchedEffect(Unit) {
        println("🏠 [WorkoutHomeScreen] LaunchedEffect اجرا شد")
        // workoutViewModel.loadWorkoutPlans() // موقتاً غیرفعال
    }

    val plansWithProgress = remember(workoutPlans) {
        println("🧮 [WorkoutHomeScreen] محاسبه پیشرفت برای ${workoutPlans.size} برنامه")
        workoutPlans.map { plan ->
            val progress = if (plan.exercises.isEmpty()) 0f
            else {
                val completed = plan.exercises.count { it.isCompleted }
                completed.toFloat() / plan.exercises.size
            }
            plan to (progress * 100).toInt()
        }.also { plans ->
            plans.forEach { (plan, progress) ->
                println("   📈 ${plan.title}: $progress%")
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text("برنامه‌های تمرینی")
                    println("🎯 [WorkoutHomeScreen] TopAppBar کامپوز شد")
                },
                navigationIcon = {
                    IconButton(onClick = {
                        println("🔙 [WorkoutHomeScreen] کاربر دکمه back را زد")
                        navController.popBackStack()
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "بازگشت")
                    }
                }
            )
        }
    ) { padding ->
        println("🎨 [WorkoutHomeScreen] Scaffold content کامپوز شد")

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            if (isLoading) {
                println("⏳ [WorkoutHomeScreen] نمایش حالت loading")
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("درحال بارگذاری...")
                    }
                }
            } else if (workoutPlans.isEmpty()) {
                println("📭 [WorkoutHomeScreen] نمایش حالت خالی")
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.FitnessCenter,
                            contentDescription = "بدون برنامه",
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("برنامه تمرینی ندارید")
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                println("🔄 [WorkoutHomeScreen] کاربر بارگذاری مجدد را زد")
                                coroutineScope.launch {
                                    workoutViewModel.loadWorkoutPlans()
                                }
                            }
                        ) {
                            Text("بارگذاری مجدد")
                        }
                    }
                }
            } else {
                println("📋 [WorkoutHomeScreen] نمایش ${plansWithProgress.size} برنامه")
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(plansWithProgress) { (plan, progress) ->
                        WorkoutPlanCard(
                            workoutPlan = plan,
                            progress = progress,
                            onCardClick = {
                                println("🎯 [WorkoutHomeScreen] کاربر روی برنامه کلیک کرد: ${plan.title}")
                                navController.navigate("workout_detail/${plan.id}")
                            }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutPlanCard(
    workoutPlan: WorkoutPlan,
    progress: Int,
    onCardClick: () -> Unit
) {
    println("🎨 [WorkoutPlanCard] کامپوز شدن: ${workoutPlan.title} - $progress%")

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp),
        onClick = onCardClick
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = workoutPlan.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = workoutPlan.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(12.dp))

            LinearProgressIndicator(
                progress = progress / 100f,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "پیشرفت: $progress%",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = "${workoutPlan.exercises.count { it.isCompleted }} از ${workoutPlan.exercises.size} تمرین تکمیل شده",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}