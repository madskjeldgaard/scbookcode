Figure 23.4

// modulating the frequency input to a Pokey UGen results in great variance
(
{ 
	var rate = MouseX.kr(0, 255);
	var mod = LFPulse.kr(1);
	var amp = 2r1100; // 12 of 16
	Pokey.ar(rate + mod, audc1: 2r01000000 + amp);
}.play
);

// modulating the pure tone bit
(
{ 
	var rate = MouseX.kr(0, 255);
	var mod = LFPulse.kr(1);
	var amp = 2r1100; // 12 of 16
	Pokey.ar(rate, audc1: 2r00100000 + (mod * 2r00100000) + amp);
}.play
);


