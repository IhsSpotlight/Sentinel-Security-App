import json
import threading
from motion_detector import detect_motion

def load_cameras():
    with open("cameras.json", "r") as f:
        return json.load(f)

def main():
    cameras = load_cameras()
    for cam in cameras:
        t = threading.Thread(target=detect_motion, args=(cam["url"], cam["name"]))
        t.start()

if __name__ == "__main__":
    main()
