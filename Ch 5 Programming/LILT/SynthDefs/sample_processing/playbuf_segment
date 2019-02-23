/* IZ 071112
Play a segment of a buffer.
Control variables: 
rate: constant playback rate, 
startPos: position in buffer to start at 0 means beginning, 1 means end of buffer
length: length of playback, max = 1 = entire buffer
Uses envelope to prevent clicks at onset and end. Fadein and fadeout are fixed at 0.03 seconds.  
*/

SynthDef("playbuf_segment", { |
	out = 0, 
	bufnum = 0, 
	startPos = 0, 
	length = 1,
	rate = 1,
	amp = 1
	|
	var src, env;
	src = PlayBuf.ar(1, bufnum, rate * BufRateScale.kr(bufnum),
		1, startPos * BufFrames.kr(bufnum), 1);
	env = EnvGen.kr(Env([0, 1, 1, 0], [0.03, BufDur.kr(bufnum) / rate * length - 0.06, 0.03]),
		timeScale: 1 / rate, doneAction: 2); 
//	FreeSelfWhenDone.kr(src);
	Out.ar(out, amp * env * src);
})