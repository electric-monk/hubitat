import groovy.transform.Field

metadata {
    definition (name: "Ring Z-Wave Contact Sensor",namespace: "tattiebogle", author: "Colin Munro") {
		capability "Contact Sensor"
		capability "Battery"
		capability "Tamper Alert"
	}
}

@Field Map zwLibType = [
	0:"N/A",1:"Static Controller",2:"Controller",3:"Enhanced Slave",4:"Slave",5:"Installer",
	6:"Routing Slave",7:"Bridge Controller",8:"Device Under Test (DUT)",9:"N/A",10:"AV Remote",11:"AV Device"
]

def parse(String description) {
	log.info description
	def result = []
    def cmd = zwave.parse(description,[0x85:1,0x86:1])
    if (cmd) {
        result += zwaveEvent(cmd)
    }
	log.debug result
	return result
}

def handleShenanigan(int event, boolean action) {
	def result = []

	switch(event) {
		case 2:	// intrusion
			result << createEvent(name:"contact", value:action ? "open" : "closed")
			break
		case 3: // tamper
			result << createEvent(name:"tamper", value:action ? "detected" : "clear")
			break
		default:
			log.debug "Unhandled shenanigan: ${event} (${action})"
			break
	}
	return result
}

def zwaveEvent(hubitat.zwave.commands.notificationv3.NotificationReport cmd) {
	def result = []

	if (cmd.notificationType == 7 /* ZWAVE_ALARM_TYPE_BURGLAR */) {
		if (cmd.event == 0) {
			for (i = 0; i < cmd.eventParametersLength; i++) {
				result << handleShenanigan(cmd.eventParameter[i], false)
			}
		} else {
			result << handleShenanigan(cmd.event, true)
		}
	} else {
		log.debug "Unhandled report: ${cmd}"
	}
	
	return result
}

def zwaveEvent(hubitat.zwave.commands.batteryv1.BatteryReport cmd) {
	log.debug "BatteryReport: $cmd"
	def val = (cmd.batteryLevel == 0xFF ? 1 : cmd.batteryLevel)
	if (val > 100) {
		val = 100
	}
	state.lastBatteryReport = new Date().time	
	
	def result = []
	result << createEvent(name: "battery", value: val, unit: "%")

	return result
}

def zwaveEvent(hubitat.zwave.Command cmd) {
    log.debug "Unhandled: ${cmd}"
}

def installed(){}

def configure() {}

def updated() {}


