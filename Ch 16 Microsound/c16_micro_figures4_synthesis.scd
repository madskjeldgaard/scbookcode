

	// FM grain as synthdef
(
SynthDef(\grainFM0, {|out, carfreq=440, modfreq=200, moddepth = 1, 
	sustain=0.02, amp=0.2, pan|
	
	var env = EnvGen.ar(Env.sine(sustain, amp), doneAction: 2);
	var sound = SinOsc.ar(carfreq, SinOsc.ar(modfreq) * moddepth) * env;
	OffsetOut.ar(out, Pan2.ar(sound, pan))
}, \ir.dup(7)).memStore;
)
(instrument: \grainFM0, sustain: 0.1, amp: 0.2).play;

	// to use buffer envelopes: Osc1 
(
q = q ? ();
q.envbuf = Buffer.sendCollection(s, Env.perc(0.1, 0.9).discretize, 1);

SynthDef(\grainFM1, {|out, envbuf, carfreq=440, modfreq=200, moddepth = 1, 
	sustain=0.02, amp=0.2, pan|
	
	var env = Osc1.ar(envbuf, sustain, doneAction: 2);
	var sound = SinOsc.ar(carfreq, SinOsc.ar(modfreq) * moddepth) * env;
	OffsetOut.ar(out, Pan2.ar(sound, pan, amp))
}, \ir.dup(8)).memStore;
)

(instrument: \grainFM1, sustain: 0.1, envbuf: q.envbuf).play;

(
Pbindef(\fm, 
		\instrument, \grainFM1, 
		\carfreq, Pbrown(300, 1200, 300), 
		\modfreq, 200, 
		\modindex, Pbrown(1.0, 10.0, 2.5), 
		\sustain, 0.1, 
		\dur, 0.1,
		\envbuf, q.envbuf,
		\pan, Pwhite(-0.8, 0.8)
).play;
)




	// GrainBuf as synthdef
(
q = q ? ();
q.envbuf = Buffer.sendCollection(s, Env.perc(0.1, 0.9).discretize, 1);
q.apollo = Buffer.read(s,"sounds/a11wlk01.wav");

SynthDef(\grainBuf1, {|out, envbuf, wavebuf, filepos, rate=1, 
	sustain=0.02, amp=0.2, pan|
	
	var env = Osc1.ar(envbuf, sustain, doneAction: 2);
	var sound = PlayBuf.ar(1, wavebuf, 
		rate * BufRateScale.ir(wavebuf), 1,
		startPos: BufFrames.ir(wavebuf) * filepos) 
		* env;
	
	OffsetOut.ar(out, Pan2.ar(sound, pan, amp))
}, \ir.dup(8)).memStore;
)

(instrument: \grainBuf1, sustain: 0.1, envbuf: q.envbuf, wavebuf: q.apollo).play;

(
Pdef(\buf1, 
	Pbind(
		\instrument, \grainBuf1, 
		\envbuf, q.envbuf, \wavebuf, q.apollo,
		\sustain, 0.1, \dur, 0.05,
		\pan, Pwhite(-0.8, 0.8),
		\filepos, Pn(Pseries(0, 0.01, 100))
	)
).play;
)

q.envbuf.sendCollection(Env.sine.discretize);
q.envbuf.sendCollection(Env.perc.discretize);



	// figure 16.15 - Glisson synthesis
(
SynthDef("glisson", 
	{ arg out = 0, envbuf, freq=800, freq2=1200, sustain=0.001, amp=0.2, pan = 0.0;
		var env = Osc1.ar(envbuf, sustain, 2);
		var freqenv = XLine.ar(freq, freq2, sustain);
		OffsetOut.ar(out, 
			Pan2.ar(SinOsc.ar(freqenv) * env, pan, amp)
		)
}, \ir!7).memStore;
)

(
Tdef(\gliss0, { |e|
	100.do({ arg i;
		s.sendBundle(s.latency, ["/s_new", "glisson", -1, 0, 0, 
			\freq, i % 10 * 100 + 1000,
			\freq2, i % 13 * -100 + 3000,
			 \sustain, 0.05, 
			 \amp, 0.1,
			 \envbuf, q.envbuf.bufnum
		]);
		(3 / (i + 10)).wait;
	});
}).play;
)



/*
	Magnetization patterns can be:
	bidirectional, shallow, 
	bidirectional, deep, 
	unidir, up,
	unidir, down,
	converging to center, 
	diverging from center. 
*/

(
SynthDef(\glisson0, { |out, freq1=440, freq2=660, sustain=0.05, amp=0.2, pan, envwide=0.5| 
	var slopetime = (1 - envwide) * 0.5;
	var sound = SinOsc.ar(XLine.ar(freq1, freq2, sustain));
	var env = EnvGen.ar(
		Env([0, amp, amp, 0], [slopetime, envwide, slopetime], \sin), 
		timeScale: sustain,
		doneAction: 2);
	OffsetOut.ar(out, Pan2.ar(sound * env, pan));
}).memStore; 
)

(
Tdef(\biGliss).set(\fmin, 100, \fmax, 5000, \fratio, 1.0); // non-gliss, wide freq range

Tdef(\biGliss, { |envir|
	
	var f1, f2, temp; 
	inf.do { 
		f1 = exprand(envir.fmin, envir.fmax);
		f2 = f1 * (envir.fratio ** 1.0.rand2);
	//	[f1, f2].postln;
		(	instrument: \glisson0, 
			freq1: f1, 
			freq2: f2,
			pan: 1.0.rand2,
			sustain: 0.05
		).play;
		0.03.wait;
	};
}).play;
)
Tdef(\biGliss).play;
Tdef(\biGliss).set(\fratio, 1.4);	// shallow
Tdef(\biGliss).set(\fratio, 3.0);	// wide


Tdef(\biGliss).set(\fmin, 600, \fmax, 600, \fratio, 1.0);  // fixed
Tdef(\biGliss).set(\fmin, 600, \fmax, 600, \fratio, 1.4);  // diverging, shallow
Tdef(\biGliss).set(\fmin, 600, \fmax, 600, \fratio, 2);	   // diverging

(
Tdef(\biGliss).set(\fmin, 1800, \fmax, 600, \fratio, 2.0);	// converging
Tdef(\biGliss, { |envir|
		
	var f1, f2, temp; 
	500.do { 
		f1 = exprand(envir.fmin, envir.fmax);
		f2 = f1 * (envir.fratio ** 1.0.rand2);
		
		(	instrument: \glisson0, 
			freq1: f2, 	// swap f1 and f2 here for converging
			freq2: f1,
			pan: 1.0.rand2,
			sustain: 0.1
		).play;
		0.1.linrand.wait;
	};
}).play;
)

Tdef(\biGliss).set(\fmin, 1800, \fmax, 2600, \fratio, 2.0);   // converging

