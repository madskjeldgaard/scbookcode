/*
 * Part of the SuperCollider Book
 * Author of code: Till Bovermann
 * for more information, see http://tuio.lfsaw.de/seto.shtml
 */

/* 
	Basic example for controlling sound rendering with TUIO 
	Given a fixed set of Objects, each having i,x,y,a and the object ids are 0..numObj-1.
*/

SynthDef(\testTUIO, {|freq = 400, out = 0, amp = 0, vol = 0.25|
	Out.ar(out, SinOsc.ar(freq, 0, amp*vol)!2)
}).send(s);

(
var synths, numObj = 4, resp;

// Create a Synth for each object. make sure it will not play.
synths = Array.fill(numObj, {
	Synth(\testTUIO, [\vol, numObj.reciprocal, \amp, 0])
});

// set up OSC responder
resp = OSCresponder(nil, "/tuio/_ixya", {|time, resp, msg|
	var id, classID, x, y, a, pos, amps;
	
	// if object state is updated, change frequency of corresponding synth 
	(msg[1] == \set).if{
		# id, classID, x, y, a = msg[2..6];
		synths[id].set(
			\freq, a.wrap(0, 2pi).linexp(0, 2pi, 400, 800)
		)
	};
	
	// only play synths for alive (i.e. visible) objects
	(msg[1] == \alive).if{
		amps = Array.fill(numObj, 0);
		msg[2..].do{|i|
			amps[i] = 1;
		};
		synths.do{|synth, i|
			synth.set(\amp, amps[i])
		}
	};
}).add;
)