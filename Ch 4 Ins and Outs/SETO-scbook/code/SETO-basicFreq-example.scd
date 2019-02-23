/*
 * Part of the SuperCollider Book
 * Author of code: Till Bovermann
 * for more information, see http://tuio.lfsaw.de/seto.shtml
 */

/* 
	Basic example for controlling sound rendering with SETO 
	We use Objects of "_ixya" style.
*/

SynthDef(\testTUIO, {|freq = 400, out = 0, amp = 0, vol = 0.25, famp=1|
	Out.ar([out, out+1], SinOsc.ar(freq, 0, (amp.lag(0.01)*vol*famp)))
}).send(s);

q = ();
(
q.synths = IdentityDictionary.new; // a storage for synths

JITseto.action = {|me|
	s.bind{
		// make sure there is a synth
		q.synths[me].isNil.if{
			q.synths[me] = Synth(\testTUIO, [\vol, 0.2, \amp, 0])
		};
		s.sync;
		me.visible.if({
			q.synths[me].set(
				\freq, me.rotEuler[0].wrap(0, 2pi).linexp(0, 2pi, 400, 800),
				\amp, 1
			)
		}, {
			q.synths[me].set(
				\amp, 0
			)
		})
	}
}
)

// instantiate SETOServer
t = SETO_OSCServer('_ixya', setoClass: JITseto);
t.gui;
t.start;
t.stop;