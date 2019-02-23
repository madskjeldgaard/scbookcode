Figure 16.27: Some of Trevor Wishart's transforms

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

