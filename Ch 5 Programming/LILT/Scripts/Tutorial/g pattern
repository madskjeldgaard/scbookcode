/* (IZ 2005-09-03)
Any playable pattern can be created and returned by the start function. Pass the event-environment of the Script to the pattern in order to use it as protoEvent when playing:
*/

~start = { Pseq([Pbind(
	\degree, Pseq([Pseq([Pseq((-10..20)), Pwhite(10, 20, 10)], 2),
		Pn(\pause, 4), { rrand(-10, 10) } ! 5, Pn(\pause, 3)]),
	\dur, 0.1
), (degree: { rrand(-10, 20) } ! 10, dur: 3)]).play(SystemClock, ~script.envir);
};

/* The above pattern uses the script's event environment as proto-event. This means it has access to environment variables that can be initialized by the script and can be changed at any time. 
*/

~stepsPerOctave = 7;	// use a 7-step-per-octave tuning
~scale = #[0, 2, 3, 5, 6]; // use a custom scale for the 7-step tuning
