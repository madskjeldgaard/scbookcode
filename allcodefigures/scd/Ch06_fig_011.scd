Figure 6.11.  Using Prout to define value and event patterns
~patA = Pbind(*[
	dur:	0.2,
	degree: Prout({ | ev |
		var noteArray  = (0..5);
		loop {
			ev = Pseq(noteArray ).embedInStream(ev);
			noteArray[6.rand] = 7.rand;
		}		
	})
]);


~patB = Prout({ | ev |
	var pat, pats= [
	 	Pbind(*[ degree: Pseq([  0, 7]), dur: 0.2   ]),
	 	Pbind(*[ degree: Pseq([11, 7]), dur: 0.2 ]),
		Pbind(*[ degree: Pseq([16, 7]), dur: 0.2 ]).
		(type: \rest, delta: 1)
	];
			
	loop {
		pat = pats.choose;
		ev = pat.embedInStream(ev);
	}
});

Pchain(
	Pbind(*[
		db: 	Pn(Pstep([-15, -25,-25, -20, -30, -25], 0.2) )
			+ Pseg([-30, -5,-10, -40], 12)
	]), 
	Ptpar([
		0, ~patA, 
		0,  ~patA, 
		12,  ~patB
	]) 
).play;

