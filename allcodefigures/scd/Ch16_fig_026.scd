Figure 16.26: A pattern to play wavesets

	// by default, this pattern reconstructs a soundfile segment as is.
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

