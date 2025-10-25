package com.gymway.gymway.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gymway.auth.AuthViewModel
import com.gymway.auth.model.User

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    authViewModel: AuthViewModel,
    onBack: () -> Unit
) {
    val currentUser by authViewModel.currentUser.collectAsStateWithLifecycle()
    val profileState by authViewModel.profileState.collectAsStateWithLifecycle()

    var displayName by remember { mutableStateOf(currentUser?.displayName ?: "") }
    var age by remember { mutableStateOf(currentUser?.age?.toString() ?: "") }  // آپدیت شد
    var gender by remember { mutableStateOf(currentUser?.gender ?: "") }  // آپدیت شد
    var selectedRole by remember { mutableStateOf(currentUser?.role ?: "athlete") }

    val scrollState = rememberScrollState()

    // Reset form when user data changes
    LaunchedEffect(currentUser)  {
        currentUser?.let { user ->
            displayName = user.displayName
            age = user.age?.toString() ?: ""
            gender = user.gender ?: ""
            selectedRole = user.role
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("ویرایش پروفایل") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "بازگشت")
                    }
                },
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
                .padding(16.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Profile Header
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = "پروفایل",
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = currentUser?.displayName ?: "کاربر",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            text = currentUser?.email ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }

            // Display Name
            OutlinedTextField(
                value = displayName,
                onValueChange = { displayName = it },
                label = { Text("نام نمایشی") },
                leadingIcon = {
                    Icon(Icons.Default.Person, contentDescription = "نام")
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = displayName.isEmpty()
            )

            // Age
            OutlinedTextField(
                value = age,
                onValueChange = { age = it },
                label = { Text("سن") },
                leadingIcon = {
                    Icon(Icons.Default.Cake, contentDescription = "سن")
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Gender
            var genderExpanded by remember { mutableStateOf(false) }
            val genders = listOf("مرد", "زن", "سایر")

            ExposedDropdownMenuBox(
                expanded = genderExpanded,
                onExpandedChange = { genderExpanded = !genderExpanded }
            ) {
                OutlinedTextField(
                    value = gender,
                    onValueChange = {},
                    label = { Text("جنسیت") },
                    leadingIcon = {
                        Icon(Icons.Default.Person, contentDescription = "جنسیت")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    readOnly = true,
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = genderExpanded)
                    }
                )

                ExposedDropdownMenu(
                    expanded = genderExpanded,
                    onDismissRequest = { genderExpanded = false }
                ) {
                    genders.forEach { genderOption ->
                        DropdownMenuItem(
                            text = { Text(genderOption) },
                            onClick = {
                                gender = genderOption
                                genderExpanded = false
                            }
                        )
                    }
                }
            }

            // Role Selection
            Text(
                text = "نقش کاربری",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 8.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                RoleChip(
                    role = "athlete",
                    selected = selectedRole == "athlete",
                    onSelected = { selectedRole = "athlete" },
                    text = "ورزشکار"
                )

                RoleChip(
                    role = "coach",
                    selected = selectedRole == "coach",
                    onSelected = { selectedRole = "coach" },
                    text = "مربی"
                )
            }

            // Save Button
            Button(
                onClick = {
                    val updatedData = mutableMapOf<String, Any>()

                    if (displayName.isNotEmpty() && displayName != currentUser?.displayName) {
                        updatedData["displayName"] = displayName
                    }

                    if (age.isNotEmpty()) {
                        updatedData["age"] = age.toIntOrNull() ?: 0
                    }

                    if (gender.isNotEmpty()) {
                        updatedData["gender"] = gender
                    }

                    if (selectedRole != currentUser?.role) {
                        updatedData["role"] = selectedRole
                    }

                    if (updatedData.isNotEmpty()) {
                        authViewModel.updateUserProfile(updatedData)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = displayName.isNotEmpty() && profileState != com.gymway.auth.ProfileUiState.Loading
            ) {
                if (profileState == com.gymway.auth.ProfileUiState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("در حال ذخیره...")
                } else {
                    Icon(Icons.Default.Save, contentDescription = "ذخیره")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("ذخیره تغییرات")
                }
            }

            // Show error message
            if (profileState is com.gymway.auth.ProfileUiState.Error) {
                val errorMessage = (profileState as com.gymway.auth.ProfileUiState.Error).message
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(8.dp)
                )
            }

            // Show success message
            if (profileState == com.gymway.auth.ProfileUiState.Success) {
                Text(
                    text = "پروفایل با موفقیت بروزرسانی شد",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
    }
}

@Composable
fun RoleChip(
    role: String,
    selected: Boolean,
    onSelected: (String) -> Unit,
    text: String
) {
    FilterChip(
        selected = selected,
        onClick = { onSelected(role) },
        label = { Text(text) },
        leadingIcon = if (selected) {
            {
                Icon(
                    Icons.Default.Check,
                    contentDescription = "Selected",
                    modifier = Modifier.size(20.dp)
                )
            }
        } else null
    )
}