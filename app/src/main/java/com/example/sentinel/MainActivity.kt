package com.sentinel.security

import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {
    private lateinit var alertAdapter: AlertAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // ✅ Setup RecyclerView for alerts
        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        alertAdapter = AlertAdapter()
        recyclerView.adapter = alertAdapter

        // ✅ Fetch alerts from server
        fetchAlerts()

        // ✅ Add Camera Button setup
        val addButton: FloatingActionButton = findViewById(R.id.addCameraBtn)
        addButton.setOnClickListener {
            showAddCameraDialog()
        }
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
        builder.setTitle("Add New Camera")

        val input = EditText(this)
        input.hint = "Enter camera URL (RTSP or HTTP)"
        builder.setView(input)

        builder.setPositiveButton("Add") { dialog, _ ->
            val url = input.text.toString().trim()
            if (url.isNotEmpty()) {
                addCameraToServer(url)
            } else {
                Toast.makeText(this, "URL cannot be empty", Toast.LENGTH_SHORT).show()
            }
            dialog.dismiss()
        }

        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }

        builder.show()
    }

    // 🔸 Send camera URL to the Python server
    private fun addCameraToServer(url: String) {
        RetrofitClient.instance.addCamera(CameraRequest(url)).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@MainActivity, "Camera added successfully ✅", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@MainActivity, "Failed to add camera ❌", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                t.printStackTrace()
                Toast.makeText(this@MainActivity, "Connection error", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
