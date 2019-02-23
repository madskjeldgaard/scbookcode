Figure 7.16

Pdefn(\y, Pdefn(\x) + 2); // derive a transposition
Pdefn(\z, Pdefn(\x) + Pseq([0, 5, 0, 7, 2], inf)); // derive a variation
Pdefn(\a, Ptuple([Pdefn(\y), Pdefn(\z)])); // combine them in a stream of arrays
(
Task {
	var stream = Pdefn(\a).asStream;
	var val;
	loop { 
		val = stream.next.postln; 
		(instrument: \wave, note: val, sustain: rrand(0.5, 0.9)).play; 
		0.2.wait 
	}
}.play
);

// rewriting the definitions causes all derivations to vary
Pdefn(\x, Pseq([0, 11], inf));
Pdefn(\x, Pseq([0, 2, 0, 7, 6, 5, 4, 3].scramble + 5, inf));
Pdefn(\z, Pdefn(\x) + Pseq([1, 5, 1, 11, 1], inf)); // change a variation

Pdefn(\a, 5); // a number as a source
Pdefn.clear; // clearing all - the empty pattern returns a series of 1. 


