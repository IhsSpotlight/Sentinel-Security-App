package com.example.sentinel

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import java.net.InetAddress
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : AppCompatActivity() {
    private lateinit var alertAdapter: AlertAdapter
    private var exoPlayer: ExoPlayer? = null
    private lateinit var playerView: PlayerView
    private lateinit var sharedPreferences: SharedPreferences
    private val savedCameras = mutableListOf<Alert>()
    private val PREFS_NAME = "SentinelPrefs"
    private val KEY_CAMERA_URLS = "camera_urls"

    private val telegramBotToken = "YOUR_TELEGRAM_BOT_TOKEN"
    private val telegramChatId = "YOUR_CHAT_ID"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        playerView = findViewById(R.id.player_view)

        val recyclerView: RecyclerView = findViewById(R.id.recycler_cameras)
        recyclerView.layoutManager = LinearLayoutManager(this)

        alertAdapter = AlertAdapter { alert ->
            playVideoStream(alert.image_url)
        }
        recyclerView.adapter = alertAdapter

        loadAndDisplayCameras()

        val addButton: FloatingActionButton = findViewById(R.id.fab_add_camera)
        addButton.setOnClickListener {
            showAddCameraDialog()
        }

        // 🔍 Automatically scan for IP cameras when app starts
        // 🔍 Scan Cameras button
        val scanButton: Button = findViewById(R.id.btn_scan_cameras)
        scanButton.setOnClickListener {
            val intent = Intent(this, ScanActivity::class.java)
            startActivity(intent)

            Toast.makeText(this, "Scanning network for cameras...", Toast.LENGTH_SHORT).show()

            CoroutineScope(Dispatchers.IO).launch {
                autoDiscoverCameras()
            }
        }

    }

    private fun initializePlayer() {
        exoPlayer = ExoPlayer.Builder(this).build()
        playerView.player = exoPlayer
    }

    private fun playVideoStream(videoUrl: String) {
        if (exoPlayer == null) initializePlayer()

        playerView.visibility = View.VISIBLE
        val mediaItem = MediaItem.fromUri(videoUrl)
        exoPlayer?.setMediaItem(mediaItem)
        exoPlayer?.prepare()
        exoPlayer?.play()

        Toast.makeText(this, "Streaming from: $videoUrl", Toast.LENGTH_LONG).show()
        saveUrl(videoUrl)
    }

    private fun saveUrl(url: String) {
        val currentUrls = sharedPreferences.getStringSet(KEY_CAMERA_URLS, mutableSetOf())?.toMutableSet() ?: mutableSetOf()

        if (currentUrls.add(url)) {
            sharedPreferences.edit { putStringSet(KEY_CAMERA_URLS, currentUrls) }
            val newAlert = Alert(savedCameras.size + 1, "Saved", url, "Local")
            savedCameras.add(newAlert)
            alertAdapter.setData(savedCameras)
        }
    }

    private fun releasePlayer() {
        exoPlayer?.release()
        exoPlayer = null
        playerView.visibility = View.GONE
    }

    override fun onStop() {
        super.onStop()
        releasePlayer()
    }

    private fun loadAndDisplayCameras() {
        val savedUrls = sharedPreferences.getStringSet(KEY_CAMERA_URLS, emptySet()) ?: emptySet()
        savedCameras.clear()
        savedUrls.forEachIndexed { index, url ->
            savedCameras.add(Alert(index, "Saved", url, "Local"))
        }
        alertAdapter.setData(savedCameras)

        fetchAlertsFromServer()
    }

    private fun fetchAlertsFromServer() {
        RetrofitClient.instance.getAlerts().enqueue(object : Callback<List<Alert>> {
            override fun onResponse(call: Call<List<Alert>>, response: Response<List<Alert>>) {
                if (response.isSuccessful) {
                    val serverAlerts = response.body() ?: emptyList()
                } else {
                    Toast.makeText(this@MainActivity, "Failed to load alerts from server", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Alert>>, t: Throwable) {}
        })
    }

    private fun showAddCameraDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Play Video Stream")

        val input = EditText(this)
        input.hint = "Enter camera URL (RTSP or HTTP)"
        builder.setView(input)

        builder.setPositiveButton("Play") { dialog, _ ->
            val url = input.text.toString().trim()
            if (url.isNotEmpty()) playVideoStream(url)
            else Toast.makeText(this, "URL cannot be empty", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
        builder.show()
    }

    //  Auto-discover IP cameras on the same network
    private suspend fun autoDiscoverCameras() {
        val subnet = "192.168.0." // Change if your network uses different subnet
        val foundCameras = mutableListOf<String>()

        withContext(Dispatchers.IO) {
            for (i in 1..255) {
                val host = subnet + i // 'host' contains the IP address
                try {
                    val reachable = InetAddress.getByName(host).isReachable(200)
                    if (reachable && checkForCamera(host)) {
                        val rtspUrl = "rtsp://$host:554/stream"
                        foundCameras.add(rtspUrl)
                        saveUrl(rtspUrl)

                        // FIX: Pass both the URL and the host (IP) to the function
                        sendToBackend(rtspUrl, host)

                        sendTelegramNotification("📷 New camera found: $rtspUrl")
                    }
                } catch (_: IOException) { }
            }
        }

        if (foundCameras.isNotEmpty()) {
            withContext(Dispatchers.Main) {
                Toast.makeText(this@MainActivity, "Found ${foundCameras.size} cameras", Toast.LENGTH_LONG).show()
            }
        }
    }

    // Simple function to detect camera-like devices by checking RTSP port
    private fun checkForCamera(ip: String): Boolean {
        return try {
            val url = URL("http://$ip:8080") // Some IP cameras host HTTP server
            val connection = url.openConnection() as HttpURLConnection
            connection.connectTimeout = 200
            connection.requestMethod = "GET"
            val responseCode = connection.responseCode
            connection.disconnect()
            responseCode == 200
        } catch (_: Exception) {
            false
        }
    }

    // ☁️ Send camera info to backend
    private fun sendToBackend(url: String,ip: String) {
        val cameraRequest = CameraRequest(url, ip)
        RetrofitClient.instance.addCamera(cameraRequest).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {}
            override fun onFailure(call: Call<Void>, t: Throwable) {}
        })
    }

    // 🤖 Send Telegram alert
    private fun sendTelegramNotification(message: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val urlString = "https://api.telegram.org/bot$telegramBotToken/sendMessage?chat_id=$telegramChatId&text=${message}"
                val url = URL(urlString)
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "GET"
                conn.inputStream.bufferedReader().use { it.readText() }
                conn.disconnect()
            } catch (_: Exception) {}
        }
    }
}
