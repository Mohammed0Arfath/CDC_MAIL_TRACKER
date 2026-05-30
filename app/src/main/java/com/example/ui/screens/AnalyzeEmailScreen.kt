package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.PlacementViewModel
import com.example.ui.components.*
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyzeEmailScreen(
    viewModel: PlacementViewModel,
    onBack: () -> Unit
) {
    var emailText by remember { mutableStateOf("") }
    val isAnalyzing by viewModel.isAnalyzing.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    
    FlowingGlassBackground {
        Scaffold(
            containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "INTEL PARSER", 
                        fontWeight = FontWeight.ExtraBold, 
                        letterSpacing = 1.5.sp,
                        style = MaterialTheme.typography.titleMedium
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
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
            Text(
                text = "CYBERNETIC EMAIL EXTRACTION",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.5.sp
            )
            
            Text(
                text = "Paste raw email body here. Either from Gmail, Outlook or copy-pasted text. The secure intelligence compiler will extract company name, registration deadlines, CTC package details, and cross-reference your name (Mohammed Arfath R) for shortlists in real-time.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
                value = emailText,
                onValueChange = { emailText = it },
                label = { Text("CDC EMAIL DATA FEED", fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp) },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp)),
                placeholder = { 
                    Text(
                        "Paste raw copy-pasted email body text from vitianscdc...\n" +
                        "e.g., 'Dear Students, Registration is open for Microsoft. CTC is 44 LPA. Deadline is 31st May...'",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    ) 
                },
                textStyle = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace, color = Color.White),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color(0x2B01010A),
                    unfocusedContainerColor = Color(0x1201010A),
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    unfocusedLabelColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                    cursorColor = MaterialTheme.colorScheme.primary
                )
            )
            
            LiquidGlassButton(
                text = "COMPILE & INDEX INTEL",
                onClick = {
                    if (emailText.isNotBlank()) {
                        viewModel.analyzeAndSaveEmail(emailText) { success ->
                            if (success) {
                                onBack()
                            } else {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Extraction unsuccessful. Please verify email data format.")
                                }
                            }
                        }
                    }
                },
                accentColor = MaterialTheme.colorScheme.primary,
                textColor = Color.White,
                enabled = !isAnalyzing && emailText.isNotBlank(),
                isLoading = isAnalyzing
            )
        }
    }
}
}

