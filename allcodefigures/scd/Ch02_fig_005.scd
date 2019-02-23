/* Figure 2.5 */
(
SynthDef(\UGen_ex5, {arg gate = 1, seed = 0, id = 1, amp = 1;
	var src, pitchbase, freq, rq, filt, trigger, env;
	RandID.ir(id);
	RandSeed.ir(1, seed);
	env = EnvGen.kr(Env([0, 1, 0], [1, 4], [4, -4], 1), gate, doneAction: 2);
	src = WhiteNoise.ar;
	trigger = Impulse.kr(Rand.new(2, 5));
	pitchbase = IRand.new(4, 9) * 12;
	freq = TIRand.kr(pitchbase, pitchbase + 12, trigger).midicps;
	rq = LFDNoise3.kr(Rand.new(0.3, 0.8)).range(0.01, 0.005);
	filt = Resonz.ar(src, Lag2.kr(freq), rq);
	Out.ar(0, Pan2.ar(filt, LFNoise1.kr(0.1)) * env * amp)
}).add;
)	
a = Synth(\UGen_ex5, [\seed, 123]);

a.release;

// Using the same seed, we get the same gesture
b = Synth(\UGen_ex5, [\seed, 123]);

b.release;

// passing in different seeds
(
r = Routine.run({
	thisThread.randSeed_(123);
	10.do({
		a = Synth(\UGen_ex5, [\seed, 10000.rand.postln, \amp, 3.dbamp]);
		1.wait;
		a.release;
	})
});
)		

