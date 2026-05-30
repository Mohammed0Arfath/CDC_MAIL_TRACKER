package com.example.domain

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.data.AppDatabase
import com.example.data.EmailAlert
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
                            dao.insertAlert(alert)
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
