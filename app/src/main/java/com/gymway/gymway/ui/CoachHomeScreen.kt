package com.gymway.gymway.ui

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.gymway.auth.model.User
import com.gymway.gymway.navigation.Routes

private const val TAG = "CoachHomeScreen"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoachHomeScreen(
    currentUser: User?,
    onLogout: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToCoachDashboard: () -> Unit,
    navController: NavController // اضافه کردن navController
) {
    Log.d(TAG, "کامپوز شدن CoachHomeScreen")

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "GymWay - مربی ${currentUser?.displayName ?: ""}"
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        floatingActionButton = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // دکمه پروفایل
                FloatingActionButton(
                    onClick = {
                        Log.d(TAG, "کلیک روی پروفایل")
                        onNavigateToProfile()
                    }
                ) {
                    Icon(Icons.Default.Person, contentDescription = "پروفایل")
                }

                // دکمه خروج
                ExtendedFloatingActionButton(
                    onClick = {
                        Log.d(TAG, "کلیک روی خروج")
                        onLogout()
                    },
                    icon = {
                        Icon(
                            Icons.Default.ExitToApp,
                            contentDescription = "خروج"
                        )
                    },
                    text = { Text("خروج") }
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.School,
                contentDescription = "مربی",
                modifier = Modifier.size(120.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "خوش آمدید مربی محترم!",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = currentUser?.displayName?.let { "سلام $it" } ?: "حساب کاربری شما فعال است",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            // نمایش نقش کاربر
            FilterChip(
                selected = true,
                onClick = { },
                label = { Text("مربی") }
            )

            Spacer(modifier = Modifier.height(32.dp))

            // امکانات مخصوص مربیان
            Card(
                onClick = {
                    Log.d(TAG, "کلیک روی مدیریت شاگردان")
                    navController.navigate(Routes.COACH_DASHBOARD)
                },
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Group,
                        contentDescription = "شاگردان",
                        modifier = Modifier.size(40.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "مدیریت شاگردان",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "لیست و اطلاعات شاگردان",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Card(
                onClick = {
                    Log.d(TAG, "کلیک روی طراحی برنامه تمرینی")
                    navController.navigate(Routes.CREATE_WORKOUT)
                },
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Create,
                        contentDescription = "برنامه‌ها",
                        modifier = Modifier.size(40.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "طراحی برنامه تمرینی",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "ساخت برنامه برای شاگردان",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Card(
                onClick = {
                    Log.d(TAG, "کلیک روی آنالیز پیشرفت")
                    // می‌تواند به صفحه آمار کلی برود
                    navController.navigate(Routes.COACH_ATHLETES)
                },
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Analytics,
                        contentDescription = "آنالیتیکس",
                        modifier = Modifier.size(40.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "آنالیز پیشرفت",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "بررسی عملکرد شاگردان",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }

            // دکمه تست Navigation
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = {
                    Log.d(TAG, "تست مستقیم Navigation به CoachDashboard")
                    navController.navigate(Routes.COACH_DASHBOARD)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("تست مستقیم - مدیریت شاگردان")
            }
        }
    }
}