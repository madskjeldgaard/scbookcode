
Figure 7.18

(
(
Pdef(\x,
	Pbind(
		\instrument, \wave,
		\mod, Pseq([1, 0, 1, 0], inf), 
		\dur, Pn(1/2, 8),
		\note, 7
	)
)
);

(
Pdef(\y,
	Pbindf(
		Pdef(\x),
		\amp, 0.2,
		\note, Pshuf([0, 2, 3, 5], 2) + Prand([0, 5, [0, 4]], inf),
		\dur, Pseq([1, 3, 2, 3], inf) / 6
	)
)
);

(
Pdef(\z, Pbindf(Pdef(\y), \dur, 1/4))
);

// the combination of all placeholders into a new placeholder
(
Pdef(\a,
	Pmul(\dur, Pwhite(-0.02, 0.02) + 1,
		Pseq([
			Ppar([Pdef(\x), Pdef(\y)]),
			Pdef(\x),
			Pdef(\y),
			Pdef(\z),
			Ppar([Pdef(\x), Pbindf(Pdef(\y), \ctranspose, 2)])
		], inf)
	)
);
))

Pdef(\a).play; // play it

// go into a looping vamp
(
Pdef(\x,
	Pbind(
		\instrument, \wave, 
		\dur, Pseq([1, 3, 2, Prand([3, 2])], inf) / 6,
		\octave, [6, 4]
	)
)
);

// release a break
(
Pdef(\x,
	Pbind(
		\instrument, \wave, 
		\dur, Pseq([1, 3, 2, Prand([3, 2])], 1) / 6,
		\octave, [6, 4]
	)
)
);

Pdef(\a).stop; // stop the player



