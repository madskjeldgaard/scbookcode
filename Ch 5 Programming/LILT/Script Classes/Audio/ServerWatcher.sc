/* IZ 04xxxx - 050903 - 070116 (quitting check redone) {

ServerWatcher registers actions (functions) that an object may want to execute whenever the status of a Server changes in any one of the following three ways: 
1. The server boots.
2. The server quits.
3. The server's nodes are freed and reset (usually in response to Command-. for stopping all sounds and routines)

If the SC lang client starts up while that server is already on (was not stopped when quitting the SuperCollider client application), then any onBoot actions will be executed as soon as the running server is recognized by the clients polling mechanism that updates the default server windows. This means, any classes that want to initialize data on the server as soon as the server is running will work in both cases:

- When the server is booted from the client
- When the client starts up but the server is already running

The ServerWatcher uses somewhat elaborate mechanisms for checking both the boot and the quit conditions. This is in order to circumvent the standard StatusWatcher which gives false notifications of server-startup and quit when a computer goes to sleep and wakes up. 

Check for booting: The ServerWatcher checks for the existence of a buffer that contains 4 samples with specific values. This both prevents false notification if the computer is waking up from sleep and works correctly if the server is already running when the client starts up. 

Check for quitting: Install an OSCresponderNode that watches the default Group of the server (its NodeID is 1), and then checks the value of the Server's serverRunning variable. This avoids false notifications of server quit that are sent by Server:aliveThread when the computer (and therefore the server with it also) goes to sleep. 

For responding to node-reset events, the ServerWatcher uses the Servers 'tree' variable. That means if you use the ServerWatcher for reset-watching, then you are obliged to use it for *any* actions that you want done at initTree time. This is more an extension than a limitation, as the ServerWatcher allows you to install multiple actions in the tree variable. The ServerWatcher will not touch the tree variable unless you start using its 'onReset' method. That is, it is possible to use the ServerWatcher along with any other way of occupying the tree variable, as long as the onReset method is not called on ServerWatcher. 

Examples:

Make object integer 1 respond to Server.local boot and quit:

1.onBoot({ |me| [me, "Ready for action"].postln; });
1.onQuit({ |me| [me, "bye bye"].postln; });

Same thing using ServerWatcher directly:

ServerWatcher.onBoot(1, { |me, server| [me, "my server booted"].postln; });
ServerWatcher.onQuit(1, { |me, server| [me, "my server quit"].postln; });

// Create a new Group whenever the local Server's nodes are reset: 

1.onReset({ Group(Server.local).postln; }, Server.local);

// stop responding to server boots, quits, and resets:
1.removeServer;

Also possible: 
f = { Group(Server.local).postln; };
f.onReset(Server.local);
// to remove: 
f.removeServer(Server.local);

050903 
Redo of ServerWatcher to prevent server-boot messages when the computer awakes from sleep. 
This works by allocating a buffer and filling it with an array of "magic numbers": #[1000, 2000, 314, 999]. When the StatusWatcher receives a serverRunning message and the serverRunning value of the server is true, then the StatusWachter checks first to see if the allocated buffer still has the same magic values. If yes, then the server was awaked from sleeping and no re-allocation or loading of any buffers is needed.

070116 Rewrote quit check mechanism using OSCresponderNode on default Group and added onReset mechanism. 

NOTE: Tried in vain to get around the buffer checking mechanism above, but could not find another solution! Keeping the buffer mechanism!

} */

