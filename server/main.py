# server/main.py
from flask import Flask, jsonify, request, send_from_directory
from camera_handler import start_camera_threads
import sqlite3
import os

app = Flask(__name__)

# Start camera streaming threads
CAMERA_URLS = [
    "rtsp://your_camera1_url",
    "rtsp://your_camera2_url",
    "rtsp://your_camera3_url",
    "rtsp://your_camera4_url"
]
start_camera_threads(CAMERA_URLS)

DB = 'database.db'
os.makedirs("static/snapshots", exist_ok=True)

@app.route("/api/alerts", methods=["GET"])
def get_alerts():
    conn = sqlite3.connect(DB)
    cursor = conn.cursor()
    cursor.execute("SELECT id, timestamp, image_path, camera_id FROM alerts ORDER BY id DESC")
    data = [
        {"id": row[0], "timestamp": row[1], "image_url": f"http://{request.host}/{row[2]}", "camera_id": row[3]}
        for row in cursor.fetchall()
    ]
    conn.close()
    return jsonify(data)

@app.route("/static/snapshots/<path:filename>")
def serve_snapshots(filename):
    return send_from_directory("static/snapshots", filename)

if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5000, debug=True)
