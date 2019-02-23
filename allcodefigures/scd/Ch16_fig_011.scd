Figure 16.11: Different control strategies applied to density.

(	// synchronous - regular time intervals
Pbindef(\grain0).clear;
Pbindef(\grain0).play;
Pbindef(\grain0, 
	\instrument, \expodec, 
	\freq, Pn(Penv([200, 1200], [10], \exp), inf),
	\dur, 0.1, \sustain, 0.06
);
)
	// different fixed values
Pbindef(\grain0, \dur, 0.06) 	// rhythm
Pbindef(\grain0, \dur, 0.035)	
Pbindef(\grain0, \dur, 0.02)	// fundamental frequency 50 Hz

	// time-changing values: accelerando/ritardando
Pbindef(\grain0, \dur, Pn(Penv([0.1, 0.02], [4], \exp), inf));
Pbindef(\grain0, \dur, Pn(Penv([0.1, 0.02, 0.06, 0.01].scramble, [3, 2, 1], \exp), inf));

	// repeating values: rhythms or tones
Pbindef(\grain0, \dur, Pstutter(Pwhite(2, 15), Pfunc({ exprand(0.01, 0.3) })));

	// introducing irregularity - quasi-synchronous
Pbindef(\grain0, \dur, 0.03 * Pwhite(0.8, 1.2))
Pbindef(\grain0, \dur, 0.03 * Pbrown(0.6, 1.4, 0.1)) // slower drift
Pbindef(\grain0, \dur, 0.03 * Pwhite(0.2, 1.8))

	// average density constant, vary degree of irregularity  
Pbindef(\grain0, \dur, 0.02 * Pfunc({ (0.1.linrand * 3) + 0.9 }));
Pbindef(\grain0, \dur, 0.02 * Pfunc({ (0.3.linrand * 3) + 0.3 }));
Pbindef(\grain0, \dur, 0.02 * Pfunc({ (1.0.linrand * 3) + 0.0 }));
Pbindef(\grain0, \dur, 0.02 * Pfunc({ 2.45.linrand.squared })); // very irregular


(	// coupling - duration depends on freq parameter
Pbindef(\grain0, 
	\freq, Pn(Penv([200, 1200], [10], \exp), inf),
	\dur, Pfunc({ |ev| 20 / ev.freq  })
);
)

	// different freq movement, different timing
Pbindef(\grain0, \freq, Pbrown(48.0, 96.0, 12.0).midicps);

(	// duration depends on freq, with some variation - tendency mask
Pbindef(\grain0, 
	\freq, Pn(Penv([200, 1200], [10], \exp), inf),
	\dur, Pfunc({ |ev| 20 / ev.freq * rrand(0.5, 1.5)  })
);
)


