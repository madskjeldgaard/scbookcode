SynthDef("nosign", { arg out = 0, dt = 6, freq = 500, amp = 1;
        var source;
        source = SinOsc.ar(freq * 1.01, SinOsc.ar(freq, 0, 0.1), 0.2)
           * (LFClipNoise.kr(XLine.kr(31, 16, dt)).max(0) + Line.kr(0.5, 0.1, dt));
        Out.ar(out, source * amp);
});

/* 

a = Synth("noSign");

a.set(\amp, 1)

(
SynthDef("noSign", { arg out = 0, dt = 10, freq = 500, amp = 1;
        var source;
        source = SinOsc.ar(freq * 1.01, SinOsc.ar(freq, 0, 0.1), 0.2)
           * (LFClipNoise.kr(XLine.kr(31, 0.1, dt)).max(0) + Line.kr(0.5, 0.1, dt));
        Out.ar(out, source * amp);
}).play(s);
)

*/

