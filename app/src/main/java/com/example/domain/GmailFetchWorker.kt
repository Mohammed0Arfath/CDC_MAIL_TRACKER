package com.example.domain

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.data.AppDatabase
import com.example.data.EmailAlert
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.gmail.Gmail
import com.google.api.services.gmail.GmailScopes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GmailFetchWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val account = GoogleSignIn.getLastSignedInAccount(context)
            if (account == null) {
                Log.e("GmailFetchWorker", "Not signed in")
                return@withContext Result.failure()
            }

            val credential = GoogleAccountCredential.usingOAuth2(
                context,
                listOf(GmailScopes.GMAIL_READONLY)
            )
            credential.selectedAccount = account.account

            val gmail = Gmail.Builder(
                NetHttpTransport(),
                GsonFactory.getDefaultInstance(),
                credential
            )
                .setApplicationName("VIT Placement Assistant")
                .build()

            // Fetch emails - search comprehensive CDC addresses or general placement keywords
            val query = "from:(vitianscdc2027@vitstudent.ac.in OR vitianscdc@vitstudent.ac.in OR vitianscdc2026@vitstudent.ac.in OR cdc@vitstudent.ac.in OR cdc@vit.ac.in) OR subject:(CDC OR Placement OR Internship OR Shortlist OR \"Next Round\" OR \"Online Test\")"
            val response = gmail.users().messages().list("me")
                .setQ(query)
                .setMaxResults(15) // fetch latest 15 to be more comprehensive and real-time
                .execute()

            val messages = response.messages
            if (messages != null && messages.isNotEmpty()) {
                val emailAnalyzer = EmailAnalyzer()
                val dao = AppDatabase.getDatabase(context).emailAlertDao()
                
                for (messageMeta in messages) {
                    val message = gmail.users().messages().get("me", messageMeta.id).setFormat("full").execute()
                    var bodyText = ""

                    // Extract body from payload
                    val payload = message.payload
                    if (payload != null) {
                        bodyText = extractText(payload)
                        val attachmentsText = extractAttachmentsText(gmail, message.id, payload)
                        if (attachmentsText.isNotBlank()) {
                            bodyText += "\n\n=== ATTACHMENTS SECTION ===\n$attachmentsText"
                        }
                    } else {
                        message.snippet?.let { bodyText = it }
                    }

                    if (bodyText.isNotBlank()) {
                        val alert = emailAnalyzer.analyzeEmail(bodyText)
                        if (alert != null) {
                            val id = dao.insertAlert(alert)
                            showNotification(context, alert.copy(id = id.toInt()))
                        }
                    }
                }
            }
            Result.success()
        } catch (e: Exception) {
            Log.e("GmailFetchWorker", "Error fetching emails", e)
            Result.retry()
        }
    }

    private fun showNotification(context: Context, alert: EmailAlert) {
        val channelId = "placement_alerts"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Placement Alerts",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(context, com.example.MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("New Alert: ${alert.companyName}")
            .setContentText("${alert.type.name} - ${if (alert.isEligible) "Eligible" else "Not Eligible"}")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            notificationManager.notify(alert.id, notification)
        } catch (e: SecurityException) {
            // Permission denied
        }
    }

    private fun extractText(payload: com.google.api.services.gmail.model.MessagePart): String {
        return try {
            val parts = payload.parts
            var text = ""
            if (parts != null) {
                for (part in parts) {
                    text += extractText(part)
                }
            } else if (payload.body?.data != null) {
                val data = payload.body.data
                text = String(android.util.Base64.decode(data, android.util.Base64.URL_SAFE))
            }
            text
        } catch (e: Exception) {
            ""
        }
    }

    private fun extractAttachmentsText(
        gmail: Gmail,
        messageId: String,
        part: com.google.api.services.gmail.model.MessagePart
    ): String {
        return try {
            var text = ""
            val parts = part.parts
            if (parts != null) {
                for (p in parts) {
                    text += extractAttachmentsText(gmail, messageId, p)
                }
            }
            
            val filename = part.filename
            val attachmentId = part.body?.attachmentId
            if (!filename.isNullOrBlank() && !attachmentId.isNullOrBlank()) {
                text += "\n--- ATTACHMENT FILENAME: $filename ---\n"
                
                val isTextual = filename.endsWith(".csv", ignoreCase = true) ||
                        filename.endsWith(".txt", ignoreCase = true) ||
                        filename.endsWith(".tsv", ignoreCase = true) ||
                        filename.endsWith(".json", ignoreCase = true) ||
                        part.mimeType?.contains("text", ignoreCase = true) == true
                
                if (isTextual) {
                    try {
                        val attachment = gmail.users().messages().attachments()
                            .get("me", messageId, attachmentId)
                            .execute()
                        val data = attachment.data
                        if (!data.isNullOrBlank()) {
                            val decodedBytes = android.util.Base64.decode(data, android.util.Base64.URL_SAFE)
                            text += "Attachment Content:\n${String(decodedBytes, Charsets.UTF_8)}\n"
                        }
                    } catch (e: Exception) {
                        Log.e("GmailFetchWorker", "Error fetching attachment: $filename", e)
                    }
                }
            }
            text
        } catch (e: Exception) {
            ""
        }
    }
}
