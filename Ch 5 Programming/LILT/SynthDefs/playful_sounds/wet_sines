/* From psycollider intro
http://www.tranceaddict.com/forums/showthread/t-361228.html

*/
SynthDef('wet_sines', {
	| out = 0, noise_freq = 4, noise_amp = 24,
		saw_freq = 7.23, saw_freq_d = 0.77,
		saw_amp = 3, saw_add = 80,
		comb_time = 0.1 |
	var source, reverb;
	source = SinOsc.ar(
		LFNoise1.kr(noise_freq, noise_amp,
			LFSaw.kr([saw_freq, saw_freq + saw_freq_d], 0, 3, 80)).midicps,
		0, 0.04);
	reverb = CombN.ar(source, 0.2, 0.2, 4);
	Out.ar(out, reverb);
})


/*
(
// select everything between the two parentheses
{
CombN.ar(
SinOsc.ar(
LFNoise1.kr(
4, // LFO
24, // range in MIDI
LFSaw.kr(
[8,7.23],//second LFO
0,
3, // range in MIDI
80 // offset in MIDI
)
).midicps,
0,
0.04
),
0.2, // max delay
0.2, // actual delay
4 // decay
)
}.play
)

*/

