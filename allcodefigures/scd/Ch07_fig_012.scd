
Figure 7.12

// this synthdef is used in the subsequent figures
(
SynthDef(\wave, { |out, freq=440, amp=0.1, sustain=0.1, mod=0.2|
	OffsetOut.ar(out,
		EnvGen.ar(Env.perc(ExpRand(0.001, 0.05), sustain, amp), doneAction: 2)
		*
		SinOsc.ar(freq, SinOsc.ar(sustain.reciprocal * 8, [0, Rand(0, pi)], mod))
	)
}).add
);
(
Tdef(\x, {
	x = 4; y = 13;
	loop {
		x = (x * y) % 11;
		(instrument: \wave, note: x.postln, sustain: 0.5, octave: 6).play;
		0.125.wait;
	}
}).play
);


