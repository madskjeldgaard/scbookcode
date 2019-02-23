// figure 8.23 - two granular synthdefs and tests
(
	// a gabor (approx. gaussian-shaped) grain
SynthDef(\gab1st, { |out, amp=0.1, freq=440, sustain=0.01, pan| 
	var snd = FSinOsc.ar(freq);
	var env = EnvGen.ar(Env.sine(sustain, amp * AmpComp.ir(freq) * 0.5), doneAction: 2);
	OffsetOut.ar(out, Pan2.ar(snd * env, pan));
}, \ir ! 5).add;


			// a simple percussive envelope
SynthDef(\percSin, { |out, amp=0.1, freq=440, sustain=0.01, pan| 
	var snd = FSinOsc.ar(freq);
	var env = EnvGen.ar(
		Env.perc(0.1, 0.9, amp * AmpComp.ir(freq) * 0.5), 
			timeScale: sustain, doneAction: 2
		);
	OffsetOut.ar(out, Pan2.ar(snd * env, pan));
}, \ir ! 5).add;


/*	
	// tests for the synthdefs:
Synth(\gab1st);
Synth(\percSin);
Synth(\percSin, [\amp, 0.2, \sustain, 0.1]);
Synth(\gab1st, [\out, 0, \amp, 0.2, \freq, 2000, \sustain, 0.05, \pan, 0.5] );
*/
);

