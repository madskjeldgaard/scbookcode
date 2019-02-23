// figure 16.9 - SynthDefs with different envelopes

(	// a gabor (approx. gaussian-shaped) grain
SynthDef(\gabor1, { |out, amp=0.1, freq=440, sustain=0.01, pan| 
	var snd = FSinOsc.ar(freq);
	var amp2 = amp * AmpComp.ir(freq.max(50)) * 0.5;
	var env = EnvGen.ar(Env.sine(sustain, amp2), doneAction: 2);
	OffsetOut.ar(out, Pan2.ar(snd * env, pan));
}, \ir ! 5).add;

			// wider, quasi-gaussian envelope, with a hold time in the middle. 
SynthDef(\gabWide, { |out, amp=0.1, freq=440, sustain=0.01, pan, width=0.5| 
	var holdT = sustain * width;
	var fadeT = 1 - width * sustain * 0.5;
	var snd = FSinOsc.ar(freq);
	var amp2 = amp * AmpComp.ir(freq.max(50)) * 0.5;
	var env = EnvGen.ar(Env([0, 1, 1, 0], [fadeT, holdT, fadeT], \sin), 
		levelScale: amp2, 
		doneAction: 2);
	OffsetOut.ar(out, Pan2.ar(snd * env, pan));
}, \ir ! 5).add;

			// a simple percussive envelope
SynthDef(\percSin, { |out, amp=0.1, freq=440, sustain=0.01, pan| 
	var snd = FSinOsc.ar(freq);
	var amp2 = amp * AmpComp.ir(freq.max(50)) * 0.5;
	var env = EnvGen.ar(
		Env.perc(0.1, 0.9, amp2), 
			timeScale: sustain, 
			doneAction: 2);
	OffsetOut.ar(out, Pan2.ar(snd * env, pan));
}, \ir ! 5).add;

			// a reversed  percussive envelope
SynthDef(\percSinRev, { |out, amp=0.1, freq=440, sustain=0.01, pan| 
	var snd = FSinOsc.ar(freq);
	var amp2 = amp * AmpComp.ir(freq.max(50)) * 0.5;
	var env = EnvGen.ar(
		Env.perc(0.9, 0.1, amp2), 
			timeScale: sustain, 
			doneAction: 2
		);
	OffsetOut.ar(out, Pan2.ar(snd * env, pan));
}, \ir ! 5).add;

			// an exponential decay envelope
SynthDef(\expodec, { |out, amp=0.1, freq=440, sustain=0.01, pan| 
	var snd = FSinOsc.ar(freq);
	var amp2 = AmpComp.ir(freq.max(50)) * 0.5 * amp;
	var env = XLine.ar(amp2, amp2 * 0.001, sustain, doneAction: 2);
	OffsetOut.ar(out, Pan2.ar(snd * env, pan));
}, \ir ! 5).add;

			// a reversed exponential decay envelope
SynthDef(\rexpodec, { |out, amp=0.1, freq=440, sustain=0.01, pan| 
	var snd = FSinOsc.ar(freq);
	var amp2 = amp * AmpComp.ir(freq.max(50)) * 0.5;
	var env = XLine.ar(amp2 * 0.001, amp2, sustain, doneAction: 2) 
		* (AmpComp.ir(freq) * 0.5);
	OffsetOut.ar(out, Pan2.ar(snd * env, pan));
}, \ir ! 5).add;
)
