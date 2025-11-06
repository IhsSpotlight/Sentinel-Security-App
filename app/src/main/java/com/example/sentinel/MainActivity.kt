package com.example.sentinel

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import androidx.core.content.edit

class MainActivity : AppCompatActivity() {
    private lateinit var alertAdapter: AlertAdapter
    private var exoPlayer: ExoPlayer? = null
    private lateinit var playerView: PlayerView

    private lateinit var sharedPreferences: SharedPreferences
    private val savedCameras = mutableListOf<Alert>()
    private val PREFS_NAME = "SentinelPrefs"
    private val KEY_CAMERA_URLS = "camera_urls"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        playerView = findViewById(R.id.player_view)

        val recyclerView: RecyclerView = findViewById(R.id.recycler_cameras)
        recyclerView.layoutManager = LinearLayoutManager(this)

        alertAdapter = AlertAdapter ( onViewClick = { alert ->
            playVideoStream(alert.image_url)
        },  onEditClick = { alert ->
            showEditCameraNameDialog(alert)
        }
        )
        recyclerView.adapter = alertAdapter

        loadAndDisplayCameras()
        loadAndDisplayCameraname()

        // FAB

        val addButton: FloatingActionButton = findViewById(R.id.fab_add_camera)
        addButton.setOnClickListener {
            showAddCameraDialog()
        }
    }

    private fun initializePlayer() {
        exoPlayer = ExoPlayer.Builder(this).build()
        playerView.player = exoPlayer
    }

    private fun playVideoStream(videoUrl: String) {
        if (exoPlayer == null) {
            initializePlayer()
        }

        playerView.visibility = View.VISIBLE

        val mediaItem = MediaItem.fromUri(videoUrl)
        exoPlayer?.setMediaItem(mediaItem)
        exoPlayer?.prepare()
        exoPlayer?.play()

        Toast.makeText(this, "Streaming from: $videoUrl", Toast.LENGTH_LONG).show()

        saveUrl(videoUrl)
    }
    private fun saveCameraNames() {
        val cameraData = savedCameras.joinToString("|") {
            "${it.image_url}::${it.cameraName}"
        }
        sharedPreferences.edit {
            putString("camera_data", cameraData)
        }
    }

    private fun loadAndDisplayCameraname() {
        val cameraData = sharedPreferences.getString("camera_data", "") ?: ""
        savedCameras.clear()

        if (cameraData.isNotEmpty()) {
            cameraData.split("|").forEachIndexed { index, entry ->
                val parts = entry.split("::")
                val url = parts.getOrNull(0) ?: ""
                val name = parts.getOrNull(1) ?: "Unnamed Camera"
                savedCameras.add(Alert(id = index, timestamp = "Saved", image_url = url, cameraid = "Local", cameraName = name))
            }
        }

        alertAdapter.setData(savedCameras)
        fetchAlertsFromServer()
    }

    private fun showEditCameraNameDialog(alert: Alert) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Edit Camera Name")

        val input = EditText(this)

        input.setText(alert.cameraName)
        input.hint="Enter camera name"
        builder.setView(input)

        builder.setPositiveButton("Save") { dialog, _ ->
            val newName = input.text.toString().trim()
            if (newName.isNotEmpty()) {
                alert.cameraName = newName
                saveCameraNames()   // ✅ Save all camera names in SharedPreferences
                alertAdapter.setData(savedCameras)
            } else {
                Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show()
            }
            dialog.dismiss()
        }

        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }

        builder.show()
    }

    private fun saveUrl(url: String) {
        val currentUrls = sharedPreferences.getStringSet(KEY_CAMERA_URLS, mutableSetOf())?.toMutableSet() ?: mutableSetOf()

        if (currentUrls.add(url)) {
            sharedPreferences.edit { putStringSet(KEY_CAMERA_URLS, currentUrls) }

            // ✨ FIXED: Changed 'camera_id' to 'cameraid'
            val newAlert = Alert(id = savedCameras.size + 1, timestamp = "Saved", image_url = url, cameraid = "Local")
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
            // ✨ FIXED: Changed 'camera_id' to 'cameraid'
            savedCameras.add(Alert(id = index, timestamp = "Saved", image_url = url, cameraid = "Local"))
        }
        alertAdapter.setData(savedCameras)

        fetchAlertsFromServer()
    }

    private fun fetchAlertsFromServer() {
        RetrofitClient.instance.getAlerts().enqueue(object : Callback<List<Alert>> {
            override fun onResponse(call: Call<List<Alert>>, response: Response<List<Alert>>) {
                if (response.isSuccessful) {
                    val serverAlerts = response.body() ?: emptyList()
                    // You can merge server alerts with local ones here if needed
                } else {
                    Toast.makeText(this@MainActivity, "Failed to load alerts from server", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Alert>>, t: Throwable) {
                // Fail silently if server is offline
            }
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
            if (url.isNotEmpty()) {
                playVideoStream(url)
            } else {
                Toast.makeText(this, "URL cannot be empty", Toast.LENGTH_SHORT).show()
            }
            dialog.dismiss()
        }

        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }

        builder.show()
    }
}
