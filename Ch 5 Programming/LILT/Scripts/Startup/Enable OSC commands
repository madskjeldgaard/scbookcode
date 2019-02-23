/* (IZ 2005-08-21)
Example of a 'sub-script' for startup. Must be loaded with:

Scripts.loadPath('Startup', 'Enable OSC commands');

Make it possible to send any SC code in text for compilation via OSC:
*/

/* 1. 'do' just interprets the first argument of the message as string, without arguments. Example: 
a = NetAddr("127.0.0.1", 57120);
a.sendMsg('do', "Post << 'Hello world. ' <<* Array.rand(10, -10, 10) << \"\\n\"");
*/

OSCresponder(nil, 'do', { | time, resp, msg |
	var msgname, args;
	#msgname, msg ... args = msg;
	{ msg.asString.interpret; }.defer;
}).add;

/* 2. 'doArgs' presupposes that the first arg string returns a function and evaluates that function using the rest of the arguments as arguments. Example: 
a = NetAddr("127.0.0.1", 57120);
a.sendMsg('doArgs', "{ | f, a | Synth('variable_sin', [\\freq, f, \\amp, a]) }", 567, 0.2);
*/

OSCresponder(nil, 'doArgs', { | time, resp, msg |
	var msgname, args;
	#msgname, msg ... args = msg;
	{ msg.asString.interpret.(*args); }.defer;
}).add;

