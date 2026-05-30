package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AlertType
import com.example.data.EmailAlert
import com.example.ui.components.*
import com.example.ui.theme.*
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

    FlowingGlassBackground {
        Scaffold(
            containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "INTEL ANALYSIS", 
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
                actions = {
                    if (alert != null) {
                        IconButton(onClick = { viewModel.deleteAlert(alert); onBack() }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
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
                    DetailRow(label = "Raw Body Snippet", value = alert.rawEmailBody.take(300) + "...")
                }
            }
        }
    }
}
}

@Composable
fun RegistrationDetails(alert: EmailAlert) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f))
            .border(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Icon(
            imageVector = Icons.Filled.Info, 
            contentDescription = null, 
            tint = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.size(28.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                "DRIVE REGISTRATION DETECTED", 
                fontWeight = FontWeight.ExtraBold, 
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.secondary,
                letterSpacing = 1.sp
            )
            Text(
                "VERIFIED CDC SOURCE PIEPELINE", 
                fontWeight = FontWeight.Bold, 
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }

    Spacer(modifier = Modifier.height(4.dp))

    DetailRow(label = "Company Profile", value = alert.companyName)
    if (alert.category.isNotBlank()) {
        DetailRow(label = "Recruitment Tier", value = alert.category.uppercase(), valueColor = MaterialTheme.colorScheme.secondary)
    }
    
    DetailRow(
        label = "Professional Eligibility", 
        value = if(alert.isEligible) "Integrated M.Tech (CSE/IT/SE) • ELIGIBLE" else "INELIGIBLE FOR PROFILE",
        valueColor = if(alert.isEligible) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
    )
    
    if (alert.stipend.isNotBlank()) DetailRow(label = "Base Stipend offered", value = alert.stipend)
    if (alert.ctc.isNotBlank()) DetailRow(label = "Offered Package (CTC)", value = alert.ctc)
    if (alert.deadline.isNotBlank()) DetailRow(label = "Absolute Registration Deadline", value = alert.deadline, valueColor = MaterialTheme.colorScheme.secondary)
    if (alert.link.isNotBlank()) DetailRow(label = "Direct Application Link", value = alert.link, valueColor = MaterialTheme.colorScheme.primary)
    
    Spacer(modifier = Modifier.height(8.dp))
    Text(
        text = "• Be cautious regarding registration deadlines. Late submissions are completely rejected by the placement portal. Copy and complete application steps carefully.",
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
fun ShortlistDetails(alert: EmailAlert, viewModel: PlacementViewModel) {
    val isGenerating by viewModel.isGeneratingStudyPlan.collectAsState()
    val isArfathIncluded = alert.status == "YOU ARE IN THE LIST"

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isArfathIncluded) MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f)
                else MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
            )
            .border(
                1.dp, 
                if (isArfathIncluded) MaterialTheme.colorScheme.tertiary.copy(alpha = 0.4f)
                else MaterialTheme.colorScheme.error.copy(alpha = 0.3f), 
                RoundedCornerShape(12.dp)
            )
            .padding(16.dp)
    ) {
        Icon(
            imageVector = if (isArfathIncluded) Icons.Filled.Star else Icons.Filled.Warning, 
            contentDescription = null, 
            tint = if (isArfathIncluded) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error,
            modifier = Modifier.size(32.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                if (isArfathIncluded) "OFFICIAL MATCH DETECTED • SHORLISTED" else "MATCH NOT DETECTED", 
                fontWeight = FontWeight.ExtraBold, 
                style = MaterialTheme.typography.titleSmall,
                color = if (isArfathIncluded) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error,
                letterSpacing = 1.sp
            )
            Text(
                if (isArfathIncluded) "MOHAMMED ARFATH R (22MIS0479)" else "YOUR NAME IS NOT EXPLICITLY FOUND", 
                fontWeight = FontWeight.Bold, 
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    Spacer(modifier = Modifier.height(4.dp))

    DetailRow(label = "Company Profile", value = alert.companyName)
    
    if (alert.batchmates.isNotBlank()) {
        DetailRow(label = "Identified Batchmates (22MIS0XXX)", value = alert.batchmates, valueColor = MaterialTheme.colorScheme.primary)
    }

    Spacer(modifier = Modifier.height(12.dp))
    HorizontalDivider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
    Spacer(modifier = Modifier.height(12.dp))

    if (alert.studyPlan.isNotBlank()) {
        Text(
            text = "PREPARATION INTELLIGENCE TERMINAL", 
            style = MaterialTheme.typography.labelMedium, 
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.tertiary,
            letterSpacing = 1.5.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        
        LiquidGlassCard(
            modifier = Modifier.fillMaxWidth(),
            accentColor = MaterialTheme.colorScheme.tertiary,
            shape = RoundedCornerShape(16.dp),
            borderWidth = 1.dp,
            glowOpacity = 0.25f
        ) {
            Text(
                text = alert.studyPlan,
                fontFamily = FontFamily.Monospace,
                fontSize = 13.sp,
                color = Color(0xFF00FF66), // matrix terminal green accent
                lineHeight = 18.sp
            )
        }
    } else {
        LiquidGlassButton(
            text = "GENERATE PREPARATION RUNBOOK",
            onClick = { viewModel.generateStudyPlan(alert) },
            accentColor = MaterialTheme.colorScheme.tertiary,
            textColor = Color.White,
            enabled = !isGenerating,
            isLoading = isGenerating
        )
    }
}

@Composable
fun EventDetails(alert: EmailAlert) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Icon(
            imageVector = Icons.Filled.Check, 
            contentDescription = null, 
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(28.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                "COMPANY SPONSORED EVENT", 
                fontWeight = FontWeight.ExtraBold, 
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.sp
            )
            Text(
                "PRE-PLACEMENT TALK (PPT) OR WEBINAR", 
                fontWeight = FontWeight.Bold, 
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    Spacer(modifier = Modifier.height(4.dp))

    DetailRow(label = "Company Profile", value = alert.companyName)
    if (alert.eventDateTime.isNotBlank()) {
        DetailRow(label = "Schedule Date and Time", value = alert.eventDateTime, valueColor = MaterialTheme.colorScheme.primary)
    }
    if (alert.link.isNotBlank()) {
        DetailRow(label = "Event Webcast Access Link", value = alert.link, valueColor = MaterialTheme.colorScheme.secondary)
    }
}

@Composable
fun DetailRow(
    label: String, 
    value: String, 
    valueColor: Color = Color.White
) {
    LiquidGlassCard(
        modifier = Modifier.fillMaxWidth(),
        accentColor = NeonCyan,
        shape = RoundedCornerShape(16.dp),
        borderWidth = 1.dp,
        glowOpacity = 0.12f
    ) {
        Column {
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = NeonCyan.copy(alpha = 0.85f),
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.2.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = valueColor,
                fontWeight = FontWeight.SemiBold,
                lineHeight = 20.sp
            )
        }
    }
}

