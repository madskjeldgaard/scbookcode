

	//	figure 16.22 - a Wavesets object
w = Wavesets.from("sounds/a11wlk01.wav");

w.xings;			// all integer indices of the zero crossings found
w.numXings;		// the total number of zero crossings
w.lengths;		// lengths of all wavesets
w.amps;			// peak amplitude of every waveset
w.maxima;			// index of positive maximum value in every waveset
w.minima;			// index of negative minimum value in every waveset

w.fracXings;		// fractional zerocrossing points
w.fracLengths;	// and lengths: allows more precise looping.
				
w.lengths.plot;	// show distribution of lengths
w.amps.plot;

	// get data for a single waveset: frameIndex, length (in frames), dur
w.frameFor(140, 1);	
w.ampFor(140, 1);		// peak amplitude of that waveset or group
	
	// extract waveset by hand
w.signal.copyRange(w.xings[150], w.xings[151]).plot("waveset 150");
w.plot(140, 1);	// convenience plotting
w.plot(1510, 1);	

	// plot a group of 5 adjacent wavesets
w.plot(1510, 5)	



	// figure 16.23 and 24 are screenshots



	//	figure 16.25 - wavesets and buffers

	// A Synthdef to play a waveset (or group) n times.
(
	// A wavesets loads the file into a buffer by default.
		b = w.buffer;		
	// Wavesets.prepareSynthDefs loads this synthdef: 
		SynthDef(\wvst0, { arg out = 0, buf = 0, start = 0, length = 441, playRate = 1, sustain = 1, amp=0.2, pan; 
			var phasor = Phasor.ar(0, BufRateScale.ir(buf) * playRate, 0, length) + start;
			var env = EnvGen.ar(Env([amp, amp, 0], [sustain, 0]), doneAction: 2);
			var snd = BufRd.ar(1, buf, phasor) * env;
			
			OffsetOut.ar(out, Pan2.ar(snd, pan));
		}, \ir.dup(8)).memStore;
)

// play from frame 0 to 440, looped for 0.1 secs, so ca 10 repeats. 
(instrument: \wvst0, bufnum: b.bufnum, start: 0, length: 440, amp: 1, sustain: 0.1).play;

	// get data from waveset
(
var start, length, sustain, repeats = 20; 
#start, length, sustain = w.frameFor(150, 5);

(	instrument: \wvst0, bufnum: b.bufnum, amp: 1, 
	start: start, length: length, sustain: sustain * repeats
).play;
)

	// or even simpler: 
w.eventFor(startWs: 150, numWs: 5, repeats: 20, playRate: 1).put(\amp, 0.5).play;




	// figure 16.26 - a pattern to play wavesets

	// by default, this pattern reconstructs a soundfile segment.
(
Pbindef(\ws1).clear;
Pbindef(\ws1, 
	\instrument, \wvst0,
	\startWs, Pn(Pseries(0, 1, 3000), 1), 
	\numWs, 1, 
	\playRate, 1, 
	\bufnum, b.bufnum, 
	\repeats, 1, 
	\amp, 0.4,
	[\start, \length, \sustain], Pfunc({ |ev| 
		var start, length, wsDur; 

		#start, length, wsDur = w.frameFor(ev[\startWs], ev[\numWs]);
		[start, length, wsDur * ev[\repeats] / ev[\playRate].abs]
	}), 
	\dur, Pkey(\sustain)
).play;
)



	//	figure 16.27 - some of wishart's transforms

	// waveset transposition: every second waveset, half speed
Pbindef(\ws1, \playRate, 0.5, \startWs, Pn(Pseries(0, 2, 500), 1)).play;

	// reverse every single waveset 
Pbindef(\ws1, \playRate, -1, \startWs, Pn(Pseries(0, 1, 1000), 1)).play;
	// reverse every 2 wavesets
Pbindef(\ws1, \numWs, 2, \playRate, -1, \startWs, Pn(Pseries(0, 2, 1000), 1)).play;
	// reverse every 20 wavesets
Pbindef(\ws1, \numWs, 20, \playRate, -1, \startWs, Pn(Pseries(0, 20, 1000), 1)).play;
	// restore
Pbindef(\ws1, \numWs, 1, \playRate, 1, \startWs, Pn(Pseries(0, 1, 1000), 1)).play;

	// time stretching
Pbindef(\ws1, \playRate, 1, \repeats, 2).play;
Pbindef(\ws1, \playRate, 1, \repeats, 4).play;
Pbindef(\ws1, \playRate, 1, \repeats, 6).play;
Pbindef(\ws1, \repeats, 1).play;	// restore

	// waveset omission: drop every second
