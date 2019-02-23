
// Figure 2. A Synth Definition with the first 16 harmonics
(
SynthDef(\simpleSynth, {|freq, amp|
	var signal, harmonics;
	harmonics = 16;
	signal = Mix.fill(harmonics, {|i| 
				SinOsc.ar(freq*(i+1), 1.0.rand, amp * harmonics.reciprocal/(i+1)) 
			});
	Out.ar(0, signal ! 2);
}, [0.15, 0.15]).add// lag times so the slider "sounds" better
)

// A line of code testing the synth definition that we created
Synth(\simpleSynth, [\freq, 440, \amp, 1])

