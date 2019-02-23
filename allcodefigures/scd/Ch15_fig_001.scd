Figure 15.1
(
x={
	var in, amp, freq, hasFreq, out;
	in = SoundIn.ar(0);
	amp = Amplitude.ar(in);
	# freq, hasFreq = Pitch.kr(in);
	LFTri.ar(freq*[1,2]) * amp;
}.play
)

x.free;

