Figure 16.31: Wavesets with pitch contour and dropout rate
(
Tdef(\ws1).set(\startWs, Pn(Pseries(0, 5, 400) + 500, inf).asStream);

Tdef(\ws1).set(\gap, 0);
Tdef(\ws1).set(\pitchContour, 0);
Tdef(\ws1).set(\keepCoin, 1.0);
Tdef( 'ws1' ).set( 'repeats' , 5 );
Tdef( 'ws1' ).set( 'numWs' , 3 );

Tdef(\ws1, { |ev|
	var startFrame, length, wsSustain, reps, numWs, len2Avg; 
	var squeezer, playRate;
	loop { 
		reps = ev.repeats.next;
		numWs = ev.numWs.next;
		
		#startFrame, length, wsSustain = 
			w.frameFor(ev.startWs.next, numWs);			
		
		len2Avg = length / numWs / w.avgLength;		
		squeezer = len2Avg ** ev.pitchContour.next;
		wsSustain = wsSustain / squeezer; 
		playRate = 1 * squeezer;

		if (ev.keepCoin.next.coin) { 
			(instrument: \wvst0, bufnum: b.bufnum, amp: 1, 
				start: startFrame, length:  length, 
				sustain: wsSustain * reps,
				playRate: playRate, 
				pan: 1.0.rand2
			).play;
		};
		
		(wsSustain * (reps + ev.gap.next)).wait;
	}
}).play;
)

	// try different pitch Contours:
Tdef(\ws1).set(\pitchContour, 0); 	// original pitch

Tdef(\ws1).set(\pitchContour, 0.5); // flattened contour

		// waveset overtone singing - all equal length
Tdef(\ws1).set(\pitchContour, 1.0); 

		// inversion of contour
Tdef(\ws1).set(\pitchContour, 1.5);
Tdef(\ws1).set(\pitchContour, 2);
Tdef(\ws1).set(\repeats, 3); 

	// waveset omission
Tdef(\ws1).set(\keepCoin, 0.75);
Tdef(\ws1).set(\keepCoin, 1);

	// fade out by omission over 13 secs, pause 2 secs 
Tdef(\ws1).set(\keepCoin, Pn(Penv([1, 0, 0], [13, 2])).asStream).play;

	// add a pitch contour envelope
Tdef(\ws1).set(\pitchContour, Pn(Penv([0, 2, 0], [21, 13])).asStream);

