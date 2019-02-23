
Figure 7.20

(
Pdefn(\sequence, Pseq([[\x, \y], \x, \y, \z, [\x, \b]], inf));
Pdef(\a,
	Pmul(\dur, Pwhite(-0.02, 0.02) + 1,
		Psym(Pdefn(\sequence).trace)
	)
).play;
)

// rewrite the sequence
Pdefn(\sequence, Pseq([\x], inf));
Pdefn(\sequence, Pseq([\x, \y, \x, [\x, \y]], inf));

Pdef(\a).stop; // stop playing




