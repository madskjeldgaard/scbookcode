Figure 18.6
(
var score, sndbuf, starttime, synth, options, cond;

SynthDef(\NRT_playback, {arg buffer, dur, startPos, amp;
	OffsetOut.ar(0, PlayBuf.ar(1, buffer, BufRateScale.kr(buffer), 
			startPos: startPos * BufSampleRate.kr(buffer)) *
		EnvGen.ar(
			Env.sine(dur, amp),
			doneAction: 2))
	}).load(s);

score = Score.new;

// set up a Condition to check for when asynchronous events are finished.

cond = Condition.new;

// wrap the code that will run in real-time in a Routine, to allow for the Server to sync
Routine.run({
	// load the buffer
	sndbuf = Buffer.read(s, "sounds/a11wlk01-44_1.aiff");
	
	// pause while the buffer is loaded
	s.sync(cond);
	
	// fill the Score with notes
	
	starttime = 0.0;
	
	while({
		synth = Synth.basicNew(\NRT_playback);
		score.add([starttime, 
			synth.newMsg(s, [\buffer, sndbuf, \dur, 0.1, \startPos, 0.0.rrand(1.0), \amp, 0.1])]);
		starttime = starttime + 0.05;
		starttime < 10.0;
		});
	
	// the last command is NOT needed, since no soundfile is being rendered
//	score.add([11, 0]);
	
	score.sort;
	
	// again, options won't be needed for real time performance
//	options = ServerOptions.new.numOutputBusChannels_(1);
	
	score.play;
	// schedule the freeing of the buffer after the Score is done playing
	SystemClock.sched(11, {sndbuf.free; "Buffer resources freed".postln;});
	})
)



