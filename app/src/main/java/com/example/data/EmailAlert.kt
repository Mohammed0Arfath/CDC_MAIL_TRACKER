package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

enum class AlertType {
    REGISTRATION,
    SHORTLIST,
    EVENT,
    UNKNOWN
}

@Serializable
@Entity(tableName = "email_alerts")
data class EmailAlert(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val type: AlertType,
    val companyName: String,
    val category: String, // Dream / Super Dream / Regular
    val status: String, // E.g., "YOU ARE IN THE LIST" or "Not found", or "Open"
    val stipend: String,
    val ctc: String,
    val location: String,
    val deadline: String,
    val link: String,
    val eventDateTime: String,
    val isEligible: Boolean,
    val rawEmailBody: String,
    val batchmates: String, // Comma-separated or JSON list of 22MIS0XXX
    val studyPlan: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
