package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.AlertType
import com.example.data.EmailAlert
import com.example.ui.PlacementViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertDetailScreen(
    alertId: Int,
    viewModel: PlacementViewModel,
    onBack: () -> Unit
) {
    val alerts by viewModel.allAlerts.collectAsState()
    val alert = alerts.find { it.id == alertId }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Alert Details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (alert != null) {
                        IconButton(onClick = { viewModel.deleteAlert(alert); onBack() }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Delete")
                        }
                    }
                }
            )
        }
    ) { padding ->
        if (alert == null) {
            Box(modifier = Modifier.padding(padding).fillMaxSize())
            return@Scaffold
        }
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when (alert.type) {
                AlertType.REGISTRATION -> RegistrationDetails(alert)
                AlertType.SHORTLIST -> ShortlistDetails(alert, viewModel)
                AlertType.EVENT -> EventDetails(alert)
                AlertType.UNKNOWN -> {
                    Text("Unknown or Irrelevant Email", color = MaterialTheme.colorScheme.error)
                    Text("Eligible: ${alert.isEligible}")
                }
            }
        }
    }
}

@Composable
fun RegistrationDetails(alert: EmailAlert) {
    Text("🚨 NEW REGISTRATION OPEN", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
    Spacer(modifier = Modifier.height(8.dp))
    Text("🏢 Company: ${alert.companyName}")
    Text("📋 Category: ${alert.category}")
    Text("🎓 Eligibility: ${if(alert.isEligible) "Integrated MTech ✅" else "Not Eligible ❌"}")
    if (alert.stipend.isNotBlank()) Text("💰 Stipend: ${alert.stipend}")
    if (alert.ctc.isNotBlank()) Text("💼 CTC: ${alert.ctc}")
    if (alert.location.isNotBlank()) Text("📍 Location: ${alert.location}")
    if (alert.deadline.isNotBlank()) Text("⏰ Deadline: ${alert.deadline}")
    if (alert.link.isNotBlank()) Text("🔗 Apply: ${alert.link}")
    Spacer(modifier = Modifier.height(16.dp))
    Text("⚡ Apply immediately!", fontWeight = FontWeight.Bold)
}

@Composable
fun ShortlistDetails(alert: EmailAlert, viewModel: PlacementViewModel) {
    val isGenerating by viewModel.isGeneratingStudyPlan.collectAsState()

    Text("📢 SHORTLIST / ONLINE TEST / NEXT ROUND", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
    Spacer(modifier = Modifier.height(8.dp))
    Text("🏢 Company: ${alert.companyName}")
    Spacer(modifier = Modifier.height(8.dp))
    
    if (alert.status == "YOU ARE IN THE LIST") {
        Text("🎯 YOU ARE IN THE LIST", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.tertiary)
        Text("✅ Name: Mohammed Arfath R")
        Text("✅ Reg no: 22MIS0479")
    } else {
        Text("❓ Your name/reg not found — check manually", color = MaterialTheme.colorScheme.error)
    }

    if (alert.batchmates.isNotBlank()) {
        Spacer(modifier = Modifier.height(8.dp))
        Text("👥 22MIS0 batchmates in list:")
        Text(alert.batchmates)
    }

    Spacer(modifier = Modifier.height(16.dp))
    Text("📬 Open your mail now!", fontWeight = FontWeight.Bold)

    Spacer(modifier = Modifier.height(16.dp))
    HorizontalDivider()
    Spacer(modifier = Modifier.height(16.dp))

    if (alert.studyPlan.isNotBlank()) {
        Text("📚 Study Plan & Questions", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(alert.studyPlan)
    } else {
        Button(
            onClick = { viewModel.generateStudyPlan(alert) },
            enabled = !isGenerating,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isGenerating) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
            } else {
                Text("Generate Study Plan")
            }
        }
    }
}

@Composable
fun EventDetails(alert: EmailAlert) {
    Text("📅 TECH TALK / EVENT", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
    Spacer(modifier = Modifier.height(8.dp))
    Text("🏢 Company: ${alert.companyName}")
    Text("📅 Date/Time: ${alert.eventDateTime}")
    Text("🔗 Registration: ${alert.link}")
}
