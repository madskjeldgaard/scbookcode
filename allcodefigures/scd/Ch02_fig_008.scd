/* Figure 2.8 */
(
SynthDef(\UGen_ex8a, {arg gate = 1, freq = 440, amp = 0.1, rate = 0.2;
	var w, x, y, out, env, decode;
	#w, x, y = PanB2.ar(
		SinOsc.ar(freq, 0), LFNoise2.kr(rate));
	env = EnvGen.kr(
		Env([0, 1, 0], [1, 1], \sin, 1), gate, levelScale: amp, doneAction: 2);
	decode = DecodeB2.ar(2, w, x, y);
	Out.ar(0, decode * env)
}).add;
			
SynthDef(\UGen_ex8b, {arg outbus, freq = 440, rate = 0.2;
	var w, x, y;
	#w, x, y = PanB2.ar(
		SinOsc.ar(freq, 0), LFNoise2.kr(rate));
	Out.ar(outbus, [w, x, y])
}).add;
			
SynthDef(\UGen_ex8c, {arg inbus, gate = 1, amp = 0.1;
	var w, x, y, env, decode;
	#w, x, y = In.ar(inbus, 3);
	env = EnvGen.kr(
		Env([0, 1, 0], [1, 1], \sin, 1), gate, levelScale: amp, doneAction: 14);
	decode = DecodeB2.ar(2, w, x, y) * env;
	ReplaceOut.ar(0, decode);
}).add;
)

(
a = Group.new;
250.do({
	Synth(\UGen_ex8a, [\freq, 440.0.rrand(1760.0), \amp, 0.001, \rate, 0.2], a)
});
)
a.release;

(
a = Group.new;
z = Bus.audio(s, 3);

// the 'catch-all' synth for decoding and enveloping
Synth(\UGen_ex8c, [\inbus, z, \amp, 0.001], a, \addToTail); // add it to the tail of the Group containing the encoding synths

250.do({
	Synth(\UGen_ex8b, [\freq, 440.0.rrand(1760.0), \outbus, z, \rate, 0.2], a)
});
)

a.release;	

