Figure 7.8

Ndef(\out, { SinOsc.ar(Ndef.kr(\x) * 200 + 300) * 0.1 }).play;
Ndef(\x, { LFPulse.kr([1.3, 2.1, 3.2]).sum });
Ndef.clear;

