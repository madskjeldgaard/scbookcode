1.21 SinOsc Offset and Scaled for Control

(
	{
	var trigger, control, scale, offset;
	scale = 300; // try other values, but not greater than offset
	offset = 600; // try other values
	trigger = Impulse.kr(10);
	control = SinOsc.ar(1/4).scope("control"); // LFO
	control = control  * scale + offset;
	SinOsc.ar(freq: abs(control).poll).scope("audio")
	}.play
) 

// Speaking of vibrato, I can't resist this faux Theremin: 

{SinOsc.ar(SinOsc.ar(8, 0, 10, MouseX.kr(440, 1760, 1)))}.play


