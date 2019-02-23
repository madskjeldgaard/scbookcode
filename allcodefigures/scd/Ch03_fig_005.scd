
// Figure 3.5 Thanks to polymorphism we can substitute objects that understand the same message
(
p = 64; // a constant note
q = Pseq([1, 2, 0.5], inf).asStream; // ordered sequence of durations
t = Task({	
	loop({	
		x.release(2);
		x = Synth(\default, [freq: p.value.midicps]);
		q.value.wait;
	});
});
t.start;
)
// now change p
p = Pseq([64, 66, 68], inf).asStream; // to a Pattern: do re mi
p = { rrand(64, 76) }; // to a Function: random notes from a chromatic octave
t.stop; x.release(2);



