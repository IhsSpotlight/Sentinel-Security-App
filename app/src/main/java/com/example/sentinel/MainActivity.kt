package com.example.sentinel

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


class MainActivity : AppCompatActivity() {
    private lateinit var alertAdapter: AlertAdapter
    private var exoPlayer: ExoPlayer? = null
    private lateinit var playerView: PlayerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        playerView = findViewById(R.id.player_view)

        // ✅ Setup RecyclerView for alerts
        val recyclerView: RecyclerView = findViewById(R.id.recycler_cameras)
        recyclerView.layoutManager = LinearLayoutManager(this)

        alertAdapter = AlertAdapter { alert ->
            // This code runs when an alert is clicked
            playVideoStream(alert.image_url)
        }
        recyclerView.adapter = alertAdapter

        // ✅ Fetch alerts from server (this part is kept to show existing items)
        fetchAlerts()

        // ✅ "Add Camera" button now acts as a "Play Stream" button
        val addButton: FloatingActionButton = findViewById(R.id.fab_add_camera)
        addButton.setOnClickListener {
            showAddCameraDialog()
        }
    }

    // ✨ ---- METHODS FOR VIDEO PLAYBACK ---- ✨

    private fun initializePlayer() {
        exoPlayer = ExoPlayer.Builder(this).build()
        playerView.player = exoPlayer
    }

    private fun playVideoStream(videoUrl: String) {
        if (exoPlayer == null) {
            initializePlayer()
        }

        // Make the player visible
        playerView.visibility = View.VISIBLE

        // Create a media item and start playback
        val mediaItem = MediaItem.fromUri(videoUrl)
        exoPlayer?.setMediaItem(mediaItem)
        exoPlayer?.prepare()
        exoPlayer?.play()

        Toast.makeText(this, "Streaming from: $videoUrl", Toast.LENGTH_LONG).show()
    }
//http://192.168.68.131:8000/stream/index.m3u8
    private fun releasePlayer() {
        exoPlayer?.release()
        exoPlayer = null
        playerView.visibility = View.GONE
    }

    override fun onStop() {
        super.onStop()
        releasePlayer()
    }

    // 🔸 Fetch existing alerts from the server
    private fun fetchAlerts() {
        RetrofitClient.instance.getAlerts().enqueue(object : Callback<List<Alert>> {
            override fun onResponse(call: Call<List<Alert>>, response: Response<List<Alert>>) {
                if (response.isSuccessful) {
                    alertAdapter.setData(response.body() ?: emptyList())
                } else {
                    Toast.makeText(this@MainActivity, "Failed to load alerts", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Alert>>, t: Throwable) {
                t.printStackTrace()
                Toast.makeText(this@MainActivity, "Connection error", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // 🔸 Show dialog to add a new camera
    private fun showAddCameraDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Play Video Stream") // Changed title for clarity

        val input = EditText(this)
        input.hint = "Enter camera URL (RTSP or HTTP)"
        builder.setView(input)

        builder.setPositiveButton("Play") { dialog, _ -> // Changed button text
            val url = input.text.toString().trim()
            if (url.isNotEmpty()) {
                // ✨ Directly play the video stream instead of sending to server
                playVideoStream(url)
            } else {
                Toast.makeText(this, "URL cannot be empty", Toast.LENGTH_SHORT).show()
            }
            dialog.dismiss()
        }

        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }

        builder.show()
    }

    // ⛔️ The function to send the camera to the server has been removed.
    // private fun addCameraToServer(url: String) { ... }
}
