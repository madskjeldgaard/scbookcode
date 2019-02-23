
SynthDef("nohihat", { arg out = 0, freq=3000, dur=0.125, amp=1, noiseLev=0.6;
	var env, src;
	env = Env.perc(dur * 0.001, dur * 0.999, amp, -4);
	src = Resonz.ar(WhiteNoise.ar(noiseLev), freq, XLine.kr(0.2, 0.001, dur));
	Out.ar(out, src * EnvGen.ar(env, doneAction: 2));
}, [\ir, \kr, \ir, \ir]);

/* 

Synth("nohihat");
Synth("nohihat", [\dur, 1]);
Synth("nohihat", [\dur, 10]);
Synth("nohihat", [\freq, 2600, \dur, 3]);
Synth("nohihat", [\freq, 2200, \dur, 2]);
Synth("nohihat", [\freq, 1200, \dur, 0.5]);
Synth("nohihat", [\freq, 1000, \dur, 0.15, \noiseLev, 0.4]);
Synth("nohihat", [\freq, 800, \dur, 5]);

Server.default.sendBundle(1, [\s_new, \nohihat]);

*/

