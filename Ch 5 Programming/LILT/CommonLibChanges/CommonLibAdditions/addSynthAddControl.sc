/* IZ 050701

Create a control synth that controls one of the (named) control inputs of this synth
via mapping to a bus. 

Return the new synth.

Allocate a new bus before starting the control synth. 
Map the named control of this synth to the bus. 
When the control synth is freed, the control of this synth is unmapped from the bus,
and the bus is automatically deallocated.  


a = Synth("variable_sin", [\amp, 0.01]);
b = a.addControl(\freq, "krsaw", \mul, 400, \add, 1000);

a.free
b.free;
a
a.map(\amp, -1);

a = Synth("variable_sin");
a.group.isRunning;
b = a.addControl(\freq, "ksaw", \mul, 20, \add, 500);
a.group.isRunning;
b.free;
a.group.isRunning;
*/

+ Synth {
	addControl { | control = \amp, defname ... args |
		var bus, synth;
		bus = Bus.control;
		synth = Synth(defname, [\out, bus.index] ++ args, this, \addBefore);
		this.map(control, bus.index);
		// following needed for graceful handling of unmap only when synth still running
		isRunning = true;
		group.isRunning = true;
		CmdPeriod.add(this);	// register that you stopped early to prevent unmap glitch
		this.onEnd {
			isRunning = false;
		};
		synth.onEnd {
			if (isRunning and: { group.isRunning }) {
				this.map(control, -1);
			};
			bus.free;
		};
		^synth;
	}

	// when Cmd-. don't wait for NodeWatcher notification, to prevent glitch 
	cmdPeriod { isRunning = false }

}