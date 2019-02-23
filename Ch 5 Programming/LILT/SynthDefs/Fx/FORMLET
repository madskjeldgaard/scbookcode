
SynthDef(\formlet, { 
	| out = 0, in = 0, freq = 440.0, attacktime = 1.0, decaytime = 1.0, mul = 1.0, add = 0.0 |
	
	Out.ar(out, Formlet.ar(In.ar(in), freq, attacktime, decaytime, mul, add));
})
