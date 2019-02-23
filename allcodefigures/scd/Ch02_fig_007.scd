/* Figure 2.7 */
(
SynthDef(\UGen_ex7a, {arg gate = 1, freq = 440, amp = 0.1, rate = 0.2;
	var src, pos, env;
	src = SinOsc.ar(freq, 0);
	pos = LFNoise2.ar(rate);
	env = EnvGen.kr(
		Env([0, 1, 0], [1, 1], \sin, 1), gate, levelScale: amp, doneAction: 2);
	Out.ar(0, Pan2.ar(src, pos) * env);
}).add;
			
SynthDef(\UGen_ex7b, {arg gate = 1, freq = 440, amp = 0.1, rate = 0.2;
	var src, pos, env;
	src = SinOsc.ar(freq, 0);
	pos = LFNoise2.kr(rate);
	env = EnvGen.kr(
		Env([0, 1, 0], [1, 1], \sin, 1), gate, levelScale: amp, doneAction: 2);
	Out.ar(0, Pan2.ar(src, pos) * env);
}).add;
	
SynthDef(\UGen_ex7c, {arg gate = 1, freq = 440, amp = 0.1, rate = 0.2;
	var src, pos, env;
	src = SinOsc.ar(freq, 0);
	pos = LFNoise2.kr(rate);
	env = EnvGen.kr(
		Env([0, 1, 0], [1, 1], \sin, 1), gate, levelScale: amp, doneAction: 2);
	Out.ar(0, Pan2.ar(src * env, pos));
}).add;
)

// 56% on my machine
(
a = Group.new;
250.do({
	Synth(\UGen_ex7a, [\freq, 440.0.rrand(1760.0), \amp, 0.001, \rate, 0.2], a)
});
)
a.release;

// 39%
(
a = Group.new;
250.do({
	Synth(\UGen_ex7b, [\freq, 440.0.rrand(1760.0), \amp, 0.001, \rate, 0.2], a)
});
)
a.release;

// 35%
(
a = Group.new;
250.do({
	Synth(\UGen_ex7c, [\freq, 440.0.rrand(1760.0), \amp, 0.001, \rate, 0.2], a)
});
)
a.release;

