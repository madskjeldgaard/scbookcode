1.3 Forbidden Planet 

(
play(
	{
		CombN.ar(
			SinOsc.ar(
				midicps(
					LFNoise1.ar(3, 24, 
						LFSaw.ar([5, 5.123], 0, 3, 80)
					)
				), 
				0, 0.4), 
			1, 0.3, 2)
	}
)
)



