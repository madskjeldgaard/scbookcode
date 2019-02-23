
/* Figure 2.10 */
(
SynthDef(\UGen_ex10a, {arg gate = 1;
	var src, delay, env;
	env = EnvGen.kr(
		Env([0, 1, 0], [1, 1], \sin, 1), gate, doneAction: 2);
	src = Decay.ar(Impulse.ar(1), 1.0, PinkNoise.ar(0.1));
	delay = CombN.ar(src, 0.1, Line.kr(0.0001, 0.001, 10));
	Out.ar(0, (delay * env).dup);
}).add;

SynthDef(\UGen_ex10b, {arg gate = 1;
	var src, delay, env;
	env = EnvGen.kr(
		Env([0, 1, 0], [1, 1], \sin, 1), gate, doneAction: 2);
	src = Decay.ar(Impulse.ar(1), 1.0, PinkNoise.ar(0.1));
	delay = CombL.ar(src, 0.1, Line.kr(0.0001, 0.001, 10));	
	Out.ar(0, (delay * env).dup);
}).add;

SynthDef(\UGen_ex10c, {arg gate = 1;
	var src, delay, env;
	env = EnvGen.kr(
		Env([0, 1, 0], [1, 1], \sin, 1), gate, doneAction: 2);
	src = Decay.ar(Impulse.ar(1), 1.0, PinkNoise.ar(0.1));
	delay = CombC.ar(src, 0.1, Line.kr(0.0001, 0.001, 10));	
	Out.ar(0, (delay * env).dup);
}).add;
)

a = Synth(\UGen_ex10a); // no interpolation
a.release;

a = Synth(\UGen_ex10b); // linear interpolation
a.release;

a = Synth(\UGen_ex10c); // cubic interpolation
a.release;

(
SynthDef(\UGen_ex10d, {arg gate = 1, deltime = 0.001;
	var src, delay, env;
	env = EnvGen.kr(
		Env([0, 1, 0], [1, 1], \sin, 1), gate, doneAction: 2);
	src = Decay.ar(Impulse.ar(1), 1.0, PinkNoise.ar(0.1));
	delay = CombN.ar(src, 0.1, deltime);
	Out.ar(0, (delay * env).dup);
}).add;

SynthDef(\UGen_ex10e, {arg gate = 1, deltime = 0.001;
	var src, delay, env;
	env = EnvGen.kr(
		Env([0, 1, 0], [1, 1], \sin, 1), gate, doneAction: 2);
	src = Decay.ar(Impulse.ar(1), 1.0, PinkNoise.ar(0.1));
	delay = CombL.ar(src, 0.1, deltime);
	Out.ar(0, (delay * env).dup);
}).add;
	
SynthDef(\UGen_ex10f, {arg gate = 1, deltime = 0.001;
	var src, delay, env;
	env = EnvGen.kr(
		Env([0, 1, 0], [1, 1], \sin, 1), gate, doneAction: 2);
	src = Decay.ar(Impulse.ar(1), 1.0, PinkNoise.ar(0.1));
	delay = CombC.ar(src, 0.1, deltime);
	Out.ar(0, (delay * env).dup);
}).add;
)

// tune to a specific pitch
a = Synth(\UGen_ex10d, [\deltime, 100.midicps.reciprocal]); // no interpolation
a.release;

a = Synth(\UGen_ex10e, [\deltime, 100.midicps.reciprocal]); // linear interpolation
a.release;

a = Synth(\UGen_ex10f, [\deltime, 100.midicps.reciprocal]); // cubic interpolation
a.release;

// a much longer delay
a = Synth(\UGen_ex10d, [\deltime, 0.1]); // no interpolation
a.release;

a = Synth(\UGen_ex10e, [\deltime, 0.1]); // linear interpolation
a.release;

a = Synth(\UGen_ex10f, [\deltime, 0.1]); // cubic interpolation
a.release;

