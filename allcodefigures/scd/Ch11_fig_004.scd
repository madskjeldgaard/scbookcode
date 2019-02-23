Figure 11.4
// Patterns
(
SynthDef("pattern", { arg out=0, freq=220, pan=0, gate=1;
	var ctl = RLPF.ar(Saw.ar(5, 1.0, 2), 25, 0.03);
 	var source = SinOsc.ar(ctl * freq) * 0.1;
	var env = EnvGen.kr(Env.adsr, gate, doneAction: 2);
	Out.ar(out, Pan2.ar(source * env, pan));
}).add;

e = Pbind(
	\midinote, Pxrand(#[60, 61, 63, 65, 72], 20), 
	\dur, 0.4,
	\pan, Pwhite(-1.0, 1.0, 20),
	\instrument, \pattern
);
)

e.play;


