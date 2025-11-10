package com.example.sentinel

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

// Data class to hold alert information
data class Alert(
    val id: Int,
    val timestamp: String,
    val image_url: String,
    val cameraid: String,
    val status: String
)

// Request body for adding a camera manually or automatically
data class CameraRequest(
    val cameraId: String,
    val ipAddress: String,
    val port: Int,
    val streamUrl: String
)

// Optional: Telegram notification request body (for later)
data class TelegramRequest(
    val message: String
)

interface ApiService {

    // Fetch all stored alerts from the backend
    @GET("alerts")
    fun getAlerts(): Call<List<Alert>>

    // Add a new camera (manual or discovered)
    @POST("add_camera")
    fun addCamera(@Body cameraRequest: CameraRequest): Call<Void>

    // (Future) Send notification to Telegram bot
    @POST("send_telegram")
    fun sendTelegramMessage(@Body telegramRequest: TelegramRequest): Call<Void>
}
