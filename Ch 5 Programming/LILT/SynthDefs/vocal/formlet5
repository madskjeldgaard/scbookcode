/* (IZ 2005-06-18) {
	formant synthesis with FOF-like Formlet
	(to be controlled by MIDI accordeon)
	v1: SinOsc vibrato (modulated by?)

Formant table for vocals from http://www.vocalist.org.uk/frequencies.html
			F1				F2
	i	200-300		2000-3000
	e	400-700		1700-2200
	a	500-1000		1000-1700
	o	400-700		700-1000
	u	200-300		500-900
}

Formlet.ar(in, freq, attacktime, decaytime, mul, add)

*/

SynthDef("formlet5", { | fund = 400, numharm = 1000, mul = 1,
	f1 = 250, f2 = 2500, f3 = 250, f4 = 2500, f5 = 3000,
	att1 = 0.2, att2 = 0.15, att3 = 0.2, att4 = 0.15, att5 = 0.1,
	dec1 = 0.2, dec2 = 0.15, dec3 = 0.2, dec4 = 0.15, dec5 = 0.1,
	a1 = 0.2, a2 = 0.15, a3 = 0.2, a4 = 0.15, a5 = 0.1,
	fundjitfreq = 10, fundjitwidth = 0.0,
	formjitfreq = 4, formjitwidth = 0.05,
	amp = 0.5, out = 0 |
	var src, filt;
	src = Blip.ar(fund + (fund * SinOsc.kr(fundjitfreq, 0, 0.75, 0.25) * fundjitwidth),
		numharm, mul);
	filt = Mix.ar(
		Formlet.ar(src,
			[f1 + (f1 * SinOsc.kr(formjitfreq, 0, 0.75, 0.25) * formjitwidth),
			f2 + (f2 * SinOsc.kr(formjitfreq, 0, 0.75, 0.25) * formjitwidth),
			f3 + (f3 * SinOsc.kr(formjitfreq, 0, 0.75, 0.25) * formjitwidth),
			f4 + (f4 * SinOsc.kr(formjitfreq, 0, 0.75, 0.25) * formjitwidth),
			f5 + (f5 * SinOsc.kr(formjitfreq, 0, 0.75, 0.25) * formjitwidth)
			],
			[att1, att2, att3, att4, att5],
			[dec1, dec2, dec3, dec4, dec5],
			[a1, a2, a3, a4, a5]
		)
	);
	Out.ar(out, amp * filt);
});


/* // V0: Random jitter. This is too uneven, therefore replace with vibrato, 
	// then add variation to the vibrato
SynthDef("formant4", { | fund = 400,
	f1 = 250, f2 = 2500, f3 = 250, f4 = 2500, f5 = 3000,
	b1 = 150, b2 = 1000, b3 = 150, b4 = 1000, b5 = 1500,
	a1 = 0.2, a2 = 0.15, a3 = 0.2, a4 = 0.15, a5 = 0.1
	fundjitfreq = 10, fundjitwidth = 0.0,
	formjitfreq = 4, formjitwidth = 0.05,
	amp = 0.5, out = 0 |
	var src;
	src = Mix.ar(
		Formant.ar(
			fund + (fund * LFDNoise1.kr(fundjitfreq, 0.75, 0.25) * fundjitwidth),
			[f1 + (f1 * LFDNoise1.kr(formjitfreq, 0.75, 0.25) * formjitwidth),
			f2 + (f2 * LFDNoise1.kr(formjitfreq, 0.75, 0.25) * formjitwidth),
			f3 + (f3 * LFDNoise1.kr(formjitfreq, 0.75, 0.25) * formjitwidth),
			f4 + (f4 * LFDNoise1.kr(formjitfreq, 0.75, 0.25) * formjitwidth),
			f5 + (f5 * LFDNoise1.kr(formjitfreq, 0.75, 0.25) * formjitwidth)
			],
			[b1, b2, b3, b4, b5],
			[a1, a2, a3, a4, a5])
	);
	Out.ar(out, amp * src);
});


*/

/* // early code draft for porting to synthdef:

~synth = { | fund = 400,
	f1 = 250, f2 = 2500, f3 = 250, f4 = 2500, f5 = 3000,
	b1 = 150, b2 = 1000, b3 = 150, b4 = 1000, b5 = 1500,
	a1 = 0.2, a2 = 0.15, a3 = 0.2, a4 = 0.15, a5 = 0.1
	fundjitfreq = 10, fundjitwidth = 0.0,
	formjitfreq = 4, formjitwidth = 0.05,
	amp = 0.5 |
	amp * Mix.ar(
		Formant.ar(
			fund + (fund * LFDNoise1.kr(fundjitfreq, 0.75, 0.25) * fundjitwidth),
			[f1 + (f1 * LFDNoise1.kr(formjitfreq, 0.75, 0.25) * formjitwidth),
			f2 + (f2 * LFDNoise1.kr(formjitfreq, 0.75, 0.25) * formjitwidth),
			f3 + (f3 * LFDNoise1.kr(formjitfreq, 0.75, 0.25) * formjitwidth),
			f4 + (f4 * LFDNoise1.kr(formjitfreq, 0.75, 0.25) * formjitwidth),
			f5 + (f5 * LFDNoise1.kr(formjitfreq, 0.75, 0.25) * formjitwidth)
			],
			[b1, b2, b3, b4, b5],
			[a1, a2, a3, a4, a5])
		);
}.scope;



*/