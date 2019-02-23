Figure 16.3: Order confusion with sounds in fast succession. 
	As grains move closer and closer together, their order becomes ambiguous. 

(
			// a simple percussive envelope
SynthDef(\percSin, { |out, amp=0.1, freq=440, sustain=0.01, pan| 
	var snd = FSinOsc.ar(freq);
	var env = EnvGen.ar(
		Env.perc(0.1, 0.9, amp), timeScale: sustain, doneAction: 2);
	OffsetOut.ar(out, Pan2.ar(snd * env, pan));
}, \ir ! 5).add;
)
(
Pbindef(\lo, 
	\instrument, \percSin, \sustain, 0.05, 
	\freq, 250, \amp, 0.2, \dur, 0.5, \lag, 0
).play;
Pbindef(\hi, 
	\instrument, \percSin, \sustain, 0.05, 
	\freq, 875, \amp, 0.1, \dur, 0.5, \lag, 0
).play;
)
	// try different lag times between them
Pbindef(\hi, \lag, 0.1);
Pbindef(\hi, \lag, 0.03);
Pbindef(\hi, \lag, 0.01);
Pbindef(\hi, \lag, 0.003);

	// hi too early or too late by a fixed time - which one is first?
Pbindef(\hi, \lag, ([-1, 1].choose * 0.01).postln).play;
Pbindef(\hi, \lag, ([-1, 1].choose * 0.02).postln);

	// is it easier to hear when the sounds are panned apart?
Pbindef(\hi, \pan, 0.5); Pbindef(\lo, \pan, -0.5);
Pbindef(\hi, \pan, 0);   Pbindef(\lo, \pan, 0);


