Figure 6.9. Chaining Event Patterns
	a = Pbind(*[
		scale: 	Pn( Pstep([[0,2,4,5,7,9,11], [0,1,3,5,6,8,11] ], 5 ) ),
		db:	Pn(Pseg([-20, -30, -25, -30], 0.4))
	]);
	b = Pbind(*[ 
degree: Pbrown(0, 6, 1), 
mtranspose: Prand([\rest, Pseq([0], 5.rand)],inf), 
dur: 0.2, 
octave: 6
]);
	c = Pbind(*[ 
degree: [0,2,4],  
mtranspose: Pbrown(0, 6, 1), 
dur: 0.4, 
db: -35
]);
	d = Pchain(Ppar([b, c]),a);
d.play;

