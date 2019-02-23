/* IZ 050703

Make any object call any function(s) when a specific server has booted, has quit, or 
has reset its nodes (as a response to freeAll for example).
See ServerWatcher for more details. Examples: 

// Provide an action to be called when the local server calls:
1.onBoot({ |me| [me, "Ready for action"].postln; });
// Note: the third argument is the server to watch. Defaults to Server.local
// To respond to any other server, provide the server as third argument:
1.onBoot({ |me| [me, "Ready for action"].postln; }, Server.internal);

// Provide an action to be called when the local server quits: 
1.onQuit({ |me| [me, "bye bye"].postln; });

now try: 
Server.local.boot;
and then: 
Server.local.quit;

// To stop responding to server boots and quits: 
1.removeServer;

*/

+ Object {
	onBoot { | argAction, argServer, doNowIfRunning = false |
		ServerWatcher.onBoot(this, argAction, argServer, doNowIfRunning);
	}

	onQuit { | argAction, argServer, doNowIfStopped = false |
		ServerWatcher.onQuit(this, argAction, argServer, doNowIfStopped);
	}

	onReset { | argAction, argServer, doNowIfRunning = false |
		ServerWatcher.onReset(this, argAction, argServer, doNowIfRunning);
	}

	// remove your onBoot and onQuit actions from argServer
	removeServer { | argServer |
		ServerWatcher.remove(this, argServer);
	}
}

+ Function {

	onBoot { | argServer, doNowIfRunning |
		ServerWatcher.onBoot(this, this, argServer, doNowIfRunning);
	}

	onQuit { | argServer, doNowIfStopped |
		ServerWatcher.onQuit(this, this, argServer, doNowIfStopped);
	}

	onReset { | argServer, doNowIfRunning |
		ServerWatcher.onReset(this, this, argServer, doNowIfRunning);
	}

}
