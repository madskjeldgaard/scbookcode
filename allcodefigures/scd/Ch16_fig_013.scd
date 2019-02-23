	Figure 16.13: GrainFM with individual control proxies

	// figure 16.13   - GrainFM with individual control proxies
p = ProxySpace.push;

(
~trig = { |dens=10| Impulse.kr(dens) };
~freq = { MouseX.kr(100, 2000, 1) * LFNoise1.kr(1).range(0.25, 1.75) };
~moddepth = { LFNoise1.kr(20).range(1, 10) };
~modfreq = 200;
~graindur = 0.1;

~grain = { arg envbuf = -1;
	GrainFM.ar(2, ~trig.kr, ~graindur.kr, 
		~freq.kr, ~modfreq.kr, ~moddepth.kr, 
		pan: WhiteNoise.kr, envbufnum: envbuf) * 0.2
};
~grain.play;
)
	// change control ugens: 
~modfreq = { ~freq.kr * LFNoise2.kr(1).range(0.5, 2.0) }; // modfreq roughly follows freq
~trig = { |dens=10| Dust.kr(dens)};	// random triggering, same density
~freq = { LFNoise0.kr(0.3).range(200, 800) };
~moddepth = 3; 	// fixed depth
~graindur = { LFNoise0.kr.range(0.01, 0.1) };

