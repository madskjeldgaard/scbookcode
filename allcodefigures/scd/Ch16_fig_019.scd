Figure 16.19: A nodeproxy for time-pitch changing

p = ProxySpace.push(s.boot);
b = Buffer.read(s, "sounds/a11wlk01-44_1.aiff");
(
~timepitch = {arg sndbuf, pitchRatio=1, pitchRd=0.01, grainRate=10, overlap=2, 
	posSpeed=1, posRd=0.01;
	
	var graindur = overlap / grainRate;
	var pitchrate = pitchRatio + LFNoise0.kr(grainRate, pitchRd);
	var position = LFSaw.kr(posSpeed / BufDur.kr(sndbuf)).range(0, 1) 
		+ LFNoise0.kr(grainRate, posRd);
		
	GrainBuf.ar(2, Impulse.kr(grainRate), graindur, sndbuf, pitchrate,
			position, 4, 0, -1)
};
~timepitch.set(\sndbuf, b.bufnum);
~timepitch.play;
);

Spec.add(\pitchRatio, [0.25, 4, \exp]);
Spec.add(\pitchRd, [0, 0.5, \amp]);
Spec.add(\grainRate, [1, 100, \exp]);
Spec.add(\overlap, [0.25, 16, \exp]);
Spec.add(\posSpeed, [-2, 2]);
Spec.add(\posRd, [0, 0.5, \amp]);
NodeProxyEditor(~timepitch, 10);

	// reconstruct original
~timepitch.set(\pitchRatio, 1, \pitchRd, 0, \grainRate, 20, \overlap, 4, \posSpeed, 1, \posRd, 0);

	// four times as long: tweak pitchRd and posJitter to reduce artifacts
~timepitch.set(\pitchRatio, 1, \pitchRd, 0, \grainRate, 20, \overlap, 4, \posSpeed, 0.25, \posRd, 0);

	// random read position, random pitch
~timepitch.set(\pitchRatio, 1, \pitchRd, 0.5, \grainRate, 20, \overlap, 4, \posSpeed, 0.25, \posRd, 0.5);

