// figure 13.4 - Sound design with noise pulses 
(
SynthDef( "noisepulses", { arg out = 0, sustain=1.0, numPulses = 0, pan = 0.0, amp = 0.2; 
	Out.ar(out, 
		PanAz.ar(4, 
			PinkNoise.ar 
			* Decay2.ar(Impulse.ar(numPulses / sustain, 0, numPulses.sign), 0.001, 0.2),
			pan, 
			EnvGen.kr( Env.linen(0.0, 0.995, 0.0), levelScale: amp, timeScale: sustain, doneAction: 2)
		)
	);
}).add;
SynthDef(\tick, { |out, amp=0.2, pan| 
	OffsetOut.ar(out, Pan2.ar(Impulse.ar(0) * Line.kr(amp, amp, 0.001, doneAction: 2), pan)) 
}).add;
)
(instrument: \noisepulses, numPulses: 10, legato: 1, dur: 2).play;
(instrument: \tick).play;

