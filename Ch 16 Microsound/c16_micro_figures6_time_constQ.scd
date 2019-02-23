				// time-pitch changing // 



		// figure 16.19 time-pitch changing
p = ProxySpace.push(s.boot);
b = Buffer.read(s, "sounds/a11wlk01-44_1.aiff");
(
~timepitch = {arg sndbuf, pitchRatio=1, pitchRd=0.01, grainRate=10, overlap=2, 
	posSpeed=1, posRd=0.01;
	
	var graindur = overlap / grainRate;
	var pitchrate = pitchRatio + LFNoise0.kr(grainRate, pitchRd);
	var position = LFSaw.kr(posSpeed / BufDur.kr(sndbuf)).range(0, 1) 
		+ LFNoise0.kr(grainRate, posRd);
		
	GrainBuf.ar(2, Impulse.kr(grainRate), graindur, sndbuf, pitchrate,
			position, 4, 0, -1)
};
~timepitch.set(\sndbuf, b.bufnum);
~timepitch.play;
);

Spec.add(\pitchRatio, [0.25, 4, \exp]);
Spec.add(\pitchRd, [0, 0.5, \amp]);
Spec.add(\grainRate, [1, 100, \exp]);
Spec.add(\overlap, [0.25, 16, \exp]);
Spec.add(\posSpeed, [-2, 2]);
Spec.add(\posRd, [0, 0.5, \amp]);
NodeProxyEditor(~timepitch, 10);

	// reconstruct original
~timepitch.set(\pitchRatio, 1, \pitchRd, 0, \grainRate, 20, \overlap, 4, \posSpeed, 1, \posRd, 0);

	// four times as long: tweak pitchRd and posJitter to reduce artifacts
~timepitch.set(\pitchRatio, 1, \pitchRd, 0, \grainRate, 20, \overlap, 4, \posSpeed, 0.25, \posRd, 0);

	// random read position, random pitch
~timepitch.set(\pitchRatio, 1, \pitchRd, 0.5, \grainRate, 20, \overlap, 4, \posSpeed, 0.25, \posRd, 0.5);






			// examples constantQ granulation 


	// figure 16.20 - A constant-Q Synthdef.

b = Buffer.read(s, "sounds/a11wlk01-44_1.aiff");
(
SynthDef(\constQ, { |out, bufnum=0, amp=0.1, pan, centerPos=0.5, sustain=0.1, 
	rate=1, freq=400, rq=0.3| 

	var ringtime = (2.4 / (freq * rq) * 0.66).min(0.5); // estimated
	var ampcomp = (rq ** -1) * (400 / freq ** 0.5);	
	var envSig = EnvGen.ar(Env([0, amp, 0], [0.5, 0.5] * sustain, \welch));
	var cutoffEnv = EnvGen.kr(Env([1, 1, 0], [sustain+ringtime,0.01]), doneAction: 2);
	var grain = PlayBuf.ar(1, bufnum, rate, 0, 
		centerPos - (sustain * rate * 0.5) * BufSampleRate.ir(bufnum), 
		1) * envSig;	
	var filtered = BPF.ar( grain, freq, rq, ampcomp ); 

	OffsetOut.ar(out, Pan2.ar(filtered, pan, cutoffEnv))
}, \ir.dup(8)).memStore;
)

Synth(\constQ, [\bufnum, b, \freq, exprand(100, 10000), \rq, exprand(0.01, 0.1), \sustain, 0.01]);




	// parameter tests for constant Q granulation
	
Synth(\constQ, [\bufnum, b]);
Synth(\constQ, [\bufnum, b, \centerPos, 0.5]);	// centerPos = where in soundfile (seconds)
Synth(\constQ, [\bufnum, b, \centerPos, 1]);	
Synth(\constQ, [\bufnum, b, \centerPos, 1.5]);	
	
	// sustain is sustain of exciter grain: 
Synth(\constQ, [\bufnum, b, \centerPos, 0.5, \sustain, 0.01]);
Synth(\constQ, [\bufnum, b, \centerPos, 0.5, \sustain, 0.03]);
Synth(\constQ, [\bufnum, b, \centerPos, 0.5, \sustain, 0.1]);
Synth(\constQ, [\bufnum, b, \centerPos, 0.5, \sustain, 0.3]);

	// filter parameters, rq of bandpass determines ringtime
Synth(\constQ, [\bufnum, b, \freq, 1200, \rq, 1]);
Synth(\constQ, [\bufnum, b, \freq, 1200, \rq, 0.3]);
Synth(\constQ, [\bufnum, b, \freq, 1200, \rq, 0.1]);
Synth(\constQ, [\bufnum, b, \freq, 1200, \rq, 0.03]);
Synth(\constQ, [\bufnum, b, \freq, 1200, \rq, 0.01]);
Synth(\constQ, [\bufnum, b, \freq, 1200, \rq, 0.003]);

	// lower freq rings longer
Synth(\constQ, [\bufnum, b, \freq, 600, \rq, 0.003]);



	// figure 16.21 - a stream of constant Q grains
(
Pbindef(\gr1Q, 
	\instrument, \constQ, \bufnum, b.bufnum,
	\sustain, 0.01, \amp, 0.2,
	\centerPos, Pn(Penv([1, 2.0], [10], \lin)), 
	\dur, Pn(Penv([0.01, 0.09, 0.03].scramble, [0.38, 0.62] * 10, \exp)),
	\rate, Pwhite(0.95, 1.05), 
	\freq, Pbrown(64.0, 120, 8.0).midicps,
	\pan, Pwhite(-1, 1, inf),
	\rq, 0.03
).play;
)
	// changing parameters while playing
Pbindef(\gr1Q, \rq, 0.1);
Pbindef(\gr1Q, \rq, 0.01);
Pbindef(\gr1Q, \sustain, 0.03, \amp, 0.08);
Pbindef(\gr1Q, \freq, Pbrown(80, 120, 18.0).midicps);

Pbindef(\gr1Q, \rq, 0.03);

Pbindef(\gr1Q, \rate, Pn(Penv([1, 2.0], [6], \lin)));

	// variable duration
Pbindef(\gr1Q, \dur, Pwhite(0.01, 0.02));

	// a rhythm that ends
Pbindef(\gr1Q, \dur, Pgeom(0.01, 1.1, 40));

