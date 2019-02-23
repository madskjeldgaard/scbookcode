Figure 20.10
// In the definition file:
(
(make: { |name|
	PR(\bufSlicer) => BP(name);
}, type: \bp) => Fact(\basic);

(make: { |name|
	PR(\bufSlicer).chuck(BP(name), parms: (
		path: "sounds/a11wlk01-44_1.aiff",
		tfactor: sin(Pseries(0, 0.1, inf)) * 0.45 + 0.55,
		delta: Pstutter(Pwhite(4, 12, inf), Pxrand((1..6) * 0.05, inf)),
		startSec: Pclutch(Pwhite(0.0, Pfunc({ ~buf.duration }) - Pkey(\time)), Pdiff(Pkey(\delta)).abs > 0),
		pan: sin(Ptime(inf)),
		quant: 1	// quantize to next beat
	))
}, type: \bp) => Fact(\fancy);
)

// In the performance script:
Fact(\basic) => BP(\basic);

BP(\basic).play;

Fact(\fancy) => BP(\fancy);

BP(\fancy).play;

BP([\basic, \fancy]).stop;

BP([\basic, \fancy]).free;


