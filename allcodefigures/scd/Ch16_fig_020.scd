Figure 16.20: A constant-Q Synthdef.

b = Buffer.read(s, "sounds/a11wlk01-44_1.aiff");
(
SynthDef(\constQ, { |out, bufnum=0, amp=0.5, pan, centerPos=0.5, sustain=0.1, 
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
}, \ir.dup(8)).add;
)

Synth(\constQ, [\bufnum, b, \freq, exprand(100, 10000), \rq, exprand(0.01, 0.1), \sustain, 0.01]);

