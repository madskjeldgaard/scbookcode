/* (IZ 2005-09-02)
To make the script play a routine, send it the message 'routine' with argument your function <func> to be played in the routine. This will create a start function that plays the <func> in a routine using as clock the clock in ~clock. Default clock is the SystemClock.  
The start/stop button works as with synths.  The routine has access to the environment variables of the script, and thus also to any variables set by controls created by controlSpec. Example:
*/

~script.routine {
	15 do: { |i|
		Post << (i + 1) << " ";
		Synth("nohihat", [\amp, ~amp, \freq, i + 5 * 200]);
		0.25.wait;
	};
	Post.nl;
};

~controlSpecs = [
[\amp, 0, 1, 'linear', 0, 0.3]
];
