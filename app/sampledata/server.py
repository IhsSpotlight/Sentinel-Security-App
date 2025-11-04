from flask import Flask, request, jsonify
app = Flask(__name__)

@app.route('/api/notify', methods=['POST'])
def notify():
    data = request.get_json()
    print("Received Notification:", data)
    return jsonify({"status": "ok"}), 200

@app.route('/')
def index():
    return "Notification Server is running!"

if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5000)
