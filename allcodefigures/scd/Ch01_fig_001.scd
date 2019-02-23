1.1 Additive synthesis example

play({
	var sines = 5, speed = 6;
	Mix.fill(sines, 
		{arg x; 
			Pan2.ar(
				SinOsc.ar(x+1*100, 
					mul: max(0, 
						LFNoise1.kr(speed) +
						Line.kr(1, -1, 30)
					)
				), rand2(1.0))})/sines})

