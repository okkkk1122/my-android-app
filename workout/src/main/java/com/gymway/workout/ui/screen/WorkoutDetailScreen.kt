package com.gymway.workout.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.gymway.workout.data.model.Exercise
import com.gymway.workout.viewmodel.WorkoutViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutDetailScreen(
    navController: NavController,
    workoutId: String?,
    workoutViewModel: WorkoutViewModel
) {
    println("🔄 [WorkoutDetailScreen] کامپوز شدن - workoutId: $workoutId")

    val workoutPlans by workoutViewModel.workoutPlans.collectAsState()
    println("📊 [WorkoutDetailScreen] ${workoutPlans.size} برنامه دریافت شد")

    val workoutPlan = workoutId?.let { id ->
        workoutPlans.find { it.id == id }.also { plan ->
            if (plan != null) {
                println("✅ [WorkoutDetailScreen] برنامه پیدا شد: ${plan.title}")
            } else {
                println("❌ [WorkoutDetailScreen] برنامه با ID $id پیدا نشد")
            }
        }
    }

    val progress = workoutPlan?.let { plan ->
        if (plan.exercises.isEmpty()) 0f
        else {
            val completed = plan.exercises.count { it.isCompleted }
            completed.toFloat() / plan.exercises.size
        }
    } ?: 0f

    println("📈 [WorkoutDetailScreen] پیشرفت برنامه: ${(progress * 100).toInt()}%")

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text("جزئیات تمرین")
                    println("🎯 [WorkoutDetailScreen] TopAppBar کامپوز شد")
                },
                navigationIcon = {
                    IconButton(onClick = {
                        println("🔙 [WorkoutDetailScreen] کاربر دکمه back را زد")
                        navController.popBackStack()
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "بازگشت")
                    }
                },
                actions = {
                    workoutPlan?.let {
                        IconButton(
                            onClick = {
                                println("✅ [WorkoutDetailScreen] کاربر تکمیل همه را زد: ${it.title}")
                                workoutViewModel.markAllExercisesCompleted(it.id)
                            }
                        ) {
                            Icon(Icons.Default.Check, contentDescription = "تکمیل همه")
                        }
                    }
                }
            )
        }
    ) { padding ->
        println("🎨 [WorkoutDetailScreen] Scaffold content کامپوز شد")

        when {
            workoutId == null -> {
                println("❌ [WorkoutDetailScreen] حالت: workoutId null")
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("شناسه برنامه نامعتبر است")
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                println("🔙 [WorkoutDetailScreen] بازگشت از حالت خطا")
                                navController.popBackStack()
                            }
                        ) {
                            Text("بازگشت")
                        }
                    }
                }
            }

            workoutPlan == null -> {
                println("❌ [WorkoutDetailScreen] حالت: برنامه پیدا نشد")
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("برنامه تمرینی یافت نشد")
                        Text("ID: $workoutId", style = MaterialTheme.typography.bodySmall)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                println("🔙 [WorkoutDetailScreen] بازگشت از حالت برنامه پیدا نشد")
                                navController.popBackStack()
                            }
                        ) {
                            Text("بازگشت به صفحه اصلی")
                        }
                    }
                }
            }

            else -> {
                println("✅ [WorkoutDetailScreen] حالت عادی: نمایش برنامه")
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = workoutPlan.title,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = workoutPlan.description,
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        LinearProgressIndicator(
                            progress = progress,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(12.dp),
                            trackColor = MaterialTheme.colorScheme.surfaceVariant,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "پیشرفت: ${(progress * 100).toInt()}%",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Text(
                            text = "${workoutPlan.exercises.count { it.isCompleted }} از ${workoutPlan.exercises.size} تمرین تکمیل شده",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        println("📝 [WorkoutDetailScreen] LazyColumn با ${workoutPlan.exercises.size} تمرین")

                        items(workoutPlan.exercises) { exercise ->
                            ExerciseItem(
                                exercise = exercise,
                                onCheckedChange = { isChecked ->
                                    println("🎯 [WorkoutDetailScreen] کاربر تیک زد: ${exercise.name} -> $isChecked")
                                    workoutViewModel.toggleExerciseCompletion(
                                        workoutPlan.id,
                                        exercise.id,
                                        isChecked
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ExerciseItem(
    exercise: Exercise,
    onCheckedChange: (Boolean) -> Unit
) {
    println("🎨 [ExerciseItem] کامپوز شدن: ${exercise.name} - وضعیت: ${exercise.isCompleted}")

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = exercise.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = exercise.getDisplayText(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }

            Checkbox(
                checked = exercise.isCompleted,
                onCheckedChange = onCheckedChange,
                colors = CheckboxDefaults.colors(
                    checkedColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    }
}