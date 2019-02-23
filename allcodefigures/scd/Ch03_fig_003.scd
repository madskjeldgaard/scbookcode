
// Figure 3.3 Nesting tasks inside routines
(
r = Routine({	
	c = TempoClock.new; // make a TempoClock
	// start a 'wobbly' loop
	t = Task({	
		loop({	
			x.release(0.1);
			x = Synth(\default, [freq: 61.midicps, amp: 0.2]);
			0.2.wait;
			x.release(0.1);
			x = Synth(\default, [freq: 67.midicps, amp: 0.2]);
			rrand(0.075, 0.25).wait; // random wait from 0.1 to 0.25 seconds
		});
	}, c); // use the TempoClock to play this Task
	t.start;
	nil.yield;
	
	// now add some notes
	y = Synth(\default, [freq: 73.midicps, amp: 0.3]);
	nil.yield;
	y.release(0.1);
	y = Synth(\default, [freq: 79.midicps, amp: 0.3]);
	c.tempo = 2; // double time
	nil.yield;
	t.stop; y.release(1); x.release(0.1); // stop the Task and Synths
});
)

r.next; // start loop
r.next; // first note
r.next; // second note; loop goes 'double time'
r.next; // stop loop and fade







