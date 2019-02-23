			// microsound - code figures - anatomy //


	// waveform, envelope, grain plotted

e = Env.sine.asSignal(400).as(Array); 
w = Array.fill(400, { |i| (i * 2pi / 40).sin });
g = e * w;	

[e, w, g].flop.flat.plot("envelope, wave, grain", Rect(0,0,408,600), numChannels: 3);


	// a gaussian envelope created with the LFGauss UGen
{ LFGauss.ar(0.01, 0.26) }.plot;

	// and a curve created from the gaussian distribution formula
(-1, -0.995 .. 1).collect { |x| exp(squared(x) / (-2.0 * squared(0.26))) }.plot;


	// a gabor grain and tests

(
SynthDef(\gabor0, {|out, freq=440, sustain=0.02, amp=0.2, pan|
	var env = EnvGen.ar(Env.sine(sustain, amp), doneAction: 2);
	var sound = SinOsc.ar(freq) * env;
	OffsetOut.ar(out, Pan2.ar(sound, pan))
}, \ir.dup(5)).memStore;
)
		// test with synth
Synth(\gabor0);		// defaults from SynthDef
Synth(\gabor0, [\freq, 1000, \sustain, 0.005, \amp, 0.1, \pan, 0.5]);

		// test with event.play
(instrument: \gabor0).play;		// default values from Event.defaultEvent
(instrument: \gabor0, sustain: 0.02).play;
(instrument: \gabor0, sustain: 0.002, freq: 1500, amp: 0.3, pan: 0.5).play;
(instrument: \gabor0, sustain: 0.001, freq: 2500, amp: 0.05, pan: -0.5).play;

Synth.grain(\gabor0, [\freq, 2000, \sustain, 0.003]) // higher efficiency, as no NodeID is kept

s.sendMsg("s_new",  \gabor0, -1, 0, 0, \freq, 2000, \sustain, 0.003); // even more efficient, as no Synth object is created.



	// figure 16.6 - making different envelope shapes

Env.sine.plot;		// approx. gaussian 
Env([0, 1, 1, 0], [0.25, 0.5, 0.25] * 0.1, \sin).test.plot; // quasi-gaussian
Env([0, 1, 1, 0], [0.25, 0.5, 0.25] * 0.1, \lin).test.plot; // 3 stage line segments.
Env([0, 1, 1, 0], [0.25, 0.5, 0.25] * 0.1, \welch).test.plot; // welch curve interpolation
Env([1, 0.001], [0.1], \exp).test.plot;	// expoDec (exponential decay);
Env([0.001, 1], [0.1], \exp).test.plot;	// revExpoDec (reverse exponential decay);
Env.perc(0.01, 0.09).test.plot;

(	// a sinc function envelope 
q = q ? ();
q.makeSinc = { |q, num=1, size=400| 
	dup({ |x| x = x.linlin(0, size-1, -pi, pi) * num; sin(x) / x }, size);
}; 
a = q.makeSinc(6);
a.plot(bounds: Rect(0,0,409,200), minval: -1, maxval: 1);
)






(	// more envelopes plotted 
[ 	Env.sine, 
	Env([0, 1, 1, 0], [0.33, 0.34, 0.33], \sin), 
	Env([0, 1, 1, 0], [0.33, 0.34, 0.33], \lin),
	Env([0, 1, 1, 0], [0.33, 0.34, 0.33], \welch),
	Env([1, 0.001], [1], \exp),
	Env([0.001, 1], [1], \exp), 
	Env.perc(0.05, 0.95)
]	.collect(_.discretize(400))
	.add(q.makeSinc(6)).clump(4).collect { |gr4, i| 
		gr4.flop.flat.plot(
			["gauss, quasi-gauss, line, welch", 
			"expodec, rexpodec, perc, sinc" ][i], 
			Rect(420 * i + 100, 300, 408, 400), numChannels: 4)
	};
)



	// figure 16.9 - SynthDefs with different envelopes

(	// a gabor (approx. gaussian-shaped) grain
SynthDef(\gabor1, { |out, amp=0.1, freq=440, sustain=0.01, pan| 
	var snd = FSinOsc.ar(freq);
	var amp2 = amp * AmpComp.ir(freq.max(50)) * 0.5;
	var env = EnvGen.ar(Env.sine(sustain, amp2), doneAction: 2);
	OffsetOut.ar(out, Pan2.ar(snd * env, pan));
}, \ir ! 5).memStore;

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
}, \ir ! 5).memStore;

			// a simple percussive envelope
