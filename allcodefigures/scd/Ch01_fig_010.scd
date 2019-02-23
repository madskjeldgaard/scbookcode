1.10 Buffer Modulation

(
{
	Out.ar(0,
		Pan2.ar( PlayBuf.ar(1, ~houston, loop: 1) * 
			SinOsc.ar(LFNoise0.kr(12, mul: 500, add: 600)),
		0.5)
	)
}.play
)

(
{
var source, delay; 
	source = PlayBuf.ar(1, ~chooston, loop: 1);
	delay =  AllpassC.ar(source, 2, [0.65, 1.15], 10);
	Out.ar(0,
	Pan2.ar(source) + delay
	)
}.play
)



