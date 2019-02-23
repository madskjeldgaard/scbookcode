/* IZ 2007-06-07 { SC3

/*
rate: playback rate
glide: lag factor in moving from one rate to the next.
// gate: when released, the synth fades out 
	gate 	positive gate starts playback from startPos
			negative gate plays rest of sample from current position
	startPos - sample frame to start playback
	startLoop - sample frame of start of loop
	endLoop - sample frame of end of loop
	interpolation - 1 means no interpolation, 2 is linear, 4 is cubic interpolation

} */

SynthDef("loopbuf_01", {
	| out = 0, bufnum = 0, rate = 1, glide = 0, gate = 1, start = 0, end = 0, amp = 0.5 | // , ipol=2;
	var num_frames, loop_start;
	var env, signal;
	num_frames = BufFrames.kr(bufnum);
	loop_start = start * num_frames;
	rate = Lag.kr(rate, glide);
//	env = EnvGen.ar(Env.adsr(0.1, 0.2, 1, 2), gate, doneAction: 2);
	signal = LoopBuf.ar(1, bufnum, BufRateScale.kr(bufnum) * rate, gate,
		loop_start, loop_start, end * num_frames, 2);
	Out.ar(out, signal * amp);
});

*/
