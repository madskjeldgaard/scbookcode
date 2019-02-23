1.12 Random MIDI Walk

Task({
a = ["C", "C#", "D",  "Eb", "E", "F", "F#", "G", "Ab", "A", "Bb", "B"];
"count, midi, pitch, octave".postln;
	do(50, {arg count;
		p = rrand(36, 72);
		[count, p, a.wrapAt(p), (p/12).round(1) - 1].postln;
	1.wait;
	})
}).play

