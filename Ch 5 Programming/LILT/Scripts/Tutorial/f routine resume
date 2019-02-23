/* (IZ 2005-09-02)
In the previous example ( 05 routine), the script always starts the routine at the beginning, regardless of whether the routine was previously stopped before its end. To resume a routine from where it stopped each time the script starts again, use ~script.stream. You must also use Task instead of Routine because Task can be paused and resumed, but Routine cannot be paused. 
See also 08b pattern pausing below.

Use the scripts start/stop button to stop the script before the routine ends (before it counts to 15), and then resume it again.   
*/

~script.stream {
	Task({
		15 do: { |i|
			Post << (i + 1) << " ";
			Synth("nohihat", [\amp, ~amp, \freq, i + 5 * 200]);
			0.25.wait;
		};
		Post.nl;
		// Synths and PauseStreams notify the script when they end.
		// Tasks do not notify. Therefore notification must be added here:
		~script.stopped; // Notify script of ended, for update of state and gui
	// Use SystemClock instead of default clock to avoid beat quantization. 
	}, SystemClock).play; // .play required to start on ~script.start.
};

~controlSpecs = [
[\amp, 0, 1, 'linear', 0, 0.3]
];
