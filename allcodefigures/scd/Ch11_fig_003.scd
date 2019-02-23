Figure 11.3
// Tasks 
(
SynthDef("task", { arg out=0, freq=2000;
	var env = EnvGen.kr(Env.perc, 1.0, 0.2, doneAction:2);
	var source = DynKlank.ar(`[ [0.5, 0.8, 1, 1.2, 2, 4]*freq, nil, nil ], PinkNoise.ar(0.007));
	Out.ar(out, source * env ! 2);
}).add;

t = Task({
	var freq;
	loop {
		freq = ((1..10)*220).choose;
		Synth("task", [\freq, freq]);
		0.3.wait;
	}
})
)

t.start;
t.stop;


