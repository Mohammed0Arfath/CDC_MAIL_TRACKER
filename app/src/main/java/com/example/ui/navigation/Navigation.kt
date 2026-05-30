package com.example.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
object Dashboard

@Serializable
object AnalyzeEmail

@Serializable
data class AlertDetail(val alertId: Int)
