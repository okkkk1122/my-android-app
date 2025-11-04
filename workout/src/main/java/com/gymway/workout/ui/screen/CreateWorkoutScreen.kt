package com.gymway.workout.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.gymway.workout.data.model.Exercise
import com.gymway.workout.data.model.WorkoutPlan
import com.gymway.workout.viewmodel.CoachViewModel
import kotlinx.coroutines.launch
import java.util.*

@Composable
fun CreateWorkoutScreen(
    navController: NavController,
    coachViewModel: CoachViewModel
) {
    println("🎯 [CreateWorkoutScreen] کامپوز شدن - شروع")

    // State برای مدیریت خطا
    var hasError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    // اگر خطا داریم، صفحه خطا رو نشون بده
    if (hasError) {
        ErrorFallbackScreen(
            errorMessage = errorMessage,
            onBack = { navController.popBackStack() }
        )
        return
    }

    // صفحه اصلی
    SafeCreateWorkoutScreen(
        navController = navController,
        coachViewModel = coachViewModel,
        onError = { message ->
            hasError = true
            errorMessage = message
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SafeCreateWorkoutScreen(
    navController: NavController,
    coachViewModel: CoachViewModel,
    onError: (String) -> Unit
) {
    println("🔄 [SafeCreateWorkoutScreen] کامپوز شدن")

    var workoutTitle by remember { mutableStateOf("") }
    var workoutDescription by remember { mutableStateOf("") }
    var selectedDifficulty by remember { mutableStateOf("beginner") }
    var workoutDuration by remember { mutableStateOf("60") }
    var showSuccess by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorDialogMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    // برای LaunchedEffect
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text("ایجاد برنامه جدید")
                    println("🎯 [CreateWorkoutScreen] TopAppBar کامپوز شد")
                },
                navigationIcon = {
                    IconButton(onClick = {
                        println("🔙 [CreateWorkoutScreen] کاربر دکمه back را زد")
                        navController.popBackStack()
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "بازگشت")
                    }
                }
            )
        }
    ) { padding ->
        println("🎨 [CreateWorkoutScreen] Scaffold content کامپوز شد")

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "📋 ایجاد برنامه تمرینی",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            // فرم ساده و ایمن
            OutlinedTextField(
                value = workoutTitle,
                onValueChange = { workoutTitle = it },
                label = { Text("عنوان برنامه *") },
                modifier = Modifier.fillMaxWidth(),
                isError = workoutTitle.isEmpty()
            )

            OutlinedTextField(
                value = workoutDescription,
                onValueChange = { workoutDescription = it },
                label = { Text("توضیحات برنامه") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = false,
                maxLines = 3
            )

            OutlinedTextField(
                value = workoutDuration,
                onValueChange = {
                    if (it.all { char -> char.isDigit() }) workoutDuration = it
                },
                label = { Text("مدت زمان (دقیقه)") },
                modifier = Modifier.fillMaxWidth()
            )

            // سطح دشواری - با استفاده از RadioButton به جای FilterChip
            Text("🎯 سطح دشواری:", style = MaterialTheme.typography.bodyMedium)

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("beginner" to "مبتدی", "intermediate" to "متوسط", "advanced" to "پیشرفته")
                    .forEach { (value, text) ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedDifficulty == value,
                                onClick = {
                                    println("🎯 [CreateWorkoutScreen] سطح دشواری: $value")
                                    selectedDifficulty = value
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = text,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.clickable {
                                    selectedDifficulty = value
                                }
                            )
                        }
                    }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // دکمه ذخیره
            Button(
                onClick = {
                    println("💾 [CreateWorkoutScreen] کاربر دکمه ذخیره را زد")

                    if (workoutTitle.isEmpty()) {
                        errorDialogMessage = "لطفاً عنوان برنامه را وارد کنید"
                        showErrorDialog = true
                        return@Button
                    }

                    isLoading = true

                    // استفاده از coroutineScope برای عملیات async
                    coroutineScope.launch {
                        try {
                            // ایجاد برنامه ساده و ایمن
                            val newWorkoutPlan = WorkoutPlan(
                                id = "plan_${UUID.randomUUID()}",
                                title = workoutTitle,
                                description = workoutDescription,
                                exercises = emptyList(), // لیست خالی برای شروع
                                duration = workoutDuration.toIntOrNull() ?: 60,
                                difficulty = selectedDifficulty,
                                createdBy = "coach_1", // مقدار موقت
                                assignedTo = "user_123" // مقدار موقت
                            )

                            println("🆕 [CreateWorkoutScreen] ایجاد برنامه: ${newWorkoutPlan.title}")

                            // ذخیره در ViewModel
                            coachViewModel.createWorkoutPlan(newWorkoutPlan)
                            showSuccess = true

                        } catch (e: Exception) {
                            println("💥 [CreateWorkoutScreen] خطا در ایجاد برنامه: ${e.message}")
                            errorDialogMessage = "خطا در ایجاد برنامه: ${e.message}"
                            showErrorDialog = true
                            e.printStackTrace()

                            // برای خطاهای جدی
                            if (e is NoSuchMethodError || e is IllegalArgumentException) {
                                onError("خطای سیستمی: ${e.message}")
                            }
                        } finally {
                            isLoading = false
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = workoutTitle.isNotEmpty() && !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("در حال ذخیره...", style = MaterialTheme.typography.titleMedium)
                } else {
                    Icon(Icons.Default.Save, contentDescription = "ذخیره")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("ذخیره برنامه", style = MaterialTheme.typography.titleMedium)
                }
            }

            // راهنما
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("💡 راهنما", style = MaterialTheme.typography.labelLarge)
                    Text(
                        "• عنوان برنامه الزامی است\n" +
                                "• می‌توانید بعداً تمرین‌ها را اضافه کنید\n" +
                                "• برنامه به صورت خودکار ذخیره می‌شود",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }

    // LaunchedEffect برای پیام موفقیت
    LaunchedEffect(showSuccess) {
        if (showSuccess) {
            println("✅ [CreateWorkoutScreen] نمایش پیام موفقیت")
            kotlinx.coroutines.delay(2000)
            navController.popBackStack()
        }
    }

    // پیام موفقیت
    if (showSuccess) {
        AlertDialog(
            onDismissRequest = { /* غیرقابل بستن */ },
            title = { Text("✅ موفقیت") },
            text = { Text("برنامه '${workoutTitle}' با موفقیت ایجاد شد!") },
            confirmButton = {
                TextButton(onClick = { }) {
                    Text("در حال بازگشت...")
                }
            }
        )
    }

    // پیام خطا
    if (showErrorDialog) {
        AlertDialog(
            onDismissRequest = { showErrorDialog = false },
            title = { Text("❌ خطا") },
            text = { Text(errorDialogMessage) },
            confirmButton = {
                TextButton(onClick = { showErrorDialog = false }) {
                    Text("متوجه شدم")
                }
            }
        )
    }
}

@Composable
fun ErrorFallbackScreen(
    errorMessage: String,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.Error,
            contentDescription = "خطا",
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text("⚠️ خطا در ایجاد برنامه", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            errorMessage,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onBack) {
            Text("بازگشت")
        }
    }
}