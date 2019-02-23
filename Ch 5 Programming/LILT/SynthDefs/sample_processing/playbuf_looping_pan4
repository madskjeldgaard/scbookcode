/* (IZ 2005-09-24) {
playbuf looping out to 4 channels
} */


SynthDef("playbuf_looping_pan4", { | out=0, bufnum=0, rate=1.0, trigger=1.0,
	startPos=0.0, loop = 0.0, amp = 1,
	pos = 0, level = 1, width = 2, orientation = 0.5
	 |
	var src;		// numChannels cannot be variable! Set to 1, that is: Mono
	var pan;		// the panner
	src = PlayBuf.ar(1, bufnum, rate * BufRateScale.kr(bufnum),
		trigger, startPos * BufFrames.kr(bufnum), loop);
	pan = PanAz.ar(4, src, pos, level, width, orientation);
	Out.ar(out, amp * pan);
});
