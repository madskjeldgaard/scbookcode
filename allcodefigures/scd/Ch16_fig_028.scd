Figure 16.28: Waveset substitution
	
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

