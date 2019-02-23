Figure 16.29: Wavesets played with a Tdef 
	
	// very simple first pass, fixed repeat time
(
Tdef(\ws1).set(\startWs, 400);
Tdef(\ws1).set(\numWs, 5);
Tdef(\ws1).set(\repeats, 5);

Tdef(\ws1, { |ev|
	var startFrame, length, wsSustain; 

	loop { 
		#startFrame, length, wsSustain = w.frameFor(ev.startWs.next, ev.numWs);

		(instrument: \wvst0, bufnum: b.bufnum, amp: 1, 
			start: startFrame, length:  length, 
			sustain: wsSustain * ev.repeats;
		).play;
		
		0.1.wait;	
	}
}).play;
)

Tdef(\ws1).set(\startWs, 420);
Tdef(\ws1).set(\repeats, 3);
Tdef(\ws1).set(\numWs, 2);

		// drop in a pattern for starting waveset 		
Tdef(\ws1).set(\startWs, Pn(Pseries(0, 5, 400) + 500, inf).asStream);
