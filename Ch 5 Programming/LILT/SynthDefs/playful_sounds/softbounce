
SynthDef("softbounce", 
	{ arg out = 0, intensity = 1, gravity = 0.1, damping = 0.01, friction = 0.01,
		freq = 1200, ring = 0.1, decayAmp = 50, amp = 0.1, attack = 0.05, 
		decay = 0.1, pan = 0;
		var surface;
		var tBall, resonz;
		var timer;
		surface = Trig.ar(intensity, 44100.reciprocal);
		// The DelayN was introduced to avoid a sound glitch in the
		// manner that Ringz reacts without it in this particular algorithm
		// the reason for the glitch when there is no delay is not known to me.
		tBall = TBall.ar(DelayN.ar(surface, 0.01, 0.01), 
			gravity, damping, friction);
		resonz = Ringz.ar(Decay2.ar(tBall, attack, decay, decayAmp ), freq, ring);
		DetectSilence.ar(tBall, 0, 10, 2);	// stop 10 secs after bounce stops
		Out.ar(out, Pan2.ar(resonz, pan, amp));
	}, [\ir]
);
