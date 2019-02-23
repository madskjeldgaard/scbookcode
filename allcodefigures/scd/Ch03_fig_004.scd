// Figure 3.4 Using patterns within a task

(// random notes from lydian b7 scale
p = Pxrand([64, 66, 68, 70, 71, 73, 74, 76], inf).asStream; 
// ordered sequence of durations
q = Pseq([1, 2, 0.5], inf).asStream; 
t = Task({	
	loop({	
		x.release(2);
		x = Synth(\default, [freq: p.value.midicps]);
		q.value.wait;
	});
});
t.start;
)
t.stop; x.release(2);

