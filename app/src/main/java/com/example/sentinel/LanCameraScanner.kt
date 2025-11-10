package com.example.sentinel
// File: LanCameraScanner.kt
import android.content.Context
import android.net.wifi.WifiManager
import kotlinx.coroutines.*
import kotlinx.coroutines.selects.select
import kotlinx.coroutines.sync.Semaphore
import java.io.IOException
import java.net.*
import kotlin.math.min

data class DiscoveredCamera(val ip: String, val port: Int, val rtspUrl: String? = null)

/**
 * Get local IPv4 address on Wi-Fi (e.g., 192.168.1.45)
 */
fun getLocalIpAddress(context: Context): String? {
    val wm = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
    val ipInt = wm.connectionInfo.ipAddress
    if (ipInt == 0) return null
    // convert little-endian int to dotted IP
    return ((ipInt and 0xff).toString() + "." +
            ((ipInt shr 8) and 0xff) + "." +
            ((ipInt shr 16) and 0xff) + "." +
            ((ipInt shr 24) and 0xff))
}

/**
 * Generate /24 range from a base ip "192.168.1.45" -> 192.168.1.1..254
 */
fun generate24Range(localIp: String): List<String> {
    val parts = localIp.split(".")
    if (parts.size != 4) return emptyList()
    val base = "${parts[0]}.${parts[1]}.${parts[2]}"
    val ips = mutableListOf<String>()
    for (i in 1..254) {
        ips.add("$base.$i")
    }
    // optionally move localIp to front
    ips.remove(localIp)
    ips.add(0, localIp)
    return ips
}

/**
 * Quick TCP port probe (non-blocking with timeout)
 */
suspend fun isPortOpen(ip: String, port: Int, timeoutMs: Int = 300): Boolean =
    withContext(Dispatchers.IO) {
        try {
            Socket().use { s ->
                val adr = InetSocketAddress(ip, port)
                s.connect(adr, timeoutMs)
                true
            }
        } catch (e: Exception) {
            false
        }
    }

/**
 * Probe a list of RTSP path templates for a given ip:port.
 * This only tries to open a TCP connection to ip:port (we already know port open),
 * and optionally tries to send a minimal RTSP OPTIONS request to detect response.
 *
 * Returns first working RTSP URL or null.
 */
suspend fun probeRtspPaths(ip: String, port: Int = 554, timeoutMs: Int = 800): String? =
    withContext(Dispatchers.IO) {
        val pathCandidates = listOf(
            "/", "/live.sdp", "/live", "/h264", "/stream", "/video", "/1", "/axis-media/media.amp",
            "/cam/realmonitor?channel=1&subtype=0"
        )
        for (p in pathCandidates) {
            val url = "rtsp://$ip:$port$p"
            try {
                // open socket and send OPTIONS (RTSP) to test if server responds
                Socket().use { sock ->
                    sock.soTimeout = timeoutMs
                    sock.connect(InetSocketAddress(ip, port), timeoutMs)
                    val out = sock.getOutputStream()
                    val `in` = sock.getInputStream()

                    // send RTSP OPTIONS request (not required but many RTSP servers respond)
                    val cseq = 1
                    val req = "OPTIONS $url RTSP/1.0\r\nCSeq: $cseq\r\n\r\n"
                    out.write(req.toByteArray())
                    out.flush()

                    // read small response (non-blocking read with timeout)
                    val buf = ByteArray(512)
                    val read = `in`.read(buf)
                    if (read > 0) {
                        val resp = String(buf, 0, read)
                        if (resp.startsWith("RTSP") || resp.contains("Public:") || resp.contains("CSeq:")) {
                            return@withContext url
                        }
                    }
                }
            } catch (e: Exception) {
                // ignore and continue
            }
        }
        null
    }

/**
 * Main scanning coroutine launcher. Reports discoveries via callback.
 *
 * - concurrency: number of parallel worker coroutines
 * - stopAfterFirstRtsp: optional quick mode
 */
fun scanLanForCameras(
    context: Context,
    concurrency: Int = 50,
    onFound: (DiscoveredCamera) -> Unit,
    onComplete: () -> Unit,
    stopAfterFirstRtsp: Boolean = false
) {
    val localIp = getLocalIpAddress(context) ?: run {
        onComplete()
        return
    }
    val ips = generate24Range(localIp)
    val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    scope.launch {
        val sem = Semaphore(concurrency)
        val foundRtsp = CompletableDeferred<Boolean>()

        ips.forEach { ip ->
            sem.acquire()
            launch {
                try {
                    // quick check: port 554
                    val hasRtspPort = isPortOpen(ip, 554, timeoutMs = 250)
                    if (hasRtspPort) {
                        // probe RTSP path templates
                        val rtsp = probeRtspPaths(ip, 554, timeoutMs = 800)
                        onFound(DiscoveredCamera(ip, 554, rtsp))
                        if (rtsp != null && stopAfterFirstRtsp) foundRtsp.complete(true)
                    } else {
                        // also check HTTP (80/8080) for camera web UI (many cameras have RTSP but port closed)
                        val hasHttp = isPortOpen(ip, 80, timeoutMs = 200) || isPortOpen(ip, 8080, timeoutMs = 200)
                        if (hasHttp) {
                            onFound(DiscoveredCamera(ip, if (isPortOpen(ip,80)) 80 else 8080, null))
                        }
                    }
                } finally {
                    sem.release()
                }
            }
        }

        // wait for completion or early stop
        // if stopping early on first found RTSP:
        if (stopAfterFirstRtsp) {
            // wait until one found or all tasks done
            select<Unit> {
                foundRtsp.onAwait { /*one found*/ }
                scope.coroutineContext[Job]!!.children.forEach { child ->
                    child.onJoin { /*no-op*/ }
                }
            }
        } else {
            // wait for all children to finish
            scope.coroutineContext[Job]?.children?.forEach { it.join() }
        }

        onComplete()
    }
}
