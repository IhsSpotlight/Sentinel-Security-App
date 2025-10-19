from flask import Flask, request, jsonify, send_from_directory
from camera_handler import start_camera
from ai_detection import detect_motion_and_face
import sqlite3
import os
from datetime import datetime

app = Flask(__name__)
UPLOAD_FOLDER = 'static/snapshots'
os.makedirs(UPLOAD_FOLDER, exist_ok=True)

# Database connection
def get_db():
    conn = sqlite3.connect('database.db')
    conn.row_factory = sqlite3.Row
    return conn

@app.route('/api/alert', methods=['GET'])
def get_alerts():
    conn = get_db()
    cursor = conn.cursor()
    cursor.execute("SELECT * FROM alerts ORDER BY timestamp DESC")
    rows = cursor.fetchall()
    alerts = [dict(row) for row in rows]
    conn.close()
    return jsonify(alerts)

@app.route('/api/upload', methods=['POST'])
def upload_alert():
    data = request.get_json()
    alert_type = data.get('type', 'Unknown')
    img_path = data.get('image_path', '')
    ts = datetime.now().strftime('%Y-%m-%d %H:%M:%S')

    conn = get_db()
    conn.execute("INSERT INTO alerts (type, image_path, timestamp) VALUES (?, ?, ?)",
                 (alert_type, img_path, ts))
    conn.commit()
    conn.close()

    return jsonify({'status': 'success'}), 200

@app.route('/snapshots/<filename>')
def get_snapshot(filename):
    return send_from_directory(UPLOAD_FOLDER, filename)

if __name__ == '__main__':
    # Start camera streaming in background
    start_camera()
    app.run(host='0.0.0.0', port=5000, debug=True)
