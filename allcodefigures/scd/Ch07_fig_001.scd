
Figure 7.1
(
Task {
	x = 4; y = 13;
	loop {
		x = (x * y) % 11;
		(note: x.postln, dur: 0.125).play;
		0.125.wait;
	}
}.play
);		// creates a loop of values

// change x and y
x = 5;	// new initial value
y = 4;	// new multiplication factor



