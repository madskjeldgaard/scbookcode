
Figure 7.27

Ndef(\x).play; // here an Ndef is used, the same can be done within a ProxySpace
Ndef(\x, { |freq=5, detune=0| Impulse.ar(freq * [detune, 1 - detune]) * 0.2 });
(
Tdef(\c, {
	loop {
		 Ndef(\x).fadeTime = rrand(0.1, 3.5);
		 Ndef(\x).send([\freq, exprand(2, 400), \detune, [0.0, 1.0].choose.rand]);
		2.wait;
	}
}).play
);


