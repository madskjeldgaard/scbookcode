
Figure 7.11

~out.play; ~out.fadeTime = 3;
(
// name with a_ represents audio rate argument
~out = { |freq=440, mod=0.4, detune=0.1, a_in = #[1,1]|
	freq = freq * ([0, detune] + 1);
	LFTri.ar(LFTri.ar(mod * freq).range(freq * mod, freq)) * a_in * 0.2
}
);

(
~mod2 = { LFNoise1.kr(1).range(0, 1) };
~mod1 =  { LFPulse.kr(~mod2.kr * 30 + 1, 0, 0.3) };
~freq1 = { ~mod1.kr * 13100 + 100 };
~freq2 = { LFTri.kr(30) * 200 + 300 };
~audio1 = { BrownNoise.ar(LFClipNoise.kr(10.dup), 1) };
~audio2 = { SinOsc.ar(LFNoise2.kr(1.dup).exprange(4, 1000)) };
);

~out.map(\freq, ~freq2, \mod, ~mod1);
~out.set(\detune, 0.01);
~out.map(\freq, ~freq1, \mod, ~mod1);
~out.xmap(\freq, ~freq1, \mod, ~mod2); // xmap crossfades over fade time to new value.
~out.xmap(\freq, ~freq2, \mod, ~mod1, \a_in, ~audio2);
~out.map(\a_in, ~audio1);


