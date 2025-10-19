import cv2
import threading

CAMERA_URL = "rtsp://your_camera_url"
cap = None

def camera_loop():
    global cap
    cap = cv2.VideoCapture(CAMERA_URL)
    while True:
        ret, frame = cap.read()
        if not ret:
            continue
        # Future: save frame or process

def start_camera():
    t = threading.Thread(target=camera_loop, daemon=True)
    t.start()
