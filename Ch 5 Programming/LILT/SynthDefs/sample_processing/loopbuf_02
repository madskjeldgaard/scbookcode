
/*

SynthDef("loopbuf_02",{ 
	| out=0, bufnum=0, rate=1, glide=0, gate=1, loopRel=0, startPos=0,
		startLoop = 0, endLoop = 1|
	// ipol=2;
	var num_frames;
	var env, signal;
	num_frames = BufFrames.kr(bufnum);
	rate = Lag.kr(rate, glide);
	env = EnvGen.ar(Env.adsr(0.1,0.2,1,2), gate, doneAction: 2);
	signal = LoopBuf.ar(1, bufnum, BufRateScale.kr(bufnum) * rate, gate, 
		startPos * num_frames, startLoop * num_frames, endLoop * num_frames, 2);
	Out.ar(out, signal * env);
});

*/