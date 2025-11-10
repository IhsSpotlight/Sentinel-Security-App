import requests

BOT_TOKEN = "7243044057:AAGKFRBsU8W-WTRE4wykCq2mC 2oAhcNsMx"
CHAT_ID = "5881176040"

def send_telegram_alert(message, image_path=None):
    url = f"https://api.telegram.org/bot{BOT_TOKEN}/sendMessage"
    data = {"chat_id": CHAT_ID, "text": message}
    requests.post(url, data=data)

    if image_path:
        photo_url = f"https://api.telegram.org/bot{BOT_TOKEN}/sendPhoto"
        with open(image_path, "rb") as photo:
            requests.post(photo_url, data={"chat_id": CHAT_ID}, files={"photo": photo})
