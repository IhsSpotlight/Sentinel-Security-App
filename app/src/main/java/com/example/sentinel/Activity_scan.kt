package com.example.sentinel

import android.annotation.SuppressLint
import android.net.wifi.WifiManager
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
    private lateinit var logScroll: ScrollView

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
        logScroll = findViewById(R.id.log_scroll)

        stopButton.setOnClickListener { isScanning = false }
        returnButton.setOnClickListener { finish() }

        startNetworkScan()
    }

    @SuppressLint("DefaultLocale")
    private fun getDeviceBaseIP(): String {
        val wifiManager = applicationContext.getSystemService(WIFI_SERVICE) as WifiManager
        val ip = wifiManager.connectionInfo.ipAddress

        val ipString = String.format(
            "%d.%d.%d.",
            ip and 0xff,
            ip shr 8 and 0xff,
            ip shr 16 and 0xff
        )

        return ipString
    }


    @SuppressLint("SetTextI18n")
    private fun startNetworkScan() {
        val baseIp = getDeviceBaseIP() //"192.168.68." // You can make this dynamic later

        appendLog("Found Base Ip: $baseIp")
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
                            //appendLog("→ Checking RTSP (554) and HTTP (8080)...")
                        }
                    }
                } catch (_: Exception) { }
            }

            withContext(Dispatchers.Main) {
                appendLog("✅ Scan complete.")
                scanStatus.text = "Scan Finished"
                Toast.makeText(this@ScanActivity, "Scan complete!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun appendLog(message: String) {
        logOutput.append("\n$message")
        logScroll.post {
            logScroll.fullScroll(ScrollView.FOCUS_DOWN)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        isScanning = false
        mainScope.cancel()
    }
}
