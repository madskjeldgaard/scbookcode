Figure 16.12: Control strategies applied to different parameters

(
Pbindef(\grain0).clear;
Pbindef(\grain0, 
	\instrument, \expodec, 
	\freq, 200,
	\sustain, 0.05, \dur, 0.07
).play;
)
	// time-varying freq with envelope pattern
Pbindef(\grain0, \freq, Pn(Penv([200, 1200], [10], \exp), inf));
	// random freq
Pbindef(\grain0, \freq, 400 * Pwhite(-24.0, 24).midiratio);
	// timechanging with random variation
Pbindef(\grain0, \freq, Pn(Penv([400, 2400], [10], \exp), inf) * Pwhite(-24.0, 24).midiratio);

	// panning
Pbindef(\grain0, \pan, Pwhite(-0.8, 0.8));  // random
Pbindef(\grain0, \pan, Pn(Penv([-1, 1], [2]), inf)); // tendency
Pbindef(\grain0, \pan, Pfunc({ |ev| ev.freq.explin(50, 5000, -1, 1) })); // coupled to freq

	// time scattering variants
Pbindef(\grain0, \dur, 0.1 * Pwhite(0.5, 1.5));	// random range
Pbindef(\grain0, \dur, 0.05 * Prand([0, 1, 1, 2, 4], inf)); // rhythmic random

	// amplitude - randomized
Pbindef(\grain0, \amp, Pwhite(0.01, 0.2));	// linear
Pbindef(\grain0, \amp, Pwhite(-50, -14).dbamp); // exponential - more depth
Pbindef(\grain0, \dur, 0.025 * Prand([0, 1, 1, 2, 4], inf)); // could be denser now

	// random amplitude envelopes with Pseg
(
Pbindef(\grain0, 
	\amp, Pseg(
		Pxrand([-50, -20, -30, -40] + 10, inf), // level pattern
		Pxrand([0.5, 1, 2, 3], inf), 		// time pattern
		Prand([\step, \lin], inf)			// curve pattern
	).dbamp
); 
)
	// grain sustain time coupled to freq
Pbindef(\grain0, \sustain, Pkey(\freq).reciprocal * 20).play;

