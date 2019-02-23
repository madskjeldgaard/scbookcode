

Figure 7.15

Pdefn(\x, Pseq([0, 2, 0, 7, 6, 5, 4, 3], inf));
(
Task {
	var stream = Pdefn(\x).asStream;
	var val;
	loop { 
		val = stream.next; 
		(instrument: \wave, note: val).play; 
		0.2.wait 
	}
}.play
);

Pdefn(\x, Pseq([0, 2, 0, 8, 6, 5, 2, 3, 4, 5], inf)); // rewrite the definition at runtime.
Pdefn(\x, Pseq([0, 2, 0, 7, 6, 5, 4, 3].scramble + 4, inf));


