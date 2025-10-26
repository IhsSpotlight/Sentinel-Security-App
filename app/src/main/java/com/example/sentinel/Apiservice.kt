package com.example.sentinel

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

data class Alert(
    val id: Int,
    val timestamp: String,
    val image_url: String,
    val cameraid: String
)

data class CameraRequest(
    val camera_url: String
)

interface ApiService {
    @GET("alerts")
    fun getAlerts(): Call<List<Alert>>

    @POST("add_camera")
    fun addCamera(@Body cameraRequest: CameraRequest): Call<Void>
}
