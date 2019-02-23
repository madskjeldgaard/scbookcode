
Figure 7.22
(
Pdef(\x, { |note=0, n=6, step=3, modulo=15, sustain=1|  
	Pbind(
		\instrument, \wave,
		\note, note.value + (Pseries(1, step, n) % modulo) + 7, 
		\dur, sustain.value / n
	) 
})
);

(
Pdef(\a, 
	Pbind(
		\type, \phrase,
		\instrument, \x,
		\note, Pseq([0, 5, 4, 8, 0], inf),
		\n, 5,
		\modulo, Pseq([3, [4, 3, 5], [13, 15]], inf),
		\dur, Pseq([1, 2, 0.5, 1.5], inf)
	)
).play
);

(
Pdef(\a, 
	Pbind(
		\type, \phrase,
		\instrument, \x,
		\note, Pseq([0, 5, 4, 8, 0], inf),
		\n, 5,
		\modulo, Prand([3, [4, 3, 5], [13, 15]], inf),
		\recursionLevel, 1,
		\dur, Pseq([1, 2, 0.5, 1.5], inf) * 2
	)
).play
);



