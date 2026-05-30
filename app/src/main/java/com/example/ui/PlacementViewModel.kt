package com.example.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.ExistingPeriodicWorkPolicy
import com.example.data.EmailAlert
import com.example.data.PlacementRepository
import com.example.domain.EmailAnalyzer
import com.example.domain.GmailFetchWorker
import com.example.domain.StudyPlanGenerator
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PlacementViewModel(
    private val repository: PlacementRepository,
    private val emailAnalyzer: EmailAnalyzer = EmailAnalyzer(),
    private val studyPlanGenerator: StudyPlanGenerator = StudyPlanGenerator()
) : ViewModel() {

    val allAlerts: StateFlow<List<EmailAlert>> = repository.allAlerts.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _isAnalyzing = MutableStateFlow(false)
    val isAnalyzing = _isAnalyzing.asStateFlow()

    private val _isGeneratingStudyPlan = MutableStateFlow(false)
    val isGeneratingStudyPlan = _isGeneratingStudyPlan.asStateFlow()

    fun analyzeAndSaveEmail(rawText: String, onDone: (Boolean) -> Unit) {
        viewModelScope.launch {
            _isAnalyzing.value = true
            val alert = emailAnalyzer.analyzeEmail(rawText)
            if (alert != null) {
                repository.insert(alert)
                onDone(true)
            } else {
                onDone(false)
            }
            _isAnalyzing.value = false
        }
    }

    fun generateStudyPlan(alert: EmailAlert) {
        if (alert.studyPlan.isNotEmpty()) return 
        viewModelScope.launch {
            _isGeneratingStudyPlan.value = true
            val plan = studyPlanGenerator.generateStudyPlan(alert.companyName)
            val updatedAlert = alert.copy(studyPlan = plan)
            repository.update(updatedAlert)
            _isGeneratingStudyPlan.value = false
        }
    }

    fun deleteAlert(alert: EmailAlert) {
        viewModelScope.launch {
            repository.deleteById(alert.id)
        }
    }

    fun startBackgroundSync(context: Context) {
        val syncWorkRequest = PeriodicWorkRequestBuilder<GmailFetchWorker>(15, TimeUnit.MINUTES)
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "GmailSyncWorker",
            ExistingPeriodicWorkPolicy.KEEP,
            syncWorkRequest
        )
    }
}
