Figure 4.1
Create a network address representing sclang itself
~host = NetAddr("localhost", NetAddr.langPort);
// Create two OSC responders for the same command
~r1 = OSCresponderNode(
	~host, "/testMsg",
	{ | time, responder, message, address |
		[\responder1, time, message, address].postln;
	}
).add;
~r2 = OSCresponderNode(
	~host, "/testMsg",
	{ | time, responder, message, address |
		[\responder2, time, message, address].postln;
	}
).add;

// Send an OSC message
~host.sendMsg("/testMsg", "OSCresponderNode test");

// Remove responders
~r1.remove;
~r2.remove;


