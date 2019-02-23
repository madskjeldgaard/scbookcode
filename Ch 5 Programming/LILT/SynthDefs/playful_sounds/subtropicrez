// Note 050710: Not pleased with this synthdef but leave it as an experiment

SynthDef("subtropicrez", { |srcFreq = 300, rezFreq = 300, rq = 0.05, amp = 0.1, dur = 10|
	var out;
	out = RLPF.ar(
		LFTri.ar(srcFreq), 		// in
		[rezFreq, rezFreq], 		// rezFreq 
		rq, amp);				// rq, mul
	Out.ar(0, out * EnvGen.kr(Env([0, 1, 1, 0], [0.01, dur-0.89, 0.1]), doneAction: 2));
}, [\kr, \kr, \kr, \kr, \ir]);

/* ///////////////////////////////////////////////////////////////////////////////////////////
see subtropic drumming

You need to drive this synth by setting different freqs with 
	Synth.set(\srcFreq, <some freq>); // for the source frequency
and 
	Synth.set(\rezFreq, <some freq>); // for the resonance frequency

(
a = Synth("subtropicrez", [\dur, 30, \rq, 0.02]);
Routine({
	var freqs, srcFreqStream, rezFreqStream;
	freqs = (200, 300 .. 1200);
	srcFreqStream = Pseq(freqs, inf).asStream;
	rezFreqStream = Pstutter(freqs.size, Pseq(freqs * 1.1, inf)).asStream; 
	(freqs.size.squared).do {
		a.set(\srcFreq, srcFreqStream.next);
		a.set(\rezFreq, rezFreqStream.next);
		0.2.wait;
	};
	15.do {
		a.set(\rezFreq, srcFreqStream.next);
		0.2.wait;
	};
}).play;
)

(
a = Synth("subtropicrez", [\dur, 30]);
Routine({
	var freqs, rezFreqStream;
	freqs = (200, 300 .. 1100);
	rezFreqStream = Pstutter(15, Pseq(freqs.scramble, inf)).asStream; 
	100.do {
		a.set(\srcFreq, freqs.choose);
		a.set(\rezFreq, rezFreqStream.next);
		0.2.wait;
	};
}).play;
)


*/