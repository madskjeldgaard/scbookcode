
SynthDef("pingsine", { | out = 0, freq = 400, amp = 0.1, dur = 1|
	var src;
	src = SinOsc.ar(freq, 0, EnvGen.kr(Env.perc(0.1, 0.9, 1, -4), 
		levelScale: amp, timeScale: dur, doneAction: 2));
	Out.ar(out, src);
});

/*
Env.perc(0.7, 0.3, 1, -8).plot
Env.perc(0.7, 0.3, 1, -4).plot
*/