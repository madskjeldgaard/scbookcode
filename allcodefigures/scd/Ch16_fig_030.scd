Figure 16.30: Waittime derived from waveset duration and an added gap 
(
Tdef(\ws1).set(\gap, 3);
Tdef(\ws1, { |ev|
	var startFrame, length, wsSustain, reps; 

	loop { 
		reps = ev.repeats.next;

		#startFrame, length, wsSustain = 
			w.frameFor(ev.startWs.next, ev.numWs.next);			
		
		(instrument: \wvst0, bufnum: b.bufnum, amp: 1, 
			start: startFrame, length:  length, 
			sustain: wsSustain * reps,
			pan: 1.0.rand2
		).play;
		
			// derive waittime from waveset sustain time
			// add gap based on waveset sustain time 
		(wsSustain * (reps + ev.gap.next)).wait;
	}
}).play;
)
	// experiment with dropping in patterns:
	// very irregular gaps
Tdef(\ws1).set(\gap, { exprand(0.1, 20) });
	// sometimes continuous, sometimes gaps
Tdef(\ws1).set(\gap, Pbrown(-10.0, 20, 2.0).max(0).asStream);

	// random repeats
Tdef(\ws1).set(\repeats, { exprand(1, 20).round });
	// randomize number of wavesets per group
Tdef(\ws1).set(\numWs, { exprand(3, 20).round });
Tdef(\ws1).set(\numWs, 3, \repeats, { rrand(2, 5) });

Tdef(\ws1).stop;

