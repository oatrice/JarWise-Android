package com.oatrice.jarwise.ui.login

import android.app.Activity
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import org.koin.androidx.compose.koinViewModel

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    viewModel: LoginViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.resetState()
    }

    // Activity Result Launcher for Google Sign-In
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            viewModel.handleSignInResult(result.data)
        } else {
             Log.d("LoginScreen", "Sign in cancelled or failed result code: ${result.resultCode}")
             viewModel.handleSignInCancelled()
        }
    }

    LoginContent(
        uiState = uiState,
        onSignInClick = {
            // Check if we are using Real or Mock service
            val intent = viewModel.getSignInIntent()
            if (intent.action != null || intent.component != null) {
                launcher.launch(intent)
            } else {
                // Fallback to mock logic
                viewModel.onSignInClick()
            }
        },
        onGuestLogin = { viewModel.onGuestLogin() },
        onRestoreConfirmed = { fileId -> viewModel.onRestoreConfirmed(fileId) },
        onRestoreCancelled = { user -> viewModel.onRestoreCancelled(user) },
        onLoginSuccess = onLoginSuccess
    )
}

@Composable
fun LoginContent(
    uiState: LoginUiState,
    onSignInClick: () -> Unit,
    onGuestLogin: () -> Unit,
    onRestoreConfirmed: (String) -> Unit = {},
    onRestoreCancelled: (com.oatrice.jarwise.data.auth.AuthUser) -> Unit = {},
    onLoginSuccess: () -> Unit = {}
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            when (val state = uiState) {
                is LoginUiState.Idle, is LoginUiState.Error -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Welcome to JarWise",
                            style = MaterialTheme.typography.headlineMedium
                        )
                        Spacer(modifier = Modifier.height(32.dp))
                        
                        if (state is LoginUiState.Error) {
                            Text(
                                text = state.message,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                        }

                        if (state !is LoginUiState.Loading) {
                            Button(
                                onClick = onSignInClick,
                                modifier = Modifier.fillMaxWidth(0.8f)
                            ) {
                                Text(text = "Sign in with Google")
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            androidx.compose.material3.TextButton(
                                onClick = onGuestLogin
                            ) {
                                Text("Continue as Guest", color = MaterialTheme.colorScheme.secondary)
                            }
                        } else {
                            CircularProgressIndicator(modifier = Modifier.size(48.dp))
                        }
                    }
                }
                is LoginUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.size(48.dp)
                    )
                }
                is LoginUiState.Success -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(24.dp, Alignment.CenterVertically)
                    ) {
                        Text("Welcome back, ${state.user.name}!")
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        LaunchedEffect(Unit) {
                            delay(1000)
                            onLoginSuccess()
                        }
                    }
                }
                is LoginUiState.RestoreAvailable -> {
                     androidx.compose.material3.AlertDialog(
                        onDismissRequest = { /* No dismiss, must choose */ },
                        title = { Text("Backup Found") },
                        text = { 
                            Column {
                                Text("We found a backup from ${state.backupDate}.")
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Do you want to restore your data? Current data will be replaced.")
                            }
                        },
                        confirmButton = {
                            Button(onClick = { onRestoreConfirmed(state.fileId) }) {
                                Text("Restore")
                            }
                        },
                        dismissButton = {
                            androidx.compose.material3.TextButton(onClick = { onRestoreCancelled(state.user) }) {
                                Text("Start Fresh")
                            }
                        }
                    )
                }
                is LoginUiState.RestoreInProgress -> {
                     Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Restoring your data...")
                    }
                }
                is LoginUiState.RestoreSuccess -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        androidx.compose.material3.Icon(
                            imageVector = androidx.compose.material.icons.Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(48.dp)
                        )
                       
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Restore Successful!")
                        Text("Restarting app...", style = androidx.compose.material3.MaterialTheme.typography.bodySmall)
                        
                        val context = androidx.compose.ui.platform.LocalContext.current
                        LaunchedEffect(Unit) {
                            delay(2000)
                            // Trigger full app restart to reload Room DB
                            val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
                            intent?.addFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP)
                            intent?.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                            if (intent != null) {
                                context.startActivity(intent)
                                android.os.Process.killProcess(android.os.Process.myPid())
                            } else {
                                onLoginSuccess()
                            }
                        }
                    }
                }
            }
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun LoginScreenPreviewIdle() {
    MaterialTheme {
        LoginContent(
            uiState = LoginUiState.Idle,
            onSignInClick = {},
            onGuestLogin = {}
        )
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun LoginScreenPreviewLoading() {
    MaterialTheme {
        LoginContent(
            uiState = LoginUiState.Loading,
            onSignInClick = {},
            onGuestLogin = {}
        )
    }
}


