package com.gymway.gymway.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gymway.auth.AuthUiState
import com.gymway.auth.AuthViewModel
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmailVerificationScreen(
    authViewModel: AuthViewModel = viewModel(),
    onVerified: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    var checkCount by remember { mutableStateOf(0) }
    var lastCheckTime by remember { mutableStateOf("") }
    var autoCheckEnabled by remember { mutableStateOf(true) }

    val uiState by authViewModel.uiState.collectAsState()

    // Auto-check every 8 seconds
    LaunchedEffect(autoCheckEnabled) {
        while (autoCheckEnabled) {
            delay(8000)
            checkCount++
            lastCheckTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                .format(Date())
            authViewModel.checkEmailVerification()
        }
    }

    // Handle UI state changes
    LaunchedEffect(uiState) {
        when (uiState) {
            is AuthUiState.Success -> {
                autoCheckEnabled = false
                onVerified()
            }
            is AuthUiState.Error -> {
                // Handle errors (could show snackbar)
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("تأیید ایمیل") },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Icon
            Icon(
                imageVector = Icons.Default.Email,
                contentDescription = "Email Verification",
                modifier = Modifier.size(120.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Title
            Text(
                text = "لینک تأیید ایمیل ارسال شد",
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Instructions
            Text(
                text = "برای فعال‌سازی حساب خود، لطفاً به صندوق ایمیل خود مراجعه کرده و روی لینک تأیید کلیک کنید.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.3
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "پس از تأیید ایمیل، به صورت خودکار به برنامه منتقل خواهید شد.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Check Verification Button
            Button(
                onClick = {
                    checkCount++
                    lastCheckTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                        .format(Date())
                    authViewModel.checkEmailVerification()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = uiState !is AuthUiState.Loading
            ) {
                if (uiState is AuthUiState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Check Verification",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("بررسی وضعیت تأیید", style = MaterialTheme.typography.titleMedium)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Resend Email Button
            OutlinedButton(
                onClick = {
                    authViewModel.resendVerificationEmail()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = uiState !is AuthUiState.Loading
            ) {
                Text("ارسال مجدد ایمیل تأیید")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Go to Login Button
            TextButton(
                onClick = {
                    autoCheckEnabled = false
                    onNavigateToLogin()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("بازگشت به صفحه ورود")
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Status Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "وضعیت بررسی",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Check Count
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("تعداد بررسی:", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            "$checkCount بار",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Last Check Time
                    if (lastCheckTime.isNotEmpty()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("آخرین بررسی:", style = MaterialTheme.typography.bodyMedium)
                            Text(
                                lastCheckTime,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // Auto Check Status
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("بررسی خودکار:", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            if (autoCheckEnabled) "فعال" else "غیرفعال",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (autoCheckEnabled) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.error
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Auto Check Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("بررسی هر ۸ ثانیه", style = MaterialTheme.typography.bodySmall)

                        Switch(
                            checked = autoCheckEnabled,
                            onCheckedChange = { autoCheckEnabled = it },
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Help Text
            Text(
                text = "اگر ایمیل را دریافت نکرده‌اید، پوشه Spam را بررسی کنید",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline,
                textAlign = TextAlign.Center
            )
        }
    }
}

// Extension for better state handling
private fun AuthUiState.isLoading(): Boolean {
    return this is AuthUiState.Loading
}