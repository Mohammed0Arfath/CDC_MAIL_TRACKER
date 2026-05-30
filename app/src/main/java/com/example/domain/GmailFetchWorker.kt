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

            // Fetch emails
            val response = gmail.users().messages().list("me")
                .setQ("from:vitianscdc2027@vitstudent.ac.in")
                .setMaxResults(10) // fetch latest 10
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
}
