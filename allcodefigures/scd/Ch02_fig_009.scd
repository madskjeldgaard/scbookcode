
/* Figure 2.9 */
(
// pass in amp in db
SynthDef(\UGen_ex9a, {arg gate = 1, freq = 440, amp = 0;
	var src, pos, env;
	src = SinOsc.ar(freq, 0, amp.dbamp);
	env = EnvGen.kr(
		Env([0, 1, 0], [1, 1], \sin, 1), gate, doneAction: 2);
	Out.ar(0, Pan2.ar(src * env, Rand(-1.0, 1.0)));
}).add;
	
// pass in linear amplitude
SynthDef(\UGen_ex9b, {arg gate = 1, freq = 440, amp = 1;
	var src, env;
	src = SinOsc.ar(freq, 0, amp);
	env = EnvGen.kr(
		Env([0, 1, 0], [1, 1], \sin, 1), gate, doneAction: 2);
	Out.ar(0, Pan2.ar(src * env, Rand(-1.0, 1.0)));
}).add;
	
SynthDef(\UGen_ex9c, {arg gate = 1, freq = 440, amp = -3, pos = 0;
	var src, env;
	src = SinOsc.ar(freq, 0, amp);
	env = EnvGen.kr(
		Env([0, 1, 0], [1, 1], \sin, 1), gate, doneAction: 2);
	Out.ar(0, Pan2.ar(src * env, pos));
}).add;
)	

// 45% on my machine
(
a = Group.new;
250.do({
	Synth(\UGen_ex9a, [\freq, 440.0.rrand(1760.0), \amp, -60], a)
});
)
a.release;

// 36%
(
a = Group.new;
250.do({
	Synth(\UGen_ex9b, [\freq, 440.0.rrand(1760.0), \amp, -60.dbamp], a)
});
)
a.release;

// 36% (no difference from b)
(
a = Group.new;
250.do({
	Synth(\UGen_ex9c, [\freq, 440.0.rrand(1760.0), \amp, -60.dbamp, \pos, 1.0.rand2], a)
});
)
a.release;