SynthDef(\percSin, { |out, amp=0.1, freq=440, sustain=0.01, pan| 
	var snd = FSinOsc.ar(freq);
	var amp2 = amp * AmpComp.ir(freq.max(50)) * 0.5;
	var env = EnvGen.ar(
		Env.perc(0.1, 0.9, amp2), 
			timeScale: sustain, 
			doneAction: 2);
	OffsetOut.ar(out, Pan2.ar(snd * env, pan));
}, \ir ! 5).memStore;

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
}, \ir ! 5).memStore;

			// an exponential decay envelope
SynthDef(\expodec, { |out, amp=0.1, freq=440, sustain=0.01, pan| 
	var snd = FSinOsc.ar(freq);
	var amp2 = AmpComp.ir(freq.max(50)) * 0.5 * amp;
	var env = XLine.ar(amp2, amp2 * 0.001, sustain, doneAction: 2);
	OffsetOut.ar(out, Pan2.ar(snd * env, pan));
}, \ir ! 5).memStore;

			// a reversed exponential decay envelope
SynthDef(\rexpodec, { |out, amp=0.1, freq=440, sustain=0.01, pan| 
	var snd = FSinOsc.ar(freq);
	var amp2 = amp * AmpComp.ir(freq.max(50)) * 0.5;
	var env = XLine.ar(amp2 * 0.001, amp2, sustain, doneAction: 2) 
		* (AmpComp.ir(freq) * 0.5);
	OffsetOut.ar(out, Pan2.ar(snd * env, pan));
}, \ir ! 5).memStore;
)





	// figure 16.10 - changing grain duration, frequency, envelope
(
Pbindef(\grain0,
	\instrument, \gabor1, \freq, 500, 
	\sustain, 0.01, \dur, 0.2	
).play;
)
	// change grain durations
Pbindef(\grain0, \sustain, 0.1);
Pbindef(\grain0, \sustain, 0.03);
Pbindef(\grain0, \sustain, 0.01);
Pbindef(\grain0, \sustain, 0.003);
Pbindef(\grain0, \sustain, 0.001);
Pbindef(\grain0, \sustain, Pn(Pgeom(0.1, 0.9, 60)));
Pbindef(\grain0, \sustain, Pfunc({ exprand(0.0003, 0.03) }));
Pbindef(\grain0, \sustain, 0.03);

	// change grain waveform (sine) frequency
Pbindef(\grain0, \freq, 300);
Pbindef(\grain0, \freq, 1000);
Pbindef(\grain0, \freq, 3000);
Pbindef(\grain0, \freq, Pn(Pgeom(300, 1.125, 32)));
Pbindef(\grain0, \freq, Pfunc({ exprand(300, 3000) }));
Pbindef(\grain0, \freq, 1000);

	// change synthdef for different envelopes
Pbindef(\grain0, \instrument, \gabor1);
Pbindef(\grain0, \instrument, \gabWide);
Pbindef(\grain0, \instrument, \percSin);
Pbindef(\grain0, \instrument, \percSinRev);
Pbindef(\grain0, \instrument, \expodec);
Pbindef(\grain0, \instrument, \rexpodec);
Pbindef(\grain0, \instrument, Prand([\gabWide, \percSin, \percSinRev], inf));





	// bonus track - adjusting phase for attack color
	
(	// an expodec envelope sine grain with adjustable phase
SynthDef(\expodecPH, { |out, amp=0.1, freq=440, click=0, sustain=0.01, pan| 
	var snd = FSinOsc.ar(freq, click * 0.5pi);
	var env = XLine.ar(amp, amp * 0.001, sustain, doneAction: 2) * (AmpComp.ir(freq) * 0.5);
	OffsetOut.ar(out, Pan2.ar(snd * env, pan));
}, \ir ! 6).memStore;
)
(
Pbindef(\grain0).play;
Pbindef(\grain0, 
	\instrument, \expodecPH, 
	\sustain, 0.1, 
	\freq, [100, 300],
	\click, Pseq((0..20)/20, inf) // add more and more click 
).play;
)






	// figure 16.11 - different control strategies applied to density
	
(	// synchronous - regular time intervals
Pbindef(\grain0).clear;
Pbindef(\grain0).play;
Pbindef(\grain0, 
	\instrument, \expodec, 
	\freq, Pn(Penv([200, 1200], [10], \exp), inf),
	\dur, 0.1, \sustain, 0.06
);
)
	// different fixed values
