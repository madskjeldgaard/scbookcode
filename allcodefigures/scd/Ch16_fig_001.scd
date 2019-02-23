Figure 16.1: Short grain durations, pitch to colored click 

(	// a gabor grain, gaussian-shaped envelope
SynthDef(\gabor, { |out, freq = 440, sustain = 1, pan, amp = 0.1, width = 0.25 |
	var env = LFGauss.ar(sustain, width, loop: 0, doneAction: 2);
	var son = FSinOsc.ar(freq, 0.5pi, env);
	OffsetOut.ar(out, Pan2.ar(son, pan, amp));

}, \ir ! 6).add;

	// or an approximation with a sine-shaped envelope
SynthDef(\gabor1, { |out, amp=0.1, freq=440, sustain=0.01, pan| 
	var snd = FSinOsc.ar(freq);
	var env = EnvGen.ar(Env.sine(sustain, amp), doneAction: 2);
	OffsetOut.ar(out, Pan2.ar(snd * env, pan));
}, \ir ! 5).add;
)

(
Pbindef(\grain,
	\instrument, \gabor, \freq, 1000, 
	\dur, 0.5, \sustain, 20/1000, \amp, 0.2
).play;
)
Pbindef(\grain, \sustain, 10/Pkey(\freq));
Pbindef(\grain, \sustain, 5/Pkey(\freq));
Pbindef(\grain, \sustain, 3/Pkey(\freq));
Pbindef(\grain, \sustain, 2/Pkey(\freq));
Pbindef(\grain, \sustain, 1/Pkey(\freq));

	// successively shorter, end
Pbindef(\grain, \sustain, Pseq((10..1)) / Pkey(\freq)).play;

	// random drift of grain duration
Pbindef(\grain, \sustain, Pbrown(1, 10, 3) / Pkey(\freq), \dur, 0.1).play

