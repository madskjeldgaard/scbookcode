/* IZ 0504-05xx 

*/

SynthDef("subtropicdrumming", { |freq = 300, amp = 1.0, dur = 10|
	var out;
	out = RLPF.ar(
		// in 
		LFTri.ar(
			// src freq
			Demand.kr(Impulse.kr([5, 2.51]), 0, 
				Dser((200, 300..1100).scramble, inf)), 0, 0.05
			),
		// rez freq
		LFNoise0.kr([0.2,0.21], freq, freq * 4 / 3),
		// rq, mul 
		0.01, amp);
	Out.ar(0, out * EnvGen.kr(Env([0, 1, 0.1, 0.3, 0], [dur, dur, dur, dur] / 4), doneAction: 2));
}, [\ir,\ir,\ir]);