Pbindef(\grain0, \dur, 0.06) 	// rhythm
Pbindef(\grain0, \dur, 0.035)	
Pbindef(\grain0, \dur, 0.02)	// fundamental frequency 50 Hz

	// time-changing values: accelerando/ritardando
Pbindef(\grain0, \dur, Pn(Penv([0.1, 0.02], [4], \exp), inf));
Pbindef(\grain0, \dur, Pn(Penv([0.1, 0.02, 0.06, 0.01].scramble, [3, 2, 1], \exp), inf));

	// repeating values: rhythms or tones
Pbindef(\grain0, \dur, Pstutter(Pwhite(2, 15), Pfunc({ exprand(0.01, 0.3) })));

	// introducing irregularity - quasi-synchronous
Pbindef(\grain0, \dur, 0.03 * Pwhite(0.8, 1.2))
Pbindef(\grain0, \dur, 0.03 * Pbrown(0.6, 1.4, 0.1)) // slower drift
Pbindef(\grain0, \dur, 0.03 * Pwhite(0.2, 1.8))

	// average density constant, vary degree of irregularity  
Pbindef(\grain0, \dur, 0.02 * Pfunc({ (0.1.linrand * 3) + 0.9 }));
Pbindef(\grain0, \dur, 0.02 * Pfunc({ (0.3.linrand * 3) + 0.3 }));
Pbindef(\grain0, \dur, 0.02 * Pfunc({ (1.0.linrand * 3) + 0.0 }));
Pbindef(\grain0, \dur, 0.02 * Pfunc({ 2.45.linrand.squared })); // very irregular


(	// coupling - duration depends on freq parameter
Pbindef(\grain0, 
	\freq, Pn(Penv([200, 1200], [10], \exp), inf),
	\dur, Pfunc({ |ev| 20 / ev.freq  })
);
)

	// different freq movement, different timing
Pbindef(\grain0, \freq, Pbrown(48.0, 96.0, 12.0).midicps);

(	// duration depends on freq, with some variation - tendency mask
Pbindef(\grain0, 
	\freq, Pn(Penv([200, 1200], [10], \exp), inf),
	\dur, Pfunc({ |ev| 20 / ev.freq * rrand(0.5, 1.5)  })
);
)


	// figure 16.12 - control strategies applied to different parameters
(
Pbindef(\grain0).clear;
Pbindef(\grain0, 
	\instrument, \expodec, 
	\freq, 200,
	\sustain, 0.05, \dur, 0.07
).play;
)
	// time-varying freq with envelope pattern
Pbindef(\grain0, \freq, Pn(Penv([200, 1200], [10], \exp), inf));
	// random freq
Pbindef(\grain0, \freq, 400 * Pwhite(-24.0, 24).midiratio);
	// timechanging with random variation
Pbindef(\grain0, \freq, Pn(Penv([400, 2400], [10], \exp), inf) * Pwhite(-24.0, 24).midiratio);

	// panning
Pbindef(\grain0, \pan, Pwhite(-0.8, 0.8));  // random
Pbindef(\grain0, \pan, Pn(Penv([-1, 1], [2]), inf)); // tendency
Pbindef(\grain0, \pan, Pfunc({ |ev| ev.freq.explin(50, 5000, -1, 1) })); // coupled to freq

	// time scattering variants
Pbindef(\grain0, \dur, 0.1 * Pwhite(0.5, 1.5));	// random range
Pbindef(\grain0, \dur, 0.05 * Prand([0, 1, 1, 2, 4], inf)); // rhythmic random

	// amplitude - randomized
Pbindef(\grain0, \amp, Pwhite(0.01, 0.2));	// linear
Pbindef(\grain0, \amp, Pwhite(-50, -14).dbamp); // exponential - more depth
Pbindef(\grain0, \dur, 0.025 * Prand([0, 1, 1, 2, 4], inf)); // could be denser now

	// random amplitude envelopes with Pseg
(
Pbindef(\grain0, 
	\amp, Pseg(
		Pxrand([-50, -20, -30, -40] + 10, inf), // level pattern
		Pxrand([0.5, 1, 2, 3], inf), 		// time pattern
		Prand([\step, \lin], inf)			// curve pattern
	).dbamp
); 
)
	// grain sustain time coupled to freq
Pbindef(\grain0, \sustain, Pkey(\freq).reciprocal * 20).play;
