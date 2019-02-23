Figure 16.10: Changing grain duration, frequency, envelope.

	// figure 16.10 - changing grain duration, frequency, envelope
(
Pbindef(\grain0,
	\instrument, \gabor1, \freq, 500, 
	\sustain, 0.01, \dur, 0.2	
).play;
)
	// change grain durations
Pbindef(\grain0, \sustain, 0.1);
Pbindef(\grain0, \sustain, 0.03);
Pbindef(\grain0, \sustain, 0.01);
Pbindef(\grain0, \sustain, 0.003);
Pbindef(\grain0, \sustain, 0.001);
Pbindef(\grain0, \sustain, Pn(Pgeom(0.1, 0.9, 60)));
Pbindef(\grain0, \sustain, Pfunc({ exprand(0.0003, 0.03) }));
Pbindef(\grain0, \sustain, 0.03);

	// change grain waveform (sine) frequency
Pbindef(\grain0, \freq, 300);
Pbindef(\grain0, \freq, 1000);
Pbindef(\grain0, \freq, 3000);
Pbindef(\grain0, \freq, Pn(Pgeom(300, 1.125, 32)));
Pbindef(\grain0, \freq, Pfunc({ exprand(300, 3000) }));
Pbindef(\grain0, \freq, 1000);

	// change synthdef for different envelopes
Pbindef(\grain0, \instrument, \gabor1);
Pbindef(\grain0, \instrument, \gabWide);
Pbindef(\grain0, \instrument, \percSin);
Pbindef(\grain0, \instrument, \percSinRev);
Pbindef(\grain0, \instrument, \expodec);
Pbindef(\grain0, \instrument, \rexpodec);
Pbindef(\grain0, \instrument, Prand([\gabWide, \percSin, \percSinRev], inf));

