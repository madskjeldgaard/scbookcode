Reverb1 {
	*ar { | in, wet = 0.3 , cutoff = 3000|
		var out = in;
		6.do{ out = LPF.ar(AllpassN.ar(out, 0.05, 0.05.rand, 1), cutoff)};
		^(out * wet) + (in * (1 - wet));
	}
}
