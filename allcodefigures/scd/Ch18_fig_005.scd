
Figure 18.5
(
var score, sndbuf, starttime, synth, options;

SynthDef(\NRT_playback, {arg buffer, dur, startPos, amp;
	OffsetOut.ar(0, PlayBuf.ar(1, buffer, BufRateScale.kr(buffer), 
			startPos: startPos * BufSampleRate.kr(buffer)) *
		EnvGen.ar(
			Env.sine(dur, amp),
			doneAction: 2))
	}).load(s);

score = Score.new;

// create a Buffer object for adding to the Score
sndbuf = Buffer.new;

// for NRT rendering, the buffer messages must be added to the Score
score.add([0, sndbuf.allocReadMsg("sounds/a11wlk01-44_1.aiff")]);

starttime = 0.0;

// a small function to create a series of small notes based on the Buffer
while({
	synth = Synth.basicNew(\NRT_playback);
	score.add([starttime, 
		synth.newMsg(s, [\buffer, sndbuf, \dur, 0.1, \startPos, 0.0.rrand(1.0), \amp, 0.1])]);
	starttime = starttime + 0.05;
	starttime < 10.0;
	});

// the dummy command. The soundfile will be 11 seconds long
score.add([11, 0]);

score.sort;

// the ServerOptions for rendering the soundfile
options = ServerOptions.new.numOutputBusChannels_(1);

// write the soundfile out to disk
score.recordNRT("/tmp/trashme", "~/test.aiff".standardizePath, options: options);
)


