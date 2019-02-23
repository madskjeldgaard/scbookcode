/* IZ 050723
From Groups.rtf help file
Version with more controls, no pan and only one out: 
*/

SynthDef("decaysin", { arg out = 0, freq = 440, amp = 0.1, 
	pulse = 0.5, attack = 0.3, decay = 1.0, 
	vibfreq = 0.2, vibamp = 110;
	var source;
	source = Decay2.ar(
	// pulse to create envelope from Decay2
	Impulse.ar(pulse),
	// attack, decay of envelope shape
		attack, decay, 
	// mul = the sound
		SinOsc.ar(SinOsc.kr(vibfreq, 0, vibamp, freq), 0, amp)
	);
	Out.ar(out, source);
})

/* 
// (almost) original version - only the out bus names altered here: 
SynthDef("decaysin", { arg out = 0, directOut = 0, direct = 0.5, freq = 440;
	var source;
	// 1.0.rand2 returns a random number from -1 to 1, used here for a random pan
	source = Pan2.ar(Decay2.ar(Impulse.ar(Rand(0.3, 1), 0, 0.125), 0.3, 1, 
		SinOsc.ar(SinOsc.kr(0.2, 0, 110, freq))), Rand(-1.0, 1.0));
	Out.ar(directOut, source * direct);
	Out.ar(out, source * (1 - direct));
})

*/
