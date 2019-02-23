1.4 VCO, VCF, VCA

(
{
	Blip.ar(
		TRand.kr( // frequency or VCO
			100, 1000, // range
			Impulse.kr(Line.kr(1, 20, 60))), // trigger
		TRand.kr( // number of harmonics or VCF
			1, 10, // range
			Impulse.kr(Line.kr(1, 20, 60))), // trigger
		Linen.kr( // mul, or amplitude, VCA
			Impulse.kr(Line.kr(1, 20, 60)), // trigger
			0, // attack
			0.5, // sustain level
			1/Line.kr(1, 20, 60)) // trigger
		)
}.play
)



