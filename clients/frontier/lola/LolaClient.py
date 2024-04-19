import json

import rel
import websocket

class AttackVectorConnection:
    connection_id_counter = 0
    ws = None
    user_name = None
    run_id = None
    site_id = None
    site_name = None

    def __init__(self, jwt):
        cookie = f"jwt={jwt}"

        self.ws = websocket.WebSocketApp("ws://localhost/ws_hacker",
                                         on_open=self.ws_on_open,
                                         on_message=self.ws_on_message,
                                         on_error=self.ws_on_error,
                                         on_close=self.ws_on_close,
                                         cookie=cookie)

        # Set dispatcher to automatic reconnection, 5 second reconnect delay if connection closed unexpectedly
        self.ws.run_forever(dispatcher=rel, reconnect=5)

    def subscribe(self, topic):
        self.ws.send(f"SUBSCRIBE\nid:{self.user_name}_{self.connection_id_counter}\ndestination:{topic}\n\n\x00")
        self.connection_id_counter += 1

    def ws_on_message(self, ws, message):
        # print(f"incoming: {message}")
        lines = message.split("\n")
        if lines[0] == "CONNECTED":
            self.on_connect_message(lines)

        if lines[0] == "MESSAGE":
            message_length = int(lines[5].split(":")[1])
            # print(f"Message length: {message_length}")
            message = lines[7][0:message_length]
            # print(f"Received message: {message}")
            message_object = json.loads(message)

            if message_object["type"] == "SERVER_ENTERED_RUN":
                self.incoming_call_enter_run(message_object["data"])
            if message_object["type"] == "SERVER_SPEAK":
                self.incoming_call_speak(message_object["data"])
            if message_object["type"] == "SERVER_RUN_TIMER":
                self.incoming_call_timers(message_object["data"]["timers"])

    def on_connect_message(self, lines):
        self.user_name = lines[3].split(":")[1]
        print("Connected as: " + self.user_name)
        user_id = self.user_name.split("_")[0]
        self.subscribe(f"/user/reply")
        self.subscribe(f"/topic/user/{user_id}")
        self.subscribe(f"/topic/user/{self.user_name}")

    def ws_on_error(self, ws, error):
        print(error)

    def ws_on_close(self, ws, close_status_code, close_msg):
        print("### closed ###")

    def ws_on_open(self, ws):
        # print(f"Opened connection")
        ws.send("CONNECT\naccept-version:1.0\nheart-beat:0,0\n\n\x00")
        # print(f"Sent CONNECT")

    def send_get_timers(self):
        self.send(f"/av/run/getTimers", f"{self.run_id}")

    def send(self, destination, payload):
        message = f"SEND\ndestination:{destination}\ncontent-length:{len(payload)}\n\n{payload}\x00"
        # print("sending: " + message)
        self.ws.send(message)

    def incoming_call_enter_run(self, data):
        self.run_id = data["run"]["runId"]
        self.site_id = data["site"]["siteProperties"]["siteId"]
        self.site_name = data["site"]["siteProperties"]["name"]
        print(f"Entered site: {self.site_name} (siteId: {self.site_id}): in run (runId: {self.run_id})")

        # Process the timer data from the run
        self.incoming_call_timers(data["timers"])

        # do a request to get the latest timer state for the current run.
        # Schedule this for example every 10 seconds.
        # in this sample code we just do it once to check that it works.
        self.send_get_timers()

    def incoming_call_timers(self, timers):
        for timer in timers:
            print(f"Timer {timer['timerId']} finishes at: {timer['finishAt']}")

    def incoming_call_speak(self, text):
        print(f"Text to speak: {text}")


if __name__ == "__main__":
    # websocket.enableTrace(True)
    authentication = "{past raw jwt token here}"

    stompClient = AttackVectorConnection(authentication)

    rel.signal(2, rel.abort)  # Keyboard Interrupt
    rel.dispatch()
