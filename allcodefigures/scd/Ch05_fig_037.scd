Figure 5.37
Server.default.boot;
(
SynthDef("ping", { | freq = 440 | 
	Out.ar(0, 
		SinOsc.ar(freq, 0, 
			EnvGen.kr(Env.perc(level: 0.1), doneAction: 2)
	))
}).add;

SynthDef("wham", {
	Out.ar(0, BrownNoise.ar(
		EnvGen.kr(Env.perc(level: 0.1), doneAction: 2)
	))
}).add; 
)


