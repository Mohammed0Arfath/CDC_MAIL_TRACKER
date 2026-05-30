package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.widget.Toast
import com.example.data.AppDatabase
import com.example.data.PlacementRepository
import com.example.ui.PlacementViewModel
import com.example.ui.PlacementViewModelFactory
import com.example.ui.navigation.AlertDetail
import com.example.ui.navigation.AnalyzeEmail
import com.example.ui.navigation.Dashboard
import com.example.ui.screens.AlertDetailScreen
import com.example.ui.screens.AnalyzeEmailScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.theme.MyApplicationTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.api.services.gmail.GmailScopes

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        val context = LocalContext.current
        val repository = remember {
            PlacementRepository(AppDatabase.getDatabase(context).emailAlertDao())
        }
        val viewModel: PlacementViewModel = viewModel(factory = PlacementViewModelFactory(repository))
        
        PlacementApp(viewModel)
      }
    }
  }
}

@Composable
fun PlacementApp(viewModel: PlacementViewModel) {
    val navController = rememberNavController()
    val allAlerts by viewModel.allAlerts.collectAsState()
    val context = LocalContext.current

    val signInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                task.getResult(com.google.android.gms.common.api.ApiException::class.java)
                viewModel.startBackgroundSync(context)
                Toast.makeText(context, "Gmail auto-sync started!", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Sign-in failed. Please verify Client ID.", Toast.LENGTH_LONG).show()
            }
        }
    }

    NavHost(navController = navController, startDestination = Dashboard) {
        composable<Dashboard> {
            DashboardScreen(
                alerts = allAlerts,
                onNavigateToAnalyze = { navController.navigate(AnalyzeEmail) },
                onNavigateToDetail = { id -> navController.navigate(AlertDetail(id)) },
                onSyncClick = {
                    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestEmail()
                        .requestScopes(Scope(GmailScopes.GMAIL_READONLY))
                        .build()
                    val signInClient = GoogleSignIn.getClient(context, gso)
                    signInLauncher.launch(signInClient.signInIntent)
                }
            )
        }
        composable<AnalyzeEmail> {
            AnalyzeEmailScreen(
                viewModel = viewModel,
                onBack = { navController.navigateUp() }
            )
        }
        composable<AlertDetail> { backStackEntry ->
            val detail = backStackEntry.toRoute<AlertDetail>()
            AlertDetailScreen(
                alertId = detail.alertId,
                viewModel = viewModel,
                onBack = { navController.navigateUp() }
            )
        }
    }
}
