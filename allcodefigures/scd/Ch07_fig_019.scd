Figure 7.19

// the combination of all placeholders into a new placeholder
(
Pdef(\b, Pbindf(Pdef(\y), \ctranspose, 2));
Pdef(\a,
	Pmul(\dur, Pwhite(-0.02, 0.02) + 1,
		Psym(Pseq([[\x, \y], \x, \y, \z, [\x, \b]], inf).trace) // trace it to post which
	)
).play;
)

