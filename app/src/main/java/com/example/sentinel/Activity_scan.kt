package com.example.sentinel

import android.annotation.SuppressLint
import android.content.Intent
import android.net.wifi.WifiManager
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.*
import java.io.IOException
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.jvm.java


class ScanActivity : AppCompatActivity() {

    private lateinit var rootLayout: View  // <-- ADD THIS

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

        rootLayout = findViewById(R.id.rootlayout)
        progressBar = findViewById(R.id.scan_progress)
        logOutput = findViewById(R.id.log_output)
        scanStatus = findViewById(R.id.scan_status)
        stopButton = findViewById(R.id.btn_stop_scan)
        returnButton = findViewById(R.id.btn_return_main)
        logScroll = findViewById(R.id.log_scroll)

        stopButton.setOnClickListener { isScanning = false }
        returnButton.setOnClickListener { finish() }

        startNetworkScan()

        val scanButton: Button = findViewById(R.id.btn_restart_scan)
        scanButton.setOnClickListener {
            val intent = Intent(this, ScanActivity::class.java)
            startActivity(intent)

            Toast.makeText(this, "Restarted Scanning network for cameras...", Toast.LENGTH_SHORT).show()


        }
    }

    private suspend fun detectCamera(ip: String): String? {
        val rtspPorts = listOf(554, 8554)
        val httpPorts = listOf( 8080, 8000, 8787)

        val rtspPaths = listOf(
            "rtsp://$ip:8000/stream",
            "rtsp://$ip:554/live",
            "rtsp://$ip:554/live/ch0",
            "rtsp://$ip:554/h264",
            "rtsp://$ip:554/av0_0",
            "rtsp://$ip:8554/video"
        )

        val mjpegPaths = listOf(
            "http://$ip:8000/video",
            "http://$ip:8000/stream",
            "http://$ip:8000/mjpeg",
            "http://$ip:8000/cam.mjpeg",
            "http://$ip/capture",
            "http://$ip/mjpeg/1"
        )

        // 1. Check RTSP ports
        for (port in rtspPorts) {
            if (isPortOpen(ip, port)) {
                for (url in rtspPaths) {
                    if (isStream(url)) return "🎥 RTSP Stream → $url"
                }
            }
        }

        // 2. Check MJPEG / HTTP camera ports

        for (port in httpPorts) {
            if (isPortOpen(ip, port)) {
                for (path in mjpegPaths) {
                    val url = "http://$ip:$port$path"
                    if (isStream(url)) {
                        runOnUiThread {
                            AlertDialog.Builder(this@ScanActivity)
                                .setTitle("Camera Found")
                                .setMessage("📷 MJPEG Stream → $url")
                                .setPositiveButton("OPEN") { _, _ ->
                                    // Open in MainActivity
                                    val intent = Intent(this@ScanActivity, MainActivity::class.java)
                                    intent.putExtra("STREAM_URL", url)
                                    startActivity(intent)
                                }
                                .setNegativeButton("CANCEL", null)
                                .show()
                        }
                        return "📷 MJPEG Stream → $url"
                    }
                }
                val serverUrl = "http://$ip:$port/stream/index.m3u8"
                runOnUiThread {
                    AlertDialog.Builder(this@ScanActivity)
                        .setTitle("Ip Camera Found")
                        .setMessage("📡 Camera Streaming → $serverUrl")
                        .setPositiveButton("OPEN") { _, _ ->
                            val intent = Intent(this@ScanActivity, MainActivity::class.java)
                            intent.putExtra("STREAM_URL", serverUrl)
                            startActivity(intent)
                        }
                        .setNegativeButton("CANCEL", null)
                        .show()
                }
                return "📡 Camera Streaming → $serverUrl"
            }
        }



        return null
    }

    private fun isPortOpen(ip: String, port: Int, timeout: Int = 500): Boolean {
        return try {
            Socket().use { socket ->
                socket.connect(InetSocketAddress(ip, port), timeout)
                true
            }
        } catch (_: Exception) {
            false
        }
    }

    private fun isStream(url: String): Boolean {
        return try {
            val connection = URL(url).openConnection()
            connection.connectTimeout = 800
            connection.readTimeout = 800
            connection.getInputStream().use { stream ->
                val buffer = ByteArray(256)
                val read = stream.read(buffer)
                read > 0
            }
        } catch (_: Exception) {
            false
        }
    }




    // Checks for video streaming on RTSP (554), HTTP MJPEG (8080, 80)
    private fun checkVideoStream(ip: String): String? {
        val ports = listOf(8080, 554, 80)

        for (port in ports) {
            try {
                val socket = Socket()
                socket.connect(InetSocketAddress(ip, port), 800)
                socket.close()

                return when (port) {
                    554 -> "🎥 RTSP stream detected at rtsp://$ip:554/stream/index.m3u8"
                    8080 -> "📷 MJPEG stream at http://$ip:8000/stream/index.m3u8"
                    80 -> "📷 HTTP server at http://$ip:80/stream/index.m3u8"
                    else -> null
                }
            } catch (_: Exception) {
                // Port closed, ignore
            }
        }

        return null
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
                    if (isHostAlive(ip)) {
                        withContext(Dispatchers.Main) {
                            appendLog("✅ Active: $ip — checking stream...")
                        }

                        val result = detectCamera(ip)

                        withContext(Dispatchers.Main) {
                            if (result != null)
                                appendLog("🎥 $result")
                            else
                                appendLog("❌ No camera stream found on $ip")
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

    private fun CoroutineScope.isHostAlive(ip: String): Boolean {
        return try {
            // Run the command on the I/O dispatcherwithContext(Dispatchers.IO) {
            val command = "ping -c 1 -W 1 $ip" // -c 1: one packet, -W 1: 1-second timeout
            val process = Runtime.getRuntime().exec(command)
            val exitCode = process.waitFor()
            exitCode == 0
        }
     catch (e: IOException) {
        // Could be a security exception or other issue
        false
    } catch (e: InterruptedException) {
        // The waiting thread was interrupted
        false
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
