definition(name: "Door Autolocker", namespace: "tattiebogle", author: "Colin Munro",
          iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
          iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
          description: "Lock a door if it's closed"
          )

preferences {
    section("Select devices") {
        input "lock1", "capability.lock", title: "Select a lock", required: true
        input "door1", "capability.contactSensor", title: "Select a door contact sensor", required: true
        input "wait", "decimal", title: "Time to wait (in seconds)", required:true, defaultValue: 60
    }
}

def installed() {
    initialize()
}

def initialize() {
    subscribe door1, "contact.open", openHandler
    subscribe door1, "contact.closed", closeHandler
}

def openHandler(evt) {
    unschedule(timeoutHandler)
}

def closeHandler(evt) {
    runIn((int)wait, timeoutHandler)
}

def timeoutHandler() {
    lock1.lock()
}
