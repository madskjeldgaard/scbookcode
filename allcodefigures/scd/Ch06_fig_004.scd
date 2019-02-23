Figure 6.4. Two ways of writing the same Event Pattern
	1. Pbind( *[ 
		dur:	0.2,
freq: 	Pseq([100, 200, 300, 400, 500, 600, 700, 800]) 
	] );

	2. Pbind( 
		\dur, 0.2,
		\freq, 	Pseq([100, 200, 300, 400, 500, 600, 700, 800]) 
	);

