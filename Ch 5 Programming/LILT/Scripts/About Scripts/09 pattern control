/* (IZ 2005-09-03)
Interactive controls can be combined with predefined pattern sequences. 
Here the degree is varied with Padd, after being read from envir's ~degree by Pbindf. 

'amp' is bound to a Pseq pattern. 
*/

~controlSpecs = [
	[\degree, 0, 13, \linear, 1, 0],
	[\octave, 0, 9, \linear, 1, 5],
	[\dur, 0.05, 1, \linear, 0, 0.1],
];

~stepsPerOctave = 24.0; // quartertone scale
// modifying the default scale by scaling to 24 degrees per octave and then 
// subtracting some random quartertones
~scale = (~scale * 2) - ({ 2.rand } ! ~scale.size);

// Combining with Padd
~start = {
	// vary the degree of the "main pattern" by adding random choice of [0, 1, 3]
	// Padd modifies the interactively controllable ~degree after it is read by Pbindf!
	Padd(\degree, Prand([0, 1, 3],inf), 
		// this is the main pattern 
		Pbindf(~script.envir, // play the environment
			// vary the amplitude with a Pseq sequence
			\amp, Prand([Pbrown(0.01, 0.2, 0.025, 50), Pseq((0.01,0.02..0.2),2)],inf)
	)).play(SystemClock);
};

