package com.example.data

import kotlinx.coroutines.flow.Flow

class PlacementRepository(private val dao: EmailAlertDao) {
    val allAlerts: Flow<List<EmailAlert>> = dao.getAllAlerts()

    fun getAlertById(id: Int): Flow<EmailAlert?> = dao.getAlertById(id)

    suspend fun insert(alert: EmailAlert) = dao.insertAlert(alert)
    
    suspend fun update(alert: EmailAlert) = dao.updateAlert(alert)

    suspend fun deleteById(id: Int) = dao.deleteAlertById(id)
}
