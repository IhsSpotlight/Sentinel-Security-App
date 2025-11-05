# 🛡️ Sentinel — Smart IoT Home Surveillance System
A modern Android + IoT app for intelligent home security, camera streaming, and face recognition alerts.

---

## ✅ Core Progress Overview

| Feature Area | Description | Status |
|---------------|-------------|--------|
| **Android App Setup** | Kotlin app with login, Retrofit, camera integration | ✅ Done |
| **Backend Server** | Flask-based Python server with stream handling | ✅ Done |
| **UI Migration to Compose** | Moving layouts to Jetpack Compose | ⏳ In Progress |
| **Live Camera Streaming** | MJPEG/RTSP stream from IP cameras | ✅ Done |
| **Secure Server Connection** | HTTPS config with network_security_config.xml | ✅ Done |
| **Camera Add/Remove via Link** | User adds IP cameras manually | ⏳ In Progress |

---

## 🔐 Authentication & User Management
- [x] Basic login screen  
- [ ] Signup screen  
- [ ] Password reset / recovery  
- [ ] JWT or token-based authentication  
- [ ] Auto-login using stored token  
- [ ] Logout functionality  

---

## 📸 Camera & Surveillance
- [x] Add camera by link  
- [ ] Switch camera (front/back)  
- [ ] Snapshot & save locally  
- [ ] Start/stop camera streaming  
- [ ] Handle connection loss  
- [ ] Adjust stream quality (Low / Medium / High)  
- [ ] Background camera service  

---

## 🧠 Face Recognition & Alerts
- [ ] Integrate Face++ or local face recognition  
- [ ] Maintain known faces database  
- [ ] Detect and alert unknown persons  
- [ ] Send push notifications to Android app  
- [ ] Alert history screen (view alerts & timestamps)  

---

## 🔔 Notifications
- [x] Basic notification listener  
- [ ] Connect with backend alerts  
- [ ] Display notification log  
- [ ] Sound/vibration customization  

---

## 💾 Local Data Storage
- [ ] Store user data with Room  
- [ ] Cache alert history  
- [ ] Save app preferences (e.g., theme, camera list)  

---

## 🎨 UI / UX (Jetpack Compose)
- [x] Compose dependencies setup  
- [ ] SentinelTheme and color palette  
- [ ] Login screen redesign  
- [ ] Dashboard (camera preview + add camera button)  
- [ ] Alert history screen  
- [ ] Settings screen (notifications, preferences)  
- [ ] Onboarding for new users  

---

## 📡 Networking (Retrofit APIs)
- [x] Retrofit integration  
- [ ] `/login`  
- [ ] `/register`  
- [ ] `/upload_frame`  
- [ ] `/fetch_alerts`  
- [ ] Error handling and loading indicators  

---

## 🧰 Deployment & Testing
- [ ] Test across devices (SDK 24–34)  
- [ ] Optimize stream performance  
- [ ] Battery & background service optimization  
- [ ] Signed release build  
- [ ] Play Store deployment  

---

## 💡 Future Enhancements
- [ ] ESP32-CAM integration  
- [ ] Web dashboard for remote monitoring  
- [ ] Voice alert system (TTS)  
- [ ] Geo-tag alerts with GPS  
- [ ] Multi-user sharing  
