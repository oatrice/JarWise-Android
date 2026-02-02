package com.oatrice.jarwise.ui.login

import android.app.Activity
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.androidx.compose.koinViewModel

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    viewModel: LoginViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Activity Result Launcher for Google Sign-In
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            viewModel.handleSignInResult(result.data)
        } else {
             Log.d("LoginScreen", "Sign in cancelled or failed result code: ${result.resultCode}")
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
        onLoginSuccess = onLoginSuccess
    )
}

@Composable
fun LoginContent(
    uiState: LoginUiState,
    onSignInClick: () -> Unit,
    onLoginSuccess: () -> Unit = {} // Default empty for preview simplicity if needed, but logic handles it
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

                        Button(onClick = onSignInClick) {
                            Text(text = "Sign in with Google")
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
                        LaunchedEffectVerify(onLoginSuccess)
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
            onSignInClick = {}
        )
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun LoginScreenPreviewLoading() {
    MaterialTheme {
        LoginContent(
            uiState = LoginUiState.Loading,
            onSignInClick = {}
        )
    }
}

@Composable
fun LaunchedEffectVerify(onSuccess: () -> Unit) {
   Button(onClick = onSuccess) {
       Text("Continue")
   }
}
