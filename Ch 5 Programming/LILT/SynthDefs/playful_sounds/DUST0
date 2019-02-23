


SynthDef("dust0", { |density = 10, amp = 0.1, dur = 1|
	var src, noi, res;
	src = Dust.ar(density, EnvGen.kr(Env.sine(1, 1), 
		levelScale: amp, timeScale: dur, doneAction: 2));
	noi = Decay2.ar(src, 1.5, 5, PinkNoise.ar(LFNoise2.kr(dur/10, 10)));
	res = Ringz.ar(src, LFNoise2.kr(dur / 4, 1000, 1500), LFNoise1.kr(dur/4, 0.2, 0.201)); 
	Out.ar(0, res);
}, [\ir,\ir,\ir]);