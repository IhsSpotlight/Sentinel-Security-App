import cv2
import requests
from config import TELEGRAM_BOT_TOKEN, CHAT_ID

def send_telegram_alert(message, image_path=None):
    url = f"https://api.telegram.org/bot{TELEGRAM_BOT_TOKEN}/sendMessage"
    requests.post(url, data={"chat_id": CHAT_ID, "text": message})

    if image_path:
        photo_url = f"https://api.telegram.org/bot{TELEGRAM_BOT_TOKEN}/sendPhoto"
        with open(image_path, "rb") as photo:
            requests.post(photo_url, data={"chat_id": CHAT_ID}, files={"photo": photo})

def detect_motion(camera_url, camera_name):
    cap = cv2.VideoCapture(camera_url)
    ret, frame1 = cap.read()
    ret, frame2 = cap.read()

    while ret:
        diff = cv2.absdiff(frame1, frame2)
        gray = cv2.cvtColor(diff, cv2.COLOR_BGR2GRAY)
        blur = cv2.GaussianBlur(gray, (5, 5), 0)
        _, thresh = cv2.threshold(blur, 20, 255, cv2.THRESH_BINARY)
        dilated = cv2.dilate(thresh, None, iterations=3)
        contours, _ = cv2.findContours(dilated, cv2.RETR_TREE, cv2.CHAIN_APPROX_SIMPLE)

        for contour in contours:
            if cv2.contourArea(contour) > 15000:  # Sensitivity
                snapshot = f"snap_{camera_name}.jpg"
                cv2.imwrite(snapshot, frame1)
                send_telegram_alert(f"🚨 Motion Detected at {camera_name}!", snapshot)
                break

        frame1 = frame2
        ret, frame2 = cap.read()

    cap.release()
