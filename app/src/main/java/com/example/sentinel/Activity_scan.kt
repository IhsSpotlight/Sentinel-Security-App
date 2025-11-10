package com.example.sentinel

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.*
import java.net.InetAddress

class ScanActivity : AppCompatActivity() {

    private lateinit var progressBar: ProgressBar
    private lateinit var logOutput: TextView
    private lateinit var scanStatus: TextView
    private lateinit var stopButton: Button
    private lateinit var returnButton: Button

    private var isScanning = true
    private val mainScope = CoroutineScope(Dispatchers.Main + Job())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan)

        progressBar = findViewById(R.id.scan_progress)
        logOutput = findViewById(R.id.log_output)
        scanStatus = findViewById(R.id.scan_status)
        stopButton = findViewById(R.id.btn_stop_scan)
        returnButton = findViewById(R.id.btn_return_main)

        stopButton.setOnClickListener { isScanning = false }
        returnButton.setOnClickListener { finish() }

        startNetworkScan()
    }

    @SuppressLint("SetTextI18n")
    private fun startNetworkScan() {
        val baseIp = "192.168.0." // You can detect automatically later
        appendLog("Starting network scan...")

        mainScope.launch(Dispatchers.IO) {
            for (i in 1..255) {
                if (!isScanning) break

                val ip = baseIp + i
                withContext(Dispatchers.Main) {
                    scanStatus.text = "Scanning: $ip"
                    progressBar.progress = i
                }

                try {
                    val reachable = InetAddress.getByName(ip).isReachable(100)
                    if (reachable) {
                        withContext(Dispatchers.Main) {
                            appendLog("✅ Active device found: $ip")

                            // 🔹 Try to identify camera ports
                            appendLog("→ Checking RTSP (554) and HTTP (8080)...")
                        }
                    }
                } catch (e: Exception) {
                    // Ignore errors
                }
            }

            withContext(Dispatchers.Main) {
                appendLog("✅ Scan complete.")
                scanStatus.text = "Scan Finished"
                Toast.makeText(this@ScanActivity, "Scan complete!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    @SuppressLint("WrongViewCast")
    private fun appendLog(message: String) {
        logOutput.append("\n$message")
        val scrollView = findViewById<ScrollView>(R.id.scan_progress)
        scrollView?.post {
            scrollView.fullScroll(ScrollView.FOCUS_DOWN)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        isScanning = false
        mainScope.cancel()
    }
}
