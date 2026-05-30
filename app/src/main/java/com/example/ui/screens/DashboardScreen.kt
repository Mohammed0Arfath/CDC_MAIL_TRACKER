package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import com.example.data.AlertType
import com.example.data.EmailAlert

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    alerts: List<EmailAlert>,
    onNavigateToAnalyze: () -> Unit,
    onNavigateToDetail: (Int) -> Unit,
    onSyncClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("VIT Placement", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                actions = {
                    IconButton(onClick = onSyncClick) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Auto Fetch Emails")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToAnalyze) {
                Icon(Icons.Filled.Add, contentDescription = "Simulate New Email")
            }
        }
    ) { padding ->
        if (alerts.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.Notifications, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.outline)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("No alerts yet. Tap + to analyze an email.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(alerts) { alert ->
                    AlertCard(alert = alert, onClick = { onNavigateToDetail(alert.id) })
                }
            }
        }
    }
}

@Composable
fun AlertCard(alert: EmailAlert, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = getCardColorForType(alert.type)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = getIconForType(alert.type),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = alert.type.name,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = alert.companyName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            if (alert.category.isNotEmpty()) {
                Text(text = "Category: ${alert.category}", style = MaterialTheme.typography.bodyMedium)
            }
            if (!alert.isEligible) {
                Text(
                    text = "Not Eligible", 
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

@Composable
fun getIconForType(type: AlertType) = when(type) {
    AlertType.REGISTRATION -> Icons.Filled.Notifications
    AlertType.SHORTLIST -> Icons.Filled.CheckCircle
    AlertType.EVENT -> Icons.Filled.Info
    AlertType.UNKNOWN -> Icons.Filled.Warning
}

@Composable
fun getCardColorForType(type: AlertType) = when(type) {
    AlertType.REGISTRATION -> MaterialTheme.colorScheme.secondaryContainer
    AlertType.SHORTLIST -> MaterialTheme.colorScheme.tertiaryContainer
    AlertType.EVENT -> MaterialTheme.colorScheme.primaryContainer
    AlertType.UNKNOWN -> MaterialTheme.colorScheme.surfaceVariant
}

