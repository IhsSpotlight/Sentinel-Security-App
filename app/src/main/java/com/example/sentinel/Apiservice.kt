package com.sentinel.security

import retrofit2.Call
import retrofit2.http.GET

data class Alert(
    val id: Int,
    val timestamp: String,
    val image_url: String,
    val camera_id: String
)

interface ApiService {
    @GET("api/alerts")
    fun getAlerts(): Call<List<Alert>>
}
