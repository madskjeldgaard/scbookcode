// Figure 4.1
//
// Create two OSC responders for the same command
~r1 = OSCresponderNode(
	~host, "/testMsg",
	{ | time, responder, message, address |
		fork {
			(time - thisThread.seconds).wait;
			[\responder1, time, message, address].postln;
		};
	}
).add;
~r2 = OSCresponderNode(
	~host, "/testMsg",
	{ | time, responder, message, address |
		fork {
			(time - thisThread.seconds).wait;
			[\responder2, time, message, address].postln;
		};
	}
).add;


// Create a network address representing sclang itself
~host = NetAddr("localhost", NetAddr.langPort);

// Send an OSC message
~host.sendMsg("/testMsg", "OSCresponderNode test");

// Send an OSC message
~host.sendMsg( "/testMsg", 42, "string", pi);

// Send an OSC bundle and execute its contents
// 200 ms from 'now'
~host.sendBundle(0.2,
	["/testMsg", 42, "string", pi],
	["/testMsg", 183]);


// Remove responders
~r1.remove;
~r2.remove;

