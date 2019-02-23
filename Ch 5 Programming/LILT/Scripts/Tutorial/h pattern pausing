/* (IZ 2006-08-06)
In the previous example " 08 pattern", the script will always start the pattern from the beginning, regardless of whether the pattern was previously stopped before its end.  
To make a script resume the pattern a pattern from where it was previously stopped instead of always starting from the beginning, pass the stream generating function to the script as argument to the stream message. 

~script.stream { .... the stream generating function }

Compare this example to 08 pattern, starting and resuming the scripts before the pattern has played to the end. 
*/

~script.stream { Pseq([Pbind(
	\degree, Pseq([Pseq([Pseq((-10..20)), Pwhite(10, 20, 10)], 2),
		Pn(\pause, 4), { rrand(-10, 10) } ! 5, Pn(\pause, 3)]),
	\dur, 0.1
), (degree: { rrand(-10, 20) } ! 10, dur: 3)]).play(SystemClock, ~script.envir);
};

~stepsPerOctave = 7;	// use a 7-step-per-octave tuning
~scale = #[0, 2, 3, 5, 6]; // use a custom scale for the 7-step tuning
