import cv2
from motion_detector import send_telegram_alert

def detect_faces(frame, camera_name):
    gray = cv2.cvtColor(frame, cv2.COLOR_BGR2GRAY)
    face_cascade = cv2.CascadeClassifier(cv2.data.haarcascades + "haarcascade_frontalface_default.xml")
    faces = face_cascade.detectMultiScale(gray, 1.3, 5)

    if len(faces) == 0:
        return

    for (x, y, w, h) in faces:
        cv2.rectangle(frame, (x, y), (x+w, y+h), (255, 0, 0), 2)
        snapshot = f"unknown_{camera_name}.jpg"
        cv2.imwrite(snapshot, frame)
        send_telegram_alert(f"⚠️ Unknown face detected at {camera_name}", snapshot)
        break
