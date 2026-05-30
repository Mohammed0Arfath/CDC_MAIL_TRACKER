package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.PlacementViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyzeEmailScreen(
    viewModel: PlacementViewModel,
    onBack: () -> Unit
) {
    var emailText by remember { mutableStateOf("") }
    val isAnalyzing by viewModel.isAnalyzing.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Analyze New Email") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = emailText,
                onValueChange = { emailText = it },
                label = { Text("Paste CDC Email Content") },
                modifier = Modifier.fillMaxWidth().weight(1f),
                placeholder = { Text("Paste the email here...") }
            )
            
            Button(
                onClick = {
                    if (emailText.isNotBlank()) {
                        viewModel.analyzeAndSaveEmail(emailText) { success ->
                            if (success) {
                                onBack()
                            } else {
                                // show error (needs coroutine scope)
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = !isAnalyzing && emailText.isNotBlank()
            ) {
                if (isAnalyzing) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("Analyze Email")
                }
            }
        }
    }
}
