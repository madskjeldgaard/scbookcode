// Figure 12.3
//
// select the following lines and type C-c C-x
// stop synthesis with C-c C-s

{
	var trem = LFTri.kr(3).range(-12.dbamp, 0.dbamp);
	SinOsc.ar(
		{ rrand(60, 80).midicps } ! 6,
		mul: -10.dbamp * trem
	).clump(2).sum
}.play


// Figure 12.4
//
// evaluate the following lines by double-clicking on the
// first paranthesis when `transient-mark-mode' is enabled
// and pressing C-c C-c

(
x = {
	Saw.ar(
		XLine.kr(20, [400,403,407], 20),
		mul: 0.7
	).clump(2).sum
}.play; "scel";
)

// evaluate this line by pressing C-c C-c
x.free; "rocks";
