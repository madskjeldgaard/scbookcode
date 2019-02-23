/* IZ 2007-01-29 { SC3 (Copied from ServerWatcher.sc file comments)

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

} */
