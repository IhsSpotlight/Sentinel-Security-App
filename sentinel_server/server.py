from flask import Flask, jsonify
import threading
from motion_detector import detect_motion
from config import CAMERAS

app = Flask(__name__)

@app.route('/')
def index():
    return jsonify({"status": "Sentinel AI Server Running"})

@app.route('/start', methods=['GET'])
def start_detection():
    for name, url in CAMERAS.items():
        threading.Thread(target=detect_motion, args=(url, name)).start()
    return jsonify({"message": "Detection started on all cameras"})

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000)