Pbindef(\ws1, \numWs, 1, \freq, Pseq([1, \], inf) ).play;
Pbindef(\ws1, \numWs, 1, \freq, Pseq([1,1, \, \], inf) ).play;
Pbindef(\ws1, \numWs, 1, \freq, Pfunc({ if (0.25.coin, 1, \) }) ).play; // drop randomly
Pbindef(\ws1, \numWs, 1, \freq, 1, \startWs, Pn(Pseries(0, 1, 1000)) ).play; // restore

	// waveset shuffling (randomize waveset order +- 5, 25, 125)
Pbindef(\ws1, \startWs, Pn(Pseries(0, 1, 1000), 1) + Pfunc({ 5.rand2 })).play;
Pbindef(\ws1, \startWs, Pn(Pseries(0, 1, 1000), 1) + Pfunc({ 25.rand2 })).play;
Pbindef(\ws1, \startWs, Pn(Pseries(0, 1, 1000), 1) + Pfunc({ 125.rand2 })).play;	





	//	figure 16.28 - waveset substitution
	
	// the waveform to substitute
c = Buffer.alloc(s, 512); c.sendCollection(Signal.sineFill(512, [1]));
(
Pbindef(\ws1).clear;
Pbindef(\ws1, 
	\instrument, \wvst0,
	\startWs, Pn(Pseries(0, 1, 1000), 5), 
	\numWs, 1, \playRate, 1, 
	\buf, c.bufnum, // sine wave
	\repeats, 1, 
	\amp, 1,
	[\start, \length, \sustain], Pfunc({ |ev| 
		var start, length, wsDur, origRate; 
		origRate = ev[\playRate];
		
			// get orig waveset specs
		#start, length, wsDur = w.frameFor(ev[\startWs], ev[\numWs]);

			// adjust playrate for different length of substituted wave
		ev[\playRate] = origRate * (512 / length); 

			// get amplitude from waveset, to scale full volume sine wave
		ev[\amp] = ev[\amp] * w.ampFor(ev[\startWs], ev[\numWs]);
		
		[0, 512, wsDur * ev[\repeats] / origRate.abs]
	}), 
	\dur, Pkey(\sustain)
).play;
)
	// clearer sinewave-ish segments
Pbindef(\ws1, \playRate, 1, \repeats, 2).play;
Pbindef(\ws1, \playRate, 1, \repeats, 6).play;
Pbindef(\ws1).stop;

	// different waveforms
c.sendCollection(Signal.sineFill(512, 1/(1..4).squared.scramble));
c.sendCollection(Signal.rand(512, -1.0, 1.0));
c.sendCollection(Signal.sineFill(512, [1]));

c.plot;




	//	waveset interpolation - web examples only
(
SynthDef("wsInterp", { arg out = 0, 
	buf1 = 0, start1 = 0, len1 = 1000, 
	buf2 = 0, start2 = 0, len2 = 500, 
	playRate = 1, sustain = 1, 
	amp=0.2, pan; 
	
	var lenRatio = (len1 / len2);
	var playRateLine = Line.ar(playRate, playRate * lenRatio, sustain); 
	
	var phasor1 = Phasor.ar(0, BufRateScale.ir(buf1) * playRateLine, 0, len1);
	var phasor2 = phasor1 / lenRatio; 
	var xfade = Line.ar(0, 1, sustain);
	
	var snd = (BufRd.ar(1, [buf1, buf2], 
		[phasor1 + start1, phasor2 + start2], 
		interpolation: 4)
		* [1 - xfade, xfade]).sum;
	
	OffsetOut.ar(out, 
		Pan2.ar(
			snd * EnvGen.ar(Env([amp, amp, 0], [sustain, 0]), doneAction: 2),
		 	pan
	 	)
	 );
}, \ir.dup(12)).memStore;
)

(
q = q ? ();
q.playInterp = { |q, start1, len1, start2, len2, numWs=200| 
	var set1 = w.frameFor(start1, len1).postln;
	var set2 = w.frameFor(start2, len2).postln; 
	var sustain = (set2[2] + set1[2] * 0.5 * numWs).postln;

	(instrument: \wsInterp, buf1: b.bufnum, buf2: b.bufnum, amp: 0.5,
	
		start1: set1[0], len1: set1[1], playRate: 1,
		start2: set2[0], len1: set2[1], sustain: sustain
	).play;	
};
)
	// some interpolations
q.playInterp(200, 1, 500, 1, 400);
q.playInterp(400, 8, 600, 3, 100);
q.playInterp(200, 1, 500, 5, 600);





	//	figure 16.29 - wavesets played with Tdef 
	
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





	// figure 16.30 - waittime from waveset duration, gap
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



	// figure 16.31 - add pitch contour and dropout rate
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


