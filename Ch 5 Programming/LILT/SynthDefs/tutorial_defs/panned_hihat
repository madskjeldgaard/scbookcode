/* (IZ 2005-11-20) { 
} */


SynthDef(
	"panned_hihat",
	{ | rate = 1, amp = 0.1, pan = 0, out = 0 |
		var source;
		var panned_source;
		source = WhiteNoise.ar(Decay.kr(Impulse.kr(rate, 0, amp)));
		panned_source = Pan2.ar(source, pan);
		Out.ar(out, panned_source);
	}
);


