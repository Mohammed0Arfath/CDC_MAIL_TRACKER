package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface EmailAlertDao {
    @Query("SELECT * FROM email_alerts ORDER BY timestamp DESC")
    fun getAllAlerts(): Flow<List<EmailAlert>>

    @Query("SELECT * FROM email_alerts WHERE id = :id LIMIT 1")
    fun getAlertById(id: Int): Flow<EmailAlert?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlert(alert: EmailAlert): Long

    @Update
    suspend fun updateAlert(alert: EmailAlert)

    @Query("DELETE FROM email_alerts WHERE id = :id")
    suspend fun deleteAlertById(id: Int)
}
