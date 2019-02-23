Figure 16.21: A stream of constant-Q grains
(
Pbindef(\gr1Q, 
	\instrument, \constQ, \bufnum, b.bufnum,
	\sustain, 0.01, \amp, 0.2,
	\centerPos, Pn(Penv([1, 2.0], [10], \lin)), 
	\dur, Pn(Penv([0.01, 0.09, 0.03].scramble, [0.38, 0.62] * 10, \exp)),
	\rate, Pwhite(0.95, 1.05), 
	\freq, Pbrown(64.0, 120, 8.0).midicps,
	\pan, Pwhite(-1, 1, inf),
	\rq, 0.03
).play;
)
	// changing parameters while playing
Pbindef(\gr1Q, \rq, 0.1);
Pbindef(\gr1Q, \rq, 0.01);
Pbindef(\gr1Q, \sustain, 0.03, \amp, 0.08);
Pbindef(\gr1Q, \freq, Pbrown(80, 120, 18.0).midicps);

Pbindef(\gr1Q, \rq, 0.03);

Pbindef(\gr1Q, \rate, Pn(Penv([1, 2.0], [6], \lin)));

	// variable duration
Pbindef(\gr1Q, \dur, Pwhite(0.01, 0.02));

	// a rhythm that ends
Pbindef(\gr1Q, \dur, Pgeom(0.01, 1.1, 40));



