/* IZ 050628

TrigWatcher: Register a function to be evaluated when a trigger is received from a synth. 
Trig1Watcher: Evaluate the registered function as soon as the first trigger is received,
    and immediately remove it. That is: Listen only to 1 trigger. 

SynthDef("help-SendTrig2",{
	var trig;
	trig = Dust.kr(1.0);
	SendTrig.kr(trig,0, Demand.kr(trig, 0, Drand([1, 3, 2, 7, 8], inf)));
}).send(s);

a = Synth("help-SendTrig2");

w = TrigWatcher(a, 0, { |val|
	Post << "just received a trigger, " <<
	"and guess what, it also sent me a value: " << val << "\n";
});

{ w.remove }.defer(10);

a.free;

//////////////////////

s = Server.local;
s.boot;

SynthDef("help-SendTrig",{
	SendTrig.kr(Dust.kr(1.0),0,0.9);
}).send(s);

a = Synth("help-SendTrig");

w = TrigWatcher(a, 0, { |val|
	Post << "just received a trigger, " <<
	"and guess what, it also sent me a value: " << val << "\n";
});

{ w.remove }.defer(10);

a.free;

*/

TrigWatcher {
	classvar <responders;
	classvar <watchers;

	var <>synth;
	var <>id = 0;
	var <>action;

	*initClass {
		responders = IdentityDictionary.new;
		watchers = MultiLevelIdentityDictionary.new;
	}

	*new { | synth, id = 0, action |
		var new, w;
		this.addResponder(synth.server.addr);
		new = super.newCopyArgs(synth, id, action);
		watchers.put(synth.server.addr, synth.nodeID, id,
			watchers.at(synth.server.addr, synth.nodeID, id) add: new);
		CmdPeriod.add(new);	// a TrigWatcher should remove itself upon Command-Period
		^new;
	}

	*addResponder { | addr |
		if (responders[addr].isNil) {
			responders.put(addr, OSCresponder(addr, '/tr', { | time, resp, msg |
				watchers.at(addr, msg[1], msg[2]).do { |w|
					w.respond(msg[3]) };
			}).add);
		}
	}

	respond { | val |
		action.(val, this);
	}

	// a TrigWatcher should remove itself upon Command-Period
	cmdPeriod {
		this.remove;
		CmdPeriod.remove(this);
	}

	remove {
		var w;
		w = watchers.at(synth.server.addr, synth.nodeID, id);
//		['before remove', w].postln;
		w.remove(this);
		// for debug:
//		['after remove', watchers.at(synth.server.addr, synth.nodeID, id)].postln;
		if (w.size == 0) {
			watchers.removeAt(synth.server.addr, synth.nodeID, id);
			w = watchers.at(synth.server.addr, synth.nodeID);
			if (w.size == 0) {
				watchers.removeAt(synth.server.addr, synth.nodeID);
			}
		};
		// for debug:
//		"-----------------------------------------".postln;
//		watchers.postln;
	}

	printOn { arg stream;
		stream << this.class.name << "(" << synth.server.addr.hostname
			<< " " << synth.nodeID << " " << id << ")";

	}

}

// Triggers only once and removes itself
Trig1Watcher : TrigWatcher {

	respond { | val |
		action.(val, this);
		this.remove;
	}
}
