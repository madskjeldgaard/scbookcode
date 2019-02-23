Figure 11.5
// ProxySpace
p = p ?? ProxySpace.push(s);

~ctl = { SinOsc.kr(1) * 110 + 440 };
~out = { arg freq=220, amp=0.5; Ringz.ar(Dust.ar(2, 0.4), freq, 0.8) ! 2 };

~out.play;

~out.map(\freq, ~ctl);

~ctl = { SinOsc.kr(LFSaw.kr(5, add: 2.0) * 10) * 220 + 440 };

ProxyMixer(p);

~out.stop;
~out.clear;

