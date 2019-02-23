/* IZ 050626 / 060812
Utilities for making a node (synth or group) perform some action when 
it is freed (removed) from the server or when it actually starts. 

Useful whenever some depends on a node running or not. 
onEnd is used by Script to notify gui. onStart is used to perform mapping of inputs to busses
in a timely manner. 

Illustration of the use of onStart: 
When mapping a parameter to a bus immediately at the creation of a synth, 
there is often an error 
	FAILURE /n_map Node not found
Because the node is not yet created by the time the c.map operation is sent. 
(
b = Bus.control;
c = { | freq = 400 | SinOsc.ar(freq, 0, 0.1) }.play;
p = { Out.kr(b.index, SinOsc.kr(7, 0, 200, 800)) }.play;
c.map(\freq, b.index);
)

This can be avoided by deferring the map operation by a fixed interval.
However, it is not clear what interval that should be (too large may produce
unwanted effects, to small may be unreliable): 

(
b = Bus.control;
c = { | freq = 400 | SinOsc.ar(freq, 0, 0.1) }.play;
p = { Out.kr(b.index, SinOsc.kr(7, 0, 20, 400)) }.play;
{ c.map(\freq, b.index); }.defer(0.001);
)

Using on start one can make the map be sent as soon as the 
client receives notification that the synth node has been created.

(
b = Bus.control;
c = { | freq = 1000 | SinOsc.ar(freq, 0, 0.1) }
	.play
	.onStart({ c.map(\freq, b.index); });
p = { Out.kr(b.index, LFPulse.kr(1, 0.5, 0.5, 200, 400)) }.play;
)

Note that the lag between the start of the audio synth with a different
default freq value (1000) and the binding to the map control value (400)
is audible. But so it is in the case of a defer of 0.001 - not always.
Plus there is the risk that the node to map may not have been created yet. 
 
(
b = Bus.control;
c = { | freq = 1000 | SinOsc.ar(freq, 0, 0.1) }.play;
p = { Out.kr(b.index, LFPulse.kr(1, 0.5, 0.5, 200, 400)) }.play;
{ c.map(\freq, b.index); }.defer(0.001);
)

Just another example. Adding multiple onEnd functions to one synth: 
(
a = Synth("variable_sin")
	.onEnd({ 'hi there i just stopped'.postln; })
	.onEnd({ { 'and there is even more stuff'.postln } ! 5});
)
a.free;
*/

+ Node {
	// stop should work both with Node and EventStreamPlayer
	// for Script to use just this one message for both.
	stop { this.free }
	onStart { | argFunc |
		var cmdPeriodFunc;
		NodeWatcher.register(this);
		// This evaluates argFunc when notified by NodeWatcher
		this.addDependant { | me, whatHappened |
			if (whatHappened == \n_go) {
				argFunc.(this);
				this.removeDependant(argFunc);
			}
		};
	}
	onEnd { | argFunc |
		var cmdPeriodFunc;
		NodeWatcher.register(this);
		// this is needed to add this to catch synths stopped by Command-Period:
		cmdPeriodFunc = {
			NodeWatcher.unregister(this);
			CmdPeriod.remove(cmdPeriodFunc);
			argFunc.value;
		};
		CmdPeriod.add(cmdPeriodFunc);
		// This evaluates argFunc when notified by NodeWatcher
		this.addDependant { | me, whatHappened |
			if (whatHappened == \n_end) {
				argFunc.(this);
				this.removeDependant(argFunc);
				// see CmdPeriod above
				CmdPeriod.remove(cmdPeriodFunc);
			}
		};
	}
}

+ EventStreamPlayer {
	onEnd { | argFunc |
		this.addDependant { | me, whatHappened |
			if (whatHappened == \stopped) {
				argFunc.(this);
				this.removeDependant(argFunc);
			}
		}
	}
	onStart { | argFunc |
	// immediately do argFunc. For compatibility with Node, 
	// to avoid checking respondsTo 2 times in Script
		argFunc.(this);
	}
}
