Figure 16.17: Pulsars as nodeproxies using GrainBuf

(
p = ProxySpace.push; 

		// fund, form, amp, pan
~controls = [ 16, 100, 0.5, 0]; 
~pulsar1.set(\wavebuf, q.bufs.pulsaret.bufnum);
~pulsar1.set(\envbuf, q.bufs.env.bufnum);

~pulsar1 = { |wavebuf, envbuf = -1| 
	var ctls = ~controls.kr;
	var trig = Impulse.ar(ctls[0]);
	var grdur = ctls[1].reciprocal;
	var rate = ctls[1] * BufDur.kr(wavebuf);
		
	GrainBuf.ar(2, trig, grdur, wavebuf, rate, 0, 4, ctls[3], envbuf);
};
~pulsar1.play;
)

	// crossfade between control settings
~controls.fadeTime = 3; 
~controls = [ 16, 500, 0.5, 0]; 	// change formfreq
~controls = [ 50, 500, 0.5, 0]; 	// change fundfreq
~controls = [ 16, 100, 0.5, 0]; 	// change both
~controls = [ rrand(12, 100), rrand(100, 1000)]; 

(	// control parameters from looping tables
~controls = { |looptime = 10| 
	var rate = BufDur.kr(q.bufs.pulsaret.bufnum) / looptime; 
	A2K.kr(PlayBuf.ar(1, [\fund, \form, \amp, \pan].collect(q.bufs[_]), 
		rate: rate, loop: 1));
};
)

