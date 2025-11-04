import os
import subprocess
from flask import Flask, send_from_directory

# === CONFIGURATION ===
HLS_DIR = "stream"
PORT = 8000
CAMERA_NAME = "HP TrueVision HD Camera"  # Change if your webcam has a different name

# === FLASK APP ===
app = Flask(__name__)

@app.route('/stream/<path:filename>')
def stream_files(filename):
    """Serve the HLS video files (.m3u8 and .ts segments)."""
    return send_from_directory(HLS_DIR, filename)

@app.route('/')
def index():
    """Simple index page."""
    return f"HLS stream running. Open: http://<your_ip>:{PORT}/stream/index.m3u8"

# === MAIN EXECUTION ===
if __name__ == '__main__':
    # Ensure HLS folder exists
    os.makedirs(HLS_DIR, exist_ok=True)

    # Start FFmpeg process to generate HLS stream
    ffmpeg_cmd = [
        "ffmpeg", "-y",
        "-f", "dshow",
        "-i", f"video={CAMERA_NAME}",
        "-vcodec", "libx264", "-preset", "ultrafast", "-tune", "zerolatency",
        "-f", "hls",
        "-hls_time", "1", "-hls_list_size", "4", "-hls_flags", "delete_segments",
        os.path.join(HLS_DIR, "index.m3u8")
    ]

    print("Starting FFmpeg HLS stream...")
    subprocess.Popen(ffmpeg_cmd)

    print(f"Serving HLS on http://0.0.0.0:{PORT}/stream/index.m3u8")
    app.run(host="0.0.0.0", port=PORT, threaded=True)
