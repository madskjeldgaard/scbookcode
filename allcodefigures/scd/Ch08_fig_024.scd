
	// figure 8.24 - global setup and a player Tdef for the cloud.
(
q = q ? (); 

	// some globals
q.paramRNames = [\freqRange, \durRange, \densRange, \ampRange, \panRange]; 
q.paramNames = [\freq, \grDur, \dens, \amp, \pan]; 
q.syndefNames = [\gab1st, \gabWide, \percSin, \percSinRev, \percNoise];

	// specs for some parameters
Spec.add(\xfadeTime, [0.001, 1000, \exp]);
Spec.add(\ring, [0.03, 30, \exp]);
Spec.add(\grDur, [0.0001, 1, \exp]);
Spec.add(\dens, [1, 1000, \exp]);

	// make an empty tdef that plays it, 
	// and put the cloud parameter ranges in the tdef's environment
Tdef(\cloud0)
	.set(
	\synName, \gab1st,
	\vol, 0.25,
	\current, (
		freqRange: [200, 2000],
		ampRange: [0.1, 1],
		durRange: [0.001, 0.01], 
		densRange: [1, 1000],
		panRange: [-1.0, 1.0] 
	)
); 
		
		// make the tdef that plays the cloud of sound particles here, 
		// based on parameter range settings.
Tdef(\cloud0, { |e| 

	loop {
		s.sendBundle(s.latency, [
			"/s_new", e.synName ? \gab1st, 
			-1, 0, 0,
			\freq, 	exprand(e.current.freqRange[0], e.current.freqRange[1]),
			\amp,	exprand(e.current.ampRange[0], e.current.ampRange[1]) * e.vol,
			\sustain,	exprand(e.current.durRange[0], e.current.durRange[1]),
			\pan, 	rrand(e.current.panRange[0], e.current.panRange[1])
		]);
		exprand(e.current.densRange[0].reciprocal, e.current.densRange[1].reciprocal).wait; 
	}
}).quant_(0);	
);