ServerWatcher {
	classvar <all;
	var <server;	// server to watch
	var <buffer;	// buffer for checking if this is still the old server instance
	var <checkArray;	// array of 16 values to check for identity of test buffer.
	var <bootObjects, <quitObjects, <resetObjects;

	*initClass {
		all = IdentityDictionary.new;
	}

	*onBoot { | anObject, anAction, aServer, doNowIfRunning = false |
		this.for(aServer).onBoot(anObject, anAction, doNowIfRunning);
	}

	*onQuit { | anObject, anAction, aServer, doNowIfStopped = false |
		this.for(aServer).onQuit(anObject, anAction, doNowIfStopped);
	}

	*onReset { | anObject, anAction, aServer, doNowIfRunning = false |
		this.for(aServer).onReset(anObject, anAction, doNowIfRunning);
	}

	onBoot { | anObject, anAction, doNowIfRunning = false |
//		[this, anObject, anAction, doNowIfRunning]
		bootObjects[anObject] = bootObjects[anObject].add(anAction ? anObject);
		if (doNowIfRunning) {
			if (server.serverRunning) { anAction.(anObject) }
		}
	}

	onQuit { | anObject, anAction, doNowIfStopped = false |
		quitObjects[anObject] = quitObjects[anObject].add(anAction ? anObject);
		if (doNowIfStopped) {
			if (server.serverRunning.not) { anAction.(anObject) }
		}
	}

	onReset { | anObject, anAction, doNowIfRunning = false |
		server.tree = { this.serverReset };
		resetObjects[anObject] = resetObjects[anObject].add(anAction ? anObject);
		if (doNowIfRunning) {
			if (server.serverRunning) { anAction.(anObject) }
		}
	}

	*for { | aServer |
		var instance;
		aServer = aServer ?? { Server.local };
		instance = all.at(aServer);
		if (instance.isNil) {
			instance = this.new(aServer);
			all.put(aServer, instance);
		};
		^instance;
	}

	*new { | server |
		^super.new.init(server);
	}

	init { | argServer |
		server = argServer ?? { Server.local };
		// create buffer for checking whether the server really started
		if (server.serverRunning) { this.makeCheckBuffer };
		// create node for watching whether the default server group ended
		// this avoids false notifications from when the server goes to sleep. 
		OSCresponderNode(server.addr, '/n_end', { | time, resp, msg |
			if (msg[1] == 1 and: { server.serverRunning.not }) {
				this.serverEnded;
			};
		}).add;
		server.addDependant(this);
		this.clear;
	}

	// remove anObject from both boot and quit list of aServers ServerWatcher
	// if aServer is nil, remove it from all servers. This way
	// if a view does not remember which server it is watching, it can still remove itself
	*remove { | anObject, aServer |
		(all.at(aServer) ?? { all.values }).asArray.do { | sw |
			sw.remove(anObject);
		};
	}

	// TODO: write individual remove methods for the 3 status-watching methods of ServerWatcher
	remove { | anObject |
		bootObjects.removeAt(anObject);
		quitObjects.removeAt(anObject);
		resetObjects.removeAt(anObject);
	}

	*clear { | server |
		var who;
		if ((who = all.at(server)).notNil) { who.clear };
	}

	clear {
		bootObjects = IdentityDictionary.new;
		quitObjects = IdentityDictionary.new;
		resetObjects = IdentityDictionary.new;
	}

	update { | theServer, statusMessage |
		// update is received by the server via its dependency mechanism. 
		// Here only check to see if the server really booted.
		// Server quit monitoring is done via an OSCresponderNode 
		// watching the default server group, created by init method above.
		if (statusMessage == \serverRunning and: theServer.serverRunning) {
				this.checkIfReallyBooted;
		}
	}

	checkIfReallyBooted {
		if (buffer.isNil) {
			this.makeCheckBuffer;
			^this.serverReallyBooted;
		};
		buffer.updateInfo({ | buf |
			if (buf.numFrames != 4) {
				this.makeCheckBuffer;
				this.serverReallyBooted;
			}{
				buffer.getn(0, 4, { | vals |
					if (vals != checkArray) { this.serverReallyBooted };
				})
			}
		})
	}

	makeCheckBuffer {
		checkArray = #[1000, 2000, 314, 999];
		buffer = Buffer.sendCollection(server, checkArray, 1);
	}

	serverReallyBooted {
		bootObjects.keysValuesDo { | object, actions |
			actions do: _.value(object);
		}
	}

	serverEnded {
		quitObjects.keysValuesDo { | object, actions |
			actions do:  _.value(object);
		};
	}

	serverReset {
		resetObjects.keysValuesDo { | object, actions |
			actions do:  _.value(object);
		};
	}
}
