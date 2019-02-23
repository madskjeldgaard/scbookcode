
Figure 18.4
(
var score, graingest;

// seed the randomness
thisThread.randSeed_(123);

// a sample SynthDef
SynthDef(\NRT_grain, {arg freq, dur, amp, pan;
	OffsetOut.ar(0, Pan2.ar(
		SinOsc.ar(freq, 0, 
			EnvGen.ar(Env.sine(dur, amp), doneAction: 2)),
		pan)
		);
	}).load(s);

score = Score.new;

// envelope times are scaled to 1.
graingest = {arg score, starttime, duration, windur, overlaps, freqenv, ampenv, panenv;
	var ratio, curfreq, curamp, curpan, notestart, now = 0.0, note;
	while({
		ratio = now / duration;
		curfreq = freqenv[ratio];
		curamp = ampenv[ratio];
		curpan = panenv[ratio];
		notestart = now + starttime;
		note = Synth.basicNew(\NRT_grain);
		score.add([notestart, 
			note.newMsg(1, [\freq, curfreq,\amp, curamp, \dur, windur, \pan, curpan], \addToHead)]
			);
		// check the current event's endtime against the global endtime
		now = now + (windur / overlaps);
		now < duration;
		});
	};

// call the above function to populate the Score

graingest.value(score, 1.0, 10.0, 100.reciprocal, 1, Env([440, 550], [1]), 
	Env([0, 0.2, 0], [0.3, 0.7], [4, -4]), Env([0, 0], [1]));
graingest.value(score, 3.0, 3.0, 130.reciprocal, 2, Env([700, 400], [1]),
	Env([0, 0.2, 0], [0.1, 0.9], [4, -1]), Env([-0.7, 0.7], [1]));

// create a number of short gestures
10.do({arg i;
	graingest.value(score, 5.0.rrand(10.0), 3.0.rrand(5.0), (100 * i).reciprocal, [1, 2, 4].choose,
		Env([1000, 800], [1]), Env([0, 0.2, 0], [0.5, 0.5]), Env([0.5.rand2, 0.5.rand2], [1]));
	});

// save the endtime to the Score to tell NRT when to stop rendering. The above gestures won't
// be more the 16 seconds

score.add([16, [0]]);

// sort the score to ensure events are in the correct order

score.sort;

// render the Score to the users home folder

score.recordNRT("/tmp/trashme", "~/test.aiff".standardizePath, 
	options: ServerOptions.new.numOutputBusChannels_(1));
	
// also save the Score to a file
score.saveToFile("~/test.sc".standardizePath);
)	


