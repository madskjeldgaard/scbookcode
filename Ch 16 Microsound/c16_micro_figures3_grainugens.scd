	
	// GrainSin.help example as nodeproxy
	
p = ProxySpace.push;
(
~grain.play;
~grain = {	arg envbuf = -1, density = 10, graindur=0.1, amp=0.2;
	var pan, env, freqdev; 
	var trig = Impulse.kr(density);
	pan = MouseX.kr(-1, 1);		// use mouse x to control panning
	// use WhiteNoise and mouse y to control deviation from center
	freqdev = WhiteNoise.kr(MouseY.kr(400, 0));
	GrainSin.ar(2, trig, graindur, 440 + freqdev, pan, envbuf) * amp
};
)



	// switching envelopes while playing
	
q = q ? ();		// make a dict to keep things around
q.envs = ();		// e.g. some envelopes 
q.bufs = ();		// and some buffers
				// make an envelope, and convert it to a buffer
q.envs.perc1 = Env([0, 1, 0], [0.1, 0.9], -4);
q.bufs.perc1 = Buffer.sendCollection(s, q.envs.perc1.discretize, 1);

	// switch between built-in hanning envelope and custom - perc1
~grain.set(\envbuf, -1);
~grain.set(\envbuf, q.bufs.perc1.bufnum);



	//  fixed parameters and control proxies
~grain.set(\density, 20);
~grain.set(\graindur, 0.03);

	// map a control proxy to a parameter
~grdur = 0.1;
~grain.map(\graindur, ~grdur);
~grdur = { LFNoise1.kr(1).range(0.01, 0.1) };
~grdur = { SinOsc.kr(0.3).range(0.01, 0.1) };
~grdur = 0.01;

	// create random densities from 2 to 2 ** 6, exponentially distributed
~grdensity = { 2 ** LFNoise0.kr(1).range(0, 6) };
	// map to density control
~grain.map(\density, ~grdensity);
~grdensity = { 2 ** LFNoise0.kr(1).range(2, 4) };


~grain.clear;

	// GrainFM with mouse control
(
~grain.play;
		// original
~grain = { arg envbuf = -1, density = 10, graindur=0.1, modfreq=200;
	var pan = MouseX.kr(-1, 1);
	var trig = Impulse.kr(density);
	var freqdev = WhiteNoise.kr(MouseY.kr(0, 400));
	var freq = 440 + freqdev;
	var moddepth = LFNoise1.kr.range(1, 10);
	GrainFM.ar(2, trig, graindur, freq, modfreq, moddepth, pan, envbuf) 
	* 0.2
};
)

(	// help file example rewritten to use MouseX for modulation range
~grain = { arg envbuf = -1, density = 10, graindur=0.1, modfreq=200;
	var pan = WhiteNoise.kr;
	var trig = Impulse.kr(density);
	var freqdev = WhiteNoise.kr(MouseY.kr(0, 400));
	var freq = 440 + freqdev;
	var modrange = MouseX.kr(1, 10);
	var moddepth = LFNoise1.kr.range(1, modrange);
	GrainFM.ar(2, trig, graindur, freq, modfreq, moddepth, pan, envbuf) 
	* 0.2
};
)


	// figure 16.13   - GrainFM with individual control proxies
(
~trig = { |dens=10| Impulse.kr(dens) };
~freq = { MouseX.kr(100, 2000, 1) * LFNoise1.kr(1).range(0.25, 1.75) };
~moddepth = { LFNoise1.kr(20).range(1, 10) };
~modfreq = 200;
~graindur = 0.1;

~grain = { arg envbuf = -1;
	GrainFM.ar(2, ~trig.kr, ~graindur.kr, 
		~freq.kr, ~modfreq.kr, ~moddepth.kr, 
		pan: WhiteNoise.kr, envbuf: envbuf) * 0.2
};
~grain.play;
)
	// change control ugens: 
~modfreq = { ~freq.kr * LFNoise2.kr(1).range(0.5, 2.0) }; // modfreq roughly follows freq
~trig = { |dens=10| Dust.kr(dens)};	// random triggering, same density
~freq = { LFNoise0.kr(0.3).range(200, 800) };
~moddepth = 3; 	// fixed depth
~graindur = { LFNoise0.kr.range(0.01, 0.1) };



	// bonus: blend dust and impulse triggers
~trig = { |dens=20, bal=0.2| Dust.kr(dens * (1-bal)) + Impulse.kr(dens * bal) };
~trig.set(\bal, 0.1);
~trig.set(\bal, 0.5);
~trig.set(\bal, 0.9);

ProxyMixer(p);




p = ProxySpace.push;


	// figure 16.14 - GrainBuf and control proxies

b = Buffer.read(s, "sounds/a11wlk01-44_1.aiff");
(
~grain.set(\wavebuf, b.bufnum);
~trig = { |dens=10| Impulse.kr(dens) };
~graindur = 0.1;
~filepos = {LFNoise2.kr(0.2).range(0, 1) };
~rate = { LFNoise1.kr.range(0.5, 1.5) };

~grain = { arg envbuf = -1, wavebuf = 0;
	GrainBuf.ar(2, ~trig.kr, ~graindur.kr, wavebuf, 
	~rate.kr, ~filepos.kr, 2, WhiteNoise.kr, envbuf) * 0.2
};
~grain.play;
)

	// experiment with control proxies
~trig = { |dens=20| Impulse.kr(dens) };
~rate = { LFNoise1.kr.range(0.99, 1.01) };
~filepos = { MouseX.kr + LFNoise0.kr(100, 0.03) };
~graindur = 0.05;
~trig = { |dens=50| Dust.kr(dens) };

c = Buffer.sendCollection(s, Env.perc(0.01, 0.99).discretize, 1);
~grain.set(\envbuf, c.bufnum); 
~grain.set(\envbuf, -1);

~trig = { |dens=50| Impulse.kr(dens) }; ~graindur = 0.05;


