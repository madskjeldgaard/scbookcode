

Figure 7.4

p = ProxySpace.push;	// if needed

~a = { Lag.ar(LFClipNoise.ar(2 ! 2, 0.5, 0.5), 0.2) };
(
~b = {
	var c, d;
	c = Dust.ar(20 ! 2);
	d = Decay2.ar(c, 0.01, 0.02, SinOsc.ar(11300));
	d + BPF.ar(c * 5, ~a.ar * 3000 + 1000, 0.1)
}
);

~b.play;

// the refactored code from above

(
~a = { 
	var a;
	a =  Lag.ar(LFClipNoise.ar(2 ! 2, 0.5, 0.5), 0.2);
	BPF.ar(~c.ar * 5, a * 3000 + 1000, 0.1) 
}
);
~c = { Dust.ar(20 ! 2) };
~d = { Decay2.ar(~c.ar, 0.01, 0.02, SinOsc.ar(11300)) };
~b = ~a + ~d;

~b.play;




