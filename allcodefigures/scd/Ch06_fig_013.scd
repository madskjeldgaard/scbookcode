Figure 6.13. Soundfile granulation with a Pattern 
SynthDef("playbuf", { | out=0, bufnum = 0, rate = 1, 
startPos = 0, amp = 0.1, sustain = 1, 
pan = 0, gate = 1|
	var audio, env;
	rate = rate * BufRateScale.kr(bufnum);
	startPos = startPos * BufFrames.kr(bufnum);
	env = EnvGen.ar(Env.sine, 1, timeScale: sustain, doneAction: 2);
	audio = PlayBuf.ar(1, bufnum, rate, 1, startPos, 0);
	audio = env * audio;
	audio = Pan2.ar(audio, pan, amp);
	OffsetOut.ar(out, audio);
}).add;
		
	Pseq([
		(	type:		\load,
			filename:	"sounds/a11wlk01.wav",
			bufnum:	1,
			delta:		0
		),
		
		Pbind(*[
			instrument:	"playbuf",
			type:		\on,
			id:		-1,
			dur:		 Pseg([0,1],21).linexp(0,1,0.01,2),
			legato:		4,
			startPos:	Pn(Pseg([0,1], 10)),
			bufnum:	1,		
		]),
		(	type:		\free, 
			bufnum: 	1
		)
	]).play(quant: 0) 
// the result: [ ( 'midinote': 60 ), ( 'midinote': 64 ), ( 'midinote': 67 ) ]

