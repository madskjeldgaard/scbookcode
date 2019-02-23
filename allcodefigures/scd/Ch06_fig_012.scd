Figure 6.12.  Rendering and playing a pattern
	~pattern = Pbind(*[ 
		instrument:	"default",
		freq: 	Pseq([100, 200, 300, 400, 500, 600, 700, 800, 900, 1000, 1100], 5),
		db:	Pseq([-10, -30, -20, -30], inf),
		dur:	Pseq([0.2,0.2,0.2,0.2, 0.4,0.4,0.8],inf),
		legato: Pseq([2,0.5,0.75,0.5,0.25], inf)	
	] );

	~score = ~pattern.asScore(24 * 11/7);
	~score.render("recordings/test.aif");
	SoundFile("recordings/test.aif").play;

