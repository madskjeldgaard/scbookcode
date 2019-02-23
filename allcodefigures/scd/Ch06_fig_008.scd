Figure 6.8.  Interdependent key values in a Pattern
	Pbind(*[
		stretch:		Pseg([0,0.1,0.2,1],8).linexp(0,1, 1,0.125),
		midinote: 		100.cpsmidi,
		harmonic:		Pwhite(1, 16),
		legato:			Pkey(\stretch) * Pkey(\harmonic)/2 ,
		db:			-10 - Pkey(\harmonic),
		detune:			Pwhite(0.0,3.0),
		dur:			0.2,
	]).play


