import cv2
import numpy as np
from face_recognition import detect_faces
from telegram_bot import send_telegram_alert

def detect_motion(rtsp_url, camera_name):
    cap = cv2.VideoCapture(rtsp_url)
    if not cap.isOpened():
        print(f"[ERROR] Cannot open stream: {camera_name}")
        return

    ret, frame1 = cap.read()
    ret, frame2 = cap.read()

    while ret:
        diff = cv2.absdiff(frame1, frame2)
        gray = cv2.cvtColor(diff, cv2.COLOR_BGR2GRAY)
        blur = cv2.GaussianBlur(gray, (5, 5), 0)
        _, thresh = cv2.threshold(blur, 25, 255, cv2.THRESH_BINARY)
        dilated = cv2.dilate(thresh, None, iterations=2)
        contours, _ = cv2.findContours(dilated, cv2.RETR_TREE, cv2.CHAIN_APPROX_SIMPLE)

        for contour in contours:
            if cv2.contourArea(contour) < 5000:
                continue
            send_telegram_alert(f"🚨 Motion detected on {camera_name}")
            detect_faces(frame1, camera_name)
            break

        frame1 = frame2
        ret, frame2 = cap.read()
