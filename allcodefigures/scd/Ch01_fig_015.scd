1.15 Additive Synthesis Examples

// Mix down a few of them tuned to harmonics:

(
{
var fund = 220;
Mix.ar(
	[
	SinOsc.ar(220, mul: max(0, LFNoise1.kr(12))),
	SinOsc.ar(440, mul: max(0, LFNoise1.kr(12)))*1/2,
	SinOsc.ar(660, mul: max(0, LFNoise1.kr(12)))*1/3,
	SinOsc.ar(880, mul: max(0, LFNoise1.kr(12)))*1/4,
	SinOsc.ar(1110, mul: max(0, LFNoise1.kr(12)))*1/5,
	SinOsc.ar(1320, mul: max(0, LFNoise1.kr(12)))*1/6
	]
)*0.3
}.play
)

