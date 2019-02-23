			// Microsound - code figures //


		// perception at the micro time scale //
		
	//  pulses, transition from rhythm to pitch 
{ Impulse.ar (XLine.kr(12, 48, 6, doneAction: 2)) * 0.1 ! 2 }.play; // up

{ Impulse.ar (XLine.kr(48, 12, 6, doneAction: 2)) * 0.1 ! 2 }.play; // down

{ Impulse.ar (MouseX.kr(12, 48, 1)) * 0.1 ! 2 }.play; // mouse-controlled





	// figure 16.1 short grain durations - pitch to colored click 
(	// a gabor grain, gaussian-shaped envelope
SynthDef(\gabor, { |out, freq = 440, sustain = 1, pan, amp = 0.1, width = 0.25 |
	var env = LFGauss.ar(sustain, width, loop: 0, doneAction: 2);
	var son = FSinOsc.ar(freq, 0.5pi, env);
	OffsetOut.ar(out, Pan2.ar(son, pan, amp));

}, \ir ! 6).memStore;

	// or an approximation with a sine-shaped envelope
SynthDef(\gabor1, { |out, amp=0.1, freq=440, sustain=0.01, pan| 
	var snd = FSinOsc.ar(freq);
	var env = EnvGen.ar(Env.sine(sustain, amp), doneAction: 2);
	OffsetOut.ar(out, Pan2.ar(snd * env, pan));
}, \ir ! 5).memStore;
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




	// short grains seem softer 
(
Pbindef(\grain, 
	\instrument, \gabor, \freq, 1000, \dur, 1,
	[\sustain, \amp], Pseq([[0.001, 0.1], [0.1, 0.1]], inf) 
).play;
)
		// short grain 2x louder 
Pbindef(\grain, [\sustain, \amp], Pseq([[0.001, 0.2], [0.1, 0.1]], inf));

		// short grain 4x louder
Pbindef(\grain, [\sustain, \amp], Pseq([[0.001, 0.4], [0.1, 0.1]], inf));



	// a grain with quasi-rectangular envelope, short grain 6x louder.
(
SynthDef(\pip, { |out, freq=440, sustain=0.02, amp=0.2, pan=0| 
	OffsetOut.ar(out, 
		Pan2.ar(SinOsc.ar(freq) 
		* EnvGen.ar(Env.linen(0.0005, sustain - 0.001, 0.0005, amp), doneAction: 2), pan)
	); 
}).memStore;

	// is this equal loudness?
Pbindef(\grain).clear;
Pbindef(\grain, 
	\instrument, \pip,
	\freq, 1000, \dur, 1,
	\sustain, Pseq([0.001, 0.1], inf), 
	\amp, Pseq([0.6, 0.1], inf) 	
).play;
)




	// figure 16.2 - Perception of short silences.
(
p = ProxySpace.push;

~source = { SinOsc.ar * 0.1 };
~silence = { |silDur=0.01| 
	EnvGen.ar(
		Env([0, 1, 1, 0, 0, 1, 1, 0], [0.01, 2, 0.001, silDur, 0.001, 2, 0.01]), 
		doneAction: 2) ! 2
};
~listen = ~source * ~silence;
~listen.play;
)

~silence.spawn([\silDur, 0.001]); // sounds like an added pulse 
~silence.spawn([\silDur, 0.003]);
~silence.spawn([\silDur, 0.01]);
~silence.spawn([\silDur, 0.03]);	  // a pause in the sound

	// try the same examples with noise:
~source = { WhiteNoise.ar * 0.1 };	

p.clear.pop; 




	// figure 16.3 - order confusion with sounds in fast succession. 
	// as grains move closer and closer together, their order becomes ambiguous. 
(
			// a simple percussive envelope
SynthDef(\percSin, { |out, amp=0.1, freq=440, sustain=0.01, pan| 
	var snd = FSinOsc.ar(freq);
	var env = EnvGen.ar(
		Env.perc(0.1, 0.9, amp), timeScale: sustain, doneAction: 2);
	OffsetOut.ar(out, Pan2.ar(snd * env, pan));
}, \ir ! 5).memStore;
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





	// figure 16.4: multiple grains fuse into one composite.
	// when their order changes, the sound is subtly different.
(
Pbindef(\grain4, 
	\instrument, \percSin, \sustain, 0.03, \amp, 0.2,
	\freq, Pshuf([1000, 600, 350, 250]), // random every each time
	\dur, 0.005
).play;
				// repeat grain cluster 
Tdef(\grain, { loop { Pbindef(\grain4).play; 1.wait } }).play;
)
	// fixed order
Pbindef(\grain4, \freq, Pseq([1000, 600, 350, 250].scramble));

	// different order every time
Pbindef(\grain4, \freq, Pshuf([1000, 600, 350, 250]));

