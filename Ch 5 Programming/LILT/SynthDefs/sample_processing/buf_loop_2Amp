/* 


 */


SynthDef("buf_loop_2Amp", { 
			| bufnum=0, rate=1.0, trigger=1.0, startPos=0.0, loop = 1.0, 
			  out=0,  amp1 = 1, amp2 = 1 ampLag = 1.5 |
		
		var src;		// numChannels cannot be variable! Set to 1, that is: Mono
		
		src = PlayBuf.ar(1, bufnum, rate * BufRateScale.kr(bufnum), 
			trigger, startPos * BufFrames.kr(bufnum), loop);
		Out.ar(out, Lag.kr((amp1*amp2), ampLag)  * src );
										
});

//a = Synth("playbuf_looping_fadeIO", [\bufnum, 1]);


