Figure 12.3
// select the following lines and type C-c C-d
// stop synthesis with C-c C-s
{
	var trem = LFTri.kr(3).range(-12.dbamp, 0.dbamp);
	SinOsc.ar(
		{ rrand(60, 80).midicps } ! 6,
		mul: -10.dbamp * trem
	).clump(2).sum
}.play

