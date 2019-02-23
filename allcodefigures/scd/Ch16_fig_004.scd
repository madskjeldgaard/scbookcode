Figure 16.4: multiple grains fuse into one composite.
	// when the order changes, the sound is subtly different.
(
Pbindef(\grain4, 
	\instrument, \percSin, \sustain, 0.03, \amp, 0.2,
	\freq, Pshuf([1000, 600, 350, 250]), // random every each time
	\dur, 0.005
).play;
				// repeat grain cluster 
Tdef(\grain, { loop { Pbindef(\grain4).play; 1.wait } }).play;
)
	// fixed order
Pbindef(\grain4, \freq, Pseq([1000, 600, 350, 250].scramble));

	// different order every time
Pbindef(\grain4, \freq, Pshuf([1000, 600, 350, 250]));


