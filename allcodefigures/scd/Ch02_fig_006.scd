/* Figure 2.6 */
(
SynthDef(\UGen_ex6, {arg gate = 1, roomsize = 200, revtime = 450;
	var src, env, gverb;
	env = EnvGen.kr(Env([0, 1, 0], [1, 4], [4, -4], 1), gate, doneAction: 2);
	src = Resonz.ar(
			Array.fill(4, {Dust.ar(6)}),
			1760 * [1, 2.2, 3.95, 8.76] + 
				Array.fill(4, {LFNoise2.kr(1, 20)}), 
			0.01).sum * 30.dbamp;
	gverb = GVerb.ar(
		src,
		roomsize, 
		revtime, 
		// feedback loop damping
		0.99,
		// input bw of signal
		LFNoise2.kr(0.1).range(0.9, 0.7),
		// spread 
		LFNoise1.kr(0.2).range(0.2, 0.6),
		// almost no direct source 
		-60.dbamp,
		// some early reflection
		-18.dbamp, 
		// lots of the tail
		3.dbamp,
		roomsize);
	Out.ar(0, gverb * env)
}).add;
)	
a = Synth(\UGen_ex6);

a.release;

