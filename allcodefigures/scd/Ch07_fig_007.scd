Figure 7.7

n = NodeProxy.new;
x = { SinOsc.ar(n.kr * 200 + 300) * 0.1 }.play;
n.source = { LFPulse.kr([1.3, 2.1, 3.2]).sum };
n.clear; x.free;


