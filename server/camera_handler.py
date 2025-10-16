import threading

camera_list = []

def detect_motion(cam_id, url):
    """
    Placeholder: Implement motion detection or camera streaming logic here.
    This function will run in a separate thread for each camera.
    """
    print(f"[{cam_id}] Camera started on URL: {url}")
    # TODO: Add your OpenCV or RTSP streaming logic here

def add_camera_url(url):
    camera_list.append(url)
    cam_id = f"CAM{len(camera_list)}"
    t = threading.Thread(target=detect_motion, args=(cam_id, url))
    t.daemon = True
    t.start()

def start_camera_threads(camera_urls):
    """
    Start threads for all camera URLs
    """
    for url in camera_urls:
        add_camera_url(url)
    print(f"✅ Started {len(camera_urls)} camera threads.")
