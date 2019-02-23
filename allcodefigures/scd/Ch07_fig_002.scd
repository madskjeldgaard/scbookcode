Figure 7.2
(
{
	x = SinOsc.kr(4); 
	y = SinOsc.kr(13);
	SinOsc.ar(x * y % 0.4 * 500 + 600) * 0.2
}.play;
)

// change x and y?
x = SinOsc.kr(4); // no effect.
y = SinOsc.kr(4); // no effect either.



