1.9 Connecting controls with a bus

 (
// if these haven't been used they will hold 0
~kbus1 = Bus.control; // a control bus
~kbus2 = Bus.control; // a control bus
{
	var speed, direction;
	speed = In.kr(~kbus1, 1) * 0.2 + 1;
	direction = In.kr(~kbus2);
	PlayBuf.ar(1, ~chooston, (speed * direction), loop: 1);
}.play;
)
(
// now start the controls
{Out.kr(~kbus1, LFNoise0.kr(12))}.play;

{Out.kr(~kbus2, LFClipNoise.kr(1/4))}.play;
)
// Now start the second buffer with the same control input buses, 
// but send it to the right channel using Out.ar(1 etc.

(
{
	var speed, direction;
	speed = In.kr(~kbus1, 1) * 0.2 + 1;
	direction = In.kr(~kbus2);
	Out.ar(1, PlayBuf.ar(1, ~houston, (speed * direction), loop: 1));
}.play;
)

