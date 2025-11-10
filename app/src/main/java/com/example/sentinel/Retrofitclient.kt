package com.example.sentinel

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.HttpURLConnection
import java.net.URL

object RetrofitClient {

    // 🧩 Local & fallback server URLs
    private const val LOCAL_BASE_URL = "http://10.0.2.2:5000/"
    private const val REMOTE_BASE_URL = "https://sentinel-server.example.com/" // optional fallback

    // Auto-detect server availability
    private val BASE_URL: String by lazy {
        if (isServerAvailable(LOCAL_BASE_URL)) LOCAL_BASE_URL else REMOTE_BASE_URL
    }

    val instance: ApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        retrofit.create(ApiService::class.java)
    }

    /**
     * ✅ Checks if local Flask server is running
     */
    private fun isServerAvailable(baseUrl: String): Boolean {
        return try {
            val url = URL(baseUrl)
            val conn = url.openConnection() as HttpURLConnection
            conn.connectTimeout = 1000
            conn.requestMethod = "GET"
            conn.connect()
            val available = conn.responseCode in 200..399
            conn.disconnect()
            available
        } catch (e: Exception) {
            false
        }
    }
}
