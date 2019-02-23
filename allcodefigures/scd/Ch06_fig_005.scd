Figure 6.5. A more elaborate Event Pattern
p = Pbind(*[ 
		instrument:	\default,
		detune:		[0,1,3],
		freq: 		Pseq( (1..11) * 100,  4 * 5 * 7),
		db:		Pseq([-20, -40, -30, -40], inf),
		pan:		Pseq([-1,0,1,0], inf),
		dur:		Pseq([0.2,0.2,0.2,0.2, 0.4,0.4,0.8], inf),
		legato:		Pseq([2,0.5,0.75,0.5,0.25], inf)	
	] );
// render 40 seconds of the pattern in the file named "sf.aif"	
	p.render("sounds/sf.aif", 40)	
// now play the pattern in real-time
	p.play;									

