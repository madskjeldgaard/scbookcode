Figure 3.1 A simple routine illustrating a musical use of yield
// Fermata
s.boot;
(
r = Routine({	
	x = Synth(\default, [freq: 76.midicps]);
	1.wait;
	
	x.release(0.1);
	y = Synth(\default, [freq: 73.midicps]);
	"Waiting...".postln;
	nil.yield;// fermata
	
	y.release(0.1);
	z = Synth(\default, [freq: 69.midicps]);
	2.wait;
	z.release;
});
)
// do this then wait for the fermata
r.play;
// feel the sweet tonic... 
r.play; 


