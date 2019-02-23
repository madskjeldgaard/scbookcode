
Figure 7.26

(
SynthDef(\train, { |out, xfreq=15, sustain=1.0, amp=0.1, pan|
	Line.ar(1, 1, sustain, doneAction:2);
	OffsetOut.ar(out, Pan2.ar(Impulse.ar(xfreq), pan, amp));
}).add
);

Ndef(\z).play;

// a pattern in an audio rate node proxy ...
(
Ndef(\x, Pbind(
	\instrument, \train, 
	\xfreq, Pseq([50, Pwhite(30, 800, 1), 5, 14, 19], inf), 
	\sustain, Pseq([Pwhite(0.01, 0.1, 1), 0.1, 1, 0.5, 0.5], inf),
	\pan, Prand([-1, 1], inf) * 0.1
))
);
Ndef(\y, { Ringz.ar(Ndef(\x).ar, 5000 * [1, 1.2], 0.01) });
Ndef(\mod, { LFNoise1.kr(0.1).exprange(200, 5000) });
Ndef(\z, { Ndef(\y).ar * (SinOsc.ar(Ndef(\mod).kr) + 1) }); // ring modulation with Ndef(\mod)

// ... and a control rate node proxy in a pattern

(
Ndef(\x, Pbind(
	\instrument, \train, 
	\xfreq, Pseq([50, Ndef(\mod), 5, Ndef(\mod), 19], inf), // read from the Ndef(\mod) proxy bus
	\sustain, Pseq([Pwhite(0.01, 0.1, 1), 0.1, 1, 0.5, 0.5], inf),
	\pan, Prand([-1, 1], inf) * 0.1
))
);


