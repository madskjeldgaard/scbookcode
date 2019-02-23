Figure 7.9

p = ProxySpace.push; // if needed
~out = { SinOsc.ar(~x.kr * 200 + 300) * 0.1 }
~out.play;
~x = { LFPulse.kr([1.3, 2.1, 3.2]).sum };
p.clear.pop;


