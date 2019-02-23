Figure 23.2
 (
{	var f = { | a, b | [a.min(1 - b), b.min(1 - a)] };
	var freq = f.value(MouseX.kr, MouseY.kr) * 400 + 500;
	SinOsc.ar(freq) * 0.1
}.play;
)


(
a = { |freq=100, width=0.5|
	var df, dw;
	df = freq - LastValue.kr(freq);
	dw = width - LastValue.kr(width);
	freq = freq + (dw * 100);
	width = width + (df / 100);
	Pulse.ar(freq, width.clip(0.01, 0.99).poll) * 0.1
}.play;
)

a.set(\freq, exprand(200.0, 600.0));
a.set(\width, 1.0.rand);

