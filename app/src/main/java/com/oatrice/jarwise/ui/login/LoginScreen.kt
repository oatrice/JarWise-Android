package com.oatrice.jarwise.ui.login

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

                        Button(onClick = { viewModel.onSignInClick() }) {
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
                    // Start effect to navigate away, or show success UI briefly
                    // In a real app, we might trigger a LaunchedEffect here calling onLoginSuccess()
                    // based on state change.
                    LaunchedEffectVerify(onLoginSuccess)
                    Text("Welcome back, ${state.user.name}!")
                }
            }
        }
    }
}

// Separate function to act as a side-effect trigger (stub for now)
@Composable
fun LaunchedEffectVerify(onSuccess: () -> Unit) {
   // In real app: LaunchedEffect(Unit) { onSuccess() }
   // keeping it simple for compilation check
   Button(onClick = onSuccess) {
       Text("Continue")
   }
}
