Figure 16.15: Glisson synthesis

(
b = Buffer.read(s, "sounds/a11wlk01-44_1.aiff");

SynthDef("glisson", 
	{ arg out = 0, envbuf, freq=800, freq2=1200, sustain=0.001, amp=0.2, pan = 0.0;
		var env = Osc1.ar(envbuf, sustain, 2);
		var freqenv = XLine.ar(freq, freq2, sustain);
		OffsetOut.ar(out, 
			Pan2.ar(SinOsc.ar(freqenv) * env, pan, amp)
		)
}, \ir!7).add;
)

(
Tdef(\gliss0, { |e|
	100.do({ arg i;
		s.sendBundle(s.latency, ["/s_new", "glisson", -1, 0, 0, 
			\freq, i % 10 * 100 + 1000,
			\freq2, i % 13 * -100 + 3000,
			 \sustain, 0.05, 
			 \amp, 0.1,
			 \envbuf, b.bufnum
		]);
		(3 / (i + 10)).wait;
	});
}).play;
)



