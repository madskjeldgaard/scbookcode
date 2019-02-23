/* Figure 18.1 */
/*
This example is adapted and extracted from the Non-Realtime Synthesis helpfile itself, accessible from the Main SuperCollider help page.
*/
(
var f, c, d;
// open a file for writing raw OSC data to
f = File("~/test.osc".standardizePath,"w");
// start a sine oscillator at 0.2 seconds.
c = [ 0.2, [\s_new, \default, 1001, 0, 0]];
// convert the bundle to raw OSC
d = c.asRawOSC;
f.write(d.size); // each bundle is preceded by a 32 bit size.
f.write(d); // write the bundle data.
f.close;
)
/* End Figure 18.1 */
/* Figure 18.2 */
s = Server.local;
s.boot;

// a sample SynthDef
SynthDef(\NRT_beep, {arg freq, dur, amp = 0.1;
	var half;
	half = dur * 0.5;
	Out.ar(0, SinOsc.ar(freq, 0, 
		EnvGen.kr(Env.new([0, amp, 0], [half, half], [4, -4]))));
	}).load(s);

(
var score;

// A Score, created from a note-list of time-stamped events.
score = Score.new([
    [0.0,
        [\g_new, 1000],
        [\s_new, \NRT_beep, 1001, 0, 1000, \freq, 75.midicps, \dur, 0.2]
    ], 
    [0.2,
        [\n_free, 1001],
        [\s_new, \NRT_beep, 1001, 0, 1000, \freq, 75.midicps, \dur, 0.2]
    ], 
    [0.4,
        [\n_free, 1001],
        [\s_new, \NRT_beep, 1001, 0, 1000, \freq, 75.midicps, \dur, 0.2]
    ], 
    [0.6,
        [\n_free, 1001],
        [\s_new, \NRT_beep, 1001, 0, 1000, \freq, 75.midicps, \dur, 0.2]
    ], 
    [0.8,
        [\n_free, 1001],
        [\s_new, \NRT_beep, 1001, 0, 1000, \freq, 71.midicps, \dur, 0.2]
    ], 
    [1.0,
        [\n_free, 1001],
        [\s_new, \NRT_beep, 1001, 0, 1000, \freq, 71.midicps, \dur, 0.2]
    ], 
    [1.2,
        [\n_free, 1001],
        [\s_new, \NRT_beep, 1001, 0, 1000, \freq, 82.midicps, \dur, 0.2]
    ], 
    [1.4,
        [\n_free, 1001],
        [\s_new, \NRT_beep, 1001, 0, 1000, \freq, 82.midicps, \dur, 0.2]
    ], 
    [1.6,
        [\n_free, 1001],
        [\s_new, \NRT_beep, 1001, 0, 1000, \freq, 82.midicps, \dur, 0.2]
    ], 
    [1.8,
        [\n_free, 1001],
        [\s_new, \NRT_beep, 1001, 0, 1000, \freq, 82.midicps, \dur, 0.2]
    ], 
    [2.0,
        [\n_free, 1001, 1000]
    ],
	[2.00001, [0]]
]);

score.play(s); // play the Score in real-time...

// ... or render in in Non-Real-Time
score.recordNRT("/tmp/trashme", "~/test.aiff".standardizePath, 
	options: ServerOptions.new.numOutputBusChannels_(1));
)
/* End Figure 18.2 */
/* Figure 18.3 */
(
//In this example, we use the higher-level server abstraction classes, Group and Synth to handle the
// node IDs. At least as important though is the use of variables. Now that the relationships are
// specified rather than the specific values, we can change the gesture dramatically by changing
// just one or two variables. To transpose everything, we only need to change the value of 
// ~baseNote. To adjust the duration, we only need to change the ~dur variable, and this is now 
// independent of the deltaOn (i.e. independent of the amount of time between the start of one
// note and the start of the next note).
var score;
var deltaOn = 0.2;  //amount of time between the start of one note and the start of the next note
var dur = 0.4;      //try changing dur to 0.3, 1.4, 3.4, or whatever you like
var baseNote = 75;  //transpose the entire fragment up or down
var firstPitch  = (baseNote + 0).midicps;  //alter the relationship between any of the pitches
var secondPitch = (baseNote - 4).midicps;  // without effecting the others
var thirdPitch  = (baseNote + 7).midicps;

score = Score.new([
	[t =  0.0,
		(g = Group.basicNew(s)).newMsg,
		//we use environment variables here (identified by the preceding ~) 
		// since we might add or remove events; hence we don't know ahead of 
		// time how many events we have, and therefore how many variables we'll need
		(~s01 = Synth.basicNew(\NRT_beep, s))
			.newMsg(g, [\freq, firstPitch, \dur, dur], \addToHead)
	], 
	[t + dur,
		~s01.freeMsg
	],
	[t = t + deltaOn,
		(~s02 = Synth.basicNew(\NRT_beep, s))
			.newMsg(g, [\freq, firstPitch, \dur, dur], \addToHead)
	], 
	[t + dur,
		~s02.freeMsg
	],
	[t = t + deltaOn,
		(~s03 = Synth.basicNew(\NRT_beep, s))
			.newMsg(g, [\freq, firstPitch, \dur, dur], \addToHead)
	],
	[t + dur,
		~s03.freeMsg
	],
	[t = t + deltaOn,
		(~s04 = Synth.basicNew(\NRT_beep, s))
			.newMsg(g, [\freq, firstPitch, \dur, dur], \addToHead)
	], 
	[t + dur,
		~s04.freeMsg
	],
	[t = t + deltaOn,
		(~s05 = Synth.basicNew(\NRT_beep, s))
			.newMsg(g, [\freq, secondPitch, \dur, dur], \addToHead)
	], 
	[t + dur,
		~s05.freeMsg
	],
	[t = t + deltaOn,
		(~s06 = Synth.basicNew(\NRT_beep, s))
			.newMsg(g, [\freq, secondPitch, \dur, dur], \addToHead)
	], 
	[t + dur,
		~s06.freeMsg
	],
	[t = t + deltaOn,
		(~s07 = Synth.basicNew(\NRT_beep, s))
			.newMsg(g, [\freq, thirdPitch, \dur, dur], \addToHead)
	], 
	[t + dur,
		~s07.freeMsg
	],
	[t = t + deltaOn,
		(~s08 = Synth.basicNew(\NRT_beep, s))
			.newMsg(g, [\freq, thirdPitch, \dur, dur], \addToHead)
	], 
	[t + dur,
		~s08.freeMsg
	],
	[t = t + deltaOn,
		(~s09 = Synth.basicNew(\NRT_beep, s))
			.newMsg(g, [\freq, thirdPitch, \dur, dur], \addToHead)
	], 
	[t + dur,
		~s09.freeMsg
	],
	[t = t + deltaOn,
		(~s10 = Synth.basicNew(\NRT_beep, s))
			.newMsg(g, [\freq, thirdPitch, \dur, dur], \addToHead)
	], 
	[t + dur,
		~s10.freeMsg,
		g.freeMsg
	],
]
);

score.sort;
score.play(s);
)
/* End Figure 18.3 */
/* Figure 18.4 */
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
		// check the current event's endtime against the gloabl endtime
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
/* End Figure 18.4 */	
/* Figure 18.5 */
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
/* End Figure 18.5 */
/* Figure 18.6 */
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
/* End Figure 18.6 */
/* Figure 18.7 */


// environment variables are used for real-time examples of Ctk objects

n = CtkNoteObject(
	SynthDef(\NRT_grain, {arg gate = 1, freq, amp;
		var src, env;
		src = SinOsc.ar(freq, 0, amp);
		env = EnvGen.kr(Env([0, 1, 0], [1, 1], \sin, 1), gate, doneAction:2);
		OffsetOut.ar(0, src * env);
		})
	);
	
// create a new note based on 'n', start to play it in 0.1 seconds
a = n.new(0.1).freq_(440).amp_(0.1).gate_(1).play;
// the release method will set 'gate' to 0.0, and free this node
a.release;

// create another note
a = n.new(0.1).freq_(440).amp_(0.1).play;
// alter the freq argument in real time
a.freq_(550);
// alter the freq with a CtkControl that describes an Env
// CtkControl.env(Env)
a.freq_(CtkControl.env(Env([550, 440, 550], [1, 2], \exp)));
// apply a random control to the amp parameter, with an envelope applied to the range. All 
// parameters to the CtkControl can themselves be CtkControls
// CtkControl.lfo(KRUGen, freq, low, high, phase)
a.amp_(CtkControl.lfo(LFNoise2, 0.5, CtkControl.env(Env([0.1, 0.9], [5])), 0.1));
a.amp_(0.1);

// release the note
a.release;

/* End Figure 18.7 */
/* Figure 18.8 */
// melodic expander
(
var note, keys, durs, now, score, chunk, expander, rangemap;

// 
thisThread.randSeed_(123);

// a simple note player

note = CtkSynthDef(\NRT_dut, {arg key, amp, dur;
		Out.ar(0, SinOsc.ar(key.midicps, 0, XLine.kr(amp, 0.00001, dur)))
		});

// first, make a melody - these will be used as midikeynums (easier to alter later)

keys = [ 72, 72, 79, 79, 81, 81, 79, 77, 77, 76, 76, 74, 74, 72 ];

// a list of durations

durs = [0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.5, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.5];

// create a var to store 'now' in

now = 0.0;

// create a CtkScore with the above melody

score = CtkScore.new;

keys.do({arg thiskey, inc;
	var thisdur;
	thisdur = durs[inc];
	score.add(note.new(now, thisdur).key_(thiskey).amp_(0.2).dur_(thisdur));
	now = now + thisdur;
	});

// first, create a function that will return a chunk of the melody the duration of the chunk
// sets the starttimes of the notes to a base of 0.0

chunk = {arg offset = 0;
	var size, start, end, duration = 0, chunk, copies;
	// the size of the current melody - 1 (for array access)
	size = score.notes.size;
	// the beginning of the chunk can come from the beginning of the melody to the second to 
	// last note
	start = 0.rrand(size-1);
	end = start.rrand(size);
	chunk = score.notes[start..end].collect({arg anote; 
		var newnote;
		newnote = anote.copy(duration + offset);
		duration = duration + anote.duration;
		newnote;
		});
	[chunk, duration];
	};

// now, create a function that will add those chunks to the score, and will keep doing this
// until the score is at least the desired length. Then check the score size, and truncate to 
// desired size.

expander = {arg len;
	var curchunk, chunkdur, insert, inserttime, insertdur, cursize, newnotes;	cursize = score.notes.size;
	while({
		cursize < len
		}, {		
		insert = 0.rrand(cursize - 1);
		inserttime = score.notes[insert].starttime;
		insertdur = score.notes[insert].duration;
		#curchunk, chunkdur = chunk.value(inserttime + insertdur);
		score.notes[(insert+1)..(cursize-1)].do({arg me; 
			me.setStarttime(me.starttime + chunkdur)});
		score = score.add(curchunk);
		(score.notes.size > len).if({
			score.notes.do({arg me, i;
				(i > (len - 1)).if({score.notes.remove(me)});
				})
			});
		cursize = score.notes.size;
		});
	};

// rangemap will place the melodic material within a certain range. The user passes 
// in an envelope that will describe the center pitch in an octave range

rangemap = {arg center;
	score.notes.do({arg me;
		me.key_(me.key.mapIntoRange(12, center[me.starttime]));
		})
	};
		
// expand it to 100 notes
expander.value(100);

// describe a new range of pitches
rangemap.value(Env([60, 96], [20]));

// finally, play the CtkScore

score.play;
)
/* End Figure 18.8 */
/* Figure 18.9 */

(
var score, grain, now, thisdur;
var ampmap, double;

grain = CtkNoteObject(
	SynthDef(\NRT_grain, {arg freq, amp, dur, pan = 0;
		var src, env;
		env = EnvGen.ar(
			Env([0, 1, 0], [0.5, 0.5], \sin),
			timeScale: dur, levelScale: amp);
		src = SinOsc.ar(freq, 0, env);
		OffsetOut.ar(0, Pan2.ar(src, pan));
		})
	);
	
score = CtkScore.new;

now = 0;

// create a 3 second granular gesture

while({
	thisdur = 0.05.rrand(0.1);
	score.add(
		grain.new(now, thisdur).freq_(440.rrand(880)).amp_(0.05).dur_(thisdur).pan_(0));
	now = now + 0.01;
	now < 3;
	});

// a function to later map the amplitude to a given shape
// envtimes should be scaled to 1
ampmap = {arg aScore, env;
	// caled the envs times by the CtkScore's duration
	env.times = env.times * aScore.endtime;
	aScore.notes.do({arg thisNote;
		var curtime;
		curtime = thisNote.starttime;
		thisNote.amp_(env[curtime]);
		});
	};

// returns a new copy of the CtkScore with notes
// double an octave higher
double = {arg aScore, shift = 2;
	var thisScore;
	thisScore = aScore.copy;
	thisScore.notes.do({arg thisNote;
		thisNote.freq_(thisNote.freq * shift)
		});
	thisScore;
	};
		
// a Routine to play the examples
Routine.run({
	var scoreDouble;
	// play the CtkScore;
	score.play;
	score.endtime.wait;
	// remap the amplitudes
	ampmap.value(score, Env([0, 0.2, 0], [0.1, 0.9], [4, -2]));
	1.wait; // pause for a moment
	// play it again!
	score.play;
	score.endtime.wait;
	// add the CtkScore that octaveDouble returns
	scoreDouble = double.value(score, 19.midiratio);
	ampmap.value(scoreDouble, Env([0, 0.25, 0], [0.6, 0.4], [4, -2]));
	score.add(scoreDouble);
	1.wait;
	score.play;
	score.endtime.wait;
	// don't like the second version? remove double
	score.ctkscores.remove(scoreDouble);
	ampmap.value(score, Env([0.15, 0.05], [1]));
	1.wait;
	score.play;
	}).randSeed_(123)
)


/* End Figure 18.9 */
/* Figure 18.10 */
s = Server.local;
s.boot;

~sinosc = CtkSynthDef.new(\NRT_sinosc, 
	{arg outbus = 0, freq = 622.254, phase = 0, amp = 1, offSet = 0;
		Out.ar(outbus, SinOsc.ar(freq, phase, amp, offSet));
	}
);

(
var score;
var baseNote = 75;
var slopeTime = 0.25;
var curve = \sine;
var firstPitch = (baseNote + 0).midicps;
var firstStart = 0.0;
var firstDur = 5.0;
var firstAttackTime = slopeTime * 0.5;
var firstDecayTime  = slopeTime - firstAttackTime;
var firstVibDepth = 0.21;
var firstVibRate = 2.3;
var firstPeakAmp = 0.25;
var firstDecayAmp = 0.01;
var secondPitch = (baseNote - 4).midicps;
var secondStart = 2.2;
var secondDur = 4.0;
var secondAttackTime = slopeTime * 0.5;
var secondDecayTime  = slopeTime - secondAttackTime;
var secondVibDepth = 0.15;
var secondVibRate = 1.7;
var secondPeakAmp = 0.25;
var secondDecayAmp = 0.01;
var thirdPitch = (baseNote + 7).midicps;
var thirdStart = 3.1;
var thirdDur = 3.75;
var thirdAttackTime = slopeTime * 0.5;
var thirdDecayTime  = slopeTime - thirdAttackTime;
var thirdVibDepth = 0.21;
var thirdVibRate = 4;
var thirdPeakAmp = 0.2;
var thirdDecayAmp = 0.25;

score = CtkScore.new(
	~firstGroup = CtkGroup.new(firstStart, firstDur, server: s),
	~sinosc.new(firstStart, firstDur, \tail, ~firstGroup, server: s)
		.freq_(CtkControl.lfo(SinOsc, firstVibRate, 
			(firstPitch - ((firstPitch / (firstPitch.log2)) * (firstVibDepth * (1/3)))), 
			(firstPitch + ((firstPitch / (firstPitch.log2)) * (firstVibDepth * (2/3)))), 
			duration: firstDur, addAction: \head, target: ~firstGroup, server: s))
		.amp_(CtkControl.env(
			Env.new([0, firstPeakAmp, firstDecayAmp, 0], [firstAttackTime, 
				firstDur - (firstAttackTime + firstDecayTime), firstDecayTime], curve), 
			addAction: \head, target: ~firstGroup, server: s)),
	~secondGroup = CtkGroup.new(secondStart, secondDur, server: s),
	~sinosc.new(secondStart, secondDur, \tail, ~secondGroup, server: s)
		.freq_(CtkControl.lfo(SinOsc, secondVibRate, 
			(secondPitch - ((secondPitch / (secondPitch.log2)) * (secondVibDepth * (1/3)))), 
			(secondPitch + ((secondPitch / (secondPitch.log2)) * (secondVibDepth * (2/3)))), 
			duration: secondDur, addAction: \head, target: ~secondGroup, server: s))
		.amp_(CtkControl.env(
			Env.new([0, secondPeakAmp, secondDecayAmp, 0], [secondAttackTime, 
				secondDur - (secondAttackTime + secondDecayTime), secondDecayTime], curve), 
			addAction: \head, target: ~secondGroup, server: s)),
	~thirdGroup = CtkGroup.new(thirdStart, thirdDur, server: s),
	~sinosc.new(thirdStart, thirdDur, \tail, ~thirdGroup, server: s)
		.freq_(CtkControl.lfo(SinOsc, thirdVibRate, 
			(thirdPitch - ((thirdPitch / (thirdPitch.log2)) * (thirdVibDepth * (1/3)))), 
			(thirdPitch + ((thirdPitch / (thirdPitch.log2)) * (thirdVibDepth * (2/3)))), 
			duration: thirdDur, addAction: \head, target: ~thirdGroup, server: s))
		.amp_(CtkControl.env(
			Env.new([0, thirdPeakAmp, thirdDecayAmp, 0], [thirdAttackTime, 
				thirdDur - (thirdAttackTime + thirdDecayTime), thirdDecayTime], curve), 
			addAction: \head, target: ~thirdGroup, server: s))
);

score.play;
)
/* End Figure 18.10 */
/* Figure 18.11 */
VSO_Vib {

	var <pitch, <depth, <rate, <control;

	*new {arg start = 0.0, dur = nil, freq = 1, vibDepth = 0.21, vibRate = 1, 
		addAction = 0, target = 1, server;
		^super.new.initVSO_Vib(start, dur, freq, vibDepth, vibRate, addAction, 
			target, server);
	}
	
	initVSO_Vib {arg start, dur, freq, vibDepth, vibRate, add = 0, tgt = 1, server;
		server = server ?? {Server.default};
		pitch = freq;
		depth = vibDepth;
		rate = vibRate;
		control = CtkControl.lfo(SinOsc, rate, this.getLowerValue,
			this.getUpperValue, 0, start, dur, add, tgt, server: server);
	
	}
	
	getLowerValue {
		^(pitch - ((pitch / (pitch.log2)) * (depth * (1/3))));
	}

	getUpperValue {
		^(pitch + ((pitch / (pitch.log2)) * (depth * (2/3))));
	}
}
/* End Figure 18.11 */
/* Figure 18.12 */
VSO_ADR {

	var <control, <attackDur, <releaseDur, <totalDur;

	*new {arg start = 0.0, dur =  nil, peak = 0.707, decay = 0.01, attackDur = 0.125, 
		releaseDur = 0.125, addAction = 0, target = 1, server;
		^super.new.initVSO_ADR(start, dur, peak, decay, attackDur, releaseDur, addAction,
			target, server);
	}
	
	initVSO_ADR {arg start = 0.0, dur =  nil, peak = 0.707, decay = 0.01, aDur = 0.125, 
		rDur = 0.125, addAction = 0, target = 1, server;
		server = server ?? {Server.default};
		attackDur = aDur;
		releaseDur = rDur;
		totalDur = dur;
		control = CtkControl.env(Env.new([0, peak, decay, 0], 
			[attackDur, this.decayDur, releaseDur], \sine), 
			start, addAction, target, server: server, doneAction: 0);
	}
	
	decayDur {
		^(totalDur - (attackDur + releaseDur));
	}

}
/* End Figure 18.12 */
/* Figure 18.13 */
	initVSO {arg start = 0.0, dur = nil, freq = 622.254, ampPeakLevel = 0.707, 
		ampDecayLevel = 0.01, vibDepth = 0.21, vibRate = 3, addAction = 0, target = 1, 
			server;
		server = server ?? {Server.default};
		group = CtkGroup.new(start, dur, addAction: addAction, target: target, 
			server: server);
		freqCntl = VSO_Vib.new(start, dur, freq, vibDepth, vibRate, \head, group, server);
		ampCntl = VSO_ADR.new(start, dur, ampPeakLevel, ampDecayLevel, addAction: \head, 
			target: group, server: server);
		oscil = sinoscdef.new(start, dur, \tail, group, server)
			.freq_(freqCntl.control).amp_(ampCntl.control);
		score = CtkScore.new(group, oscil);
	}
/* End Figure 18.13 */
/* Figure 18.14 */
VSO {

	classvar <sinoscdef;
	var <score, group, oscil, freqCntl, <ampCntl;
	
	*new {arg start = 0.0, dur = nil, freq = 622.254, ampPeakLevel = 0.707, ampDecayLevel = 0.01, 
		vibDepth = 0.21, vibRate = 3, addAction = 0, target = 1, server;
		^super.new.initVSO(start, dur, freq, ampPeakLevel, ampDecayLevel, 
			vibDepth, vibRate, addAction, target, server);
	}

	*initClass {
		sinoscdef.isNil.if({
			sinoscdef = CtkSynthDef.new(\NRT_sinosc, 
				{arg outbus = 0, freq = 622.254, phase = 0, amp = 1, offSet = 0;
					Out.ar(outbus, SinOsc.ar(freq, phase, amp, offSet));
				})
			});
	}

	initVSO {arg start = 0.0, dur = nil, freq = 622.254, ampPeakLevel = 0.707, 
		ampDecayLevel = 0.01, vibDepth = 0.21, vibRate = 3, addAction = 0, target = 1, 
			server;
		server = server ?? {Server.default};
		group = CtkGroup.new(start, dur, addAction: addAction, target: target, 
			server: server);
		freqCntl = VSO_Vib.new(start, dur, freq, vibDepth, vibRate, \head, group, server);
		ampCntl = VSO_ADR.new(start, dur, ampPeakLevel, ampDecayLevel, addAction: \head, 
			target: group, server: server);
		oscil = sinoscdef.new(start, dur, \tail, group, server)
			.freq_(freqCntl.control).amp_(ampCntl.control);
		score = CtkScore.new(group, oscil);
	}

}
/* End Figure 18.14 */
/* Figure 18.15 */
s = Server.local;
s.boot;

(
var score;
var baseNote = 75;
var firstPitch = (baseNote + 0).midicps;
var secondPitch = (baseNote - 4).midicps;
var thirdPitch = (baseNote + 7).midicps;

score = CtkScore.new(
	(a = VSO.new(0.0, 5.0, firstPitch, 0.25, 0.01, 0.21, 2.3, server: s)).score,
	(b = VSO.new(2.2, 4.0, secondPitch, 0.25, 0.01, 0.15, 1.7, server: s)).score,
	(c = VSO.new(3.1, 3.75, thirdPitch, 0.15, 0.3, 0.21, 4, server: s)).score
);
score.play;
)
/* End Figure 18.15 */
/* Figure 18.16 */
s = Server.local;
s.boot;

(
var score;
var baseNote = 75;
var firstPitch = (baseNote + 0).midicps;
var secondPitch = (baseNote - 4).midicps;
var thirdPitch = (baseNote + 7).midicps;

d = [2.4, 1.7];
a = NRT_TimeFrame.new(0.0, 11);
b = NRT_TimeFrame.new(a.starttime + d.at(0), a.endtime - (a.starttime + d.at(0)));
c = NRT_TimeFrame.new(b.starttime + d.at(1), b.endtime - (b.starttime + d.at(1)));

score = CtkScore.new(
	(a = VSO.new(a.starttime, a.duration, firstPitch, 0.25, 0.01, 0.21, 2.3, server: s)).score,
	(b = VSO.new(b.starttime, b.duration, secondPitch, 0.25, 0.01, 0.15, 1.7, server: s)).score,
	(c = VSO.new(c.starttime, c.duration, thirdPitch, 0.15, 0.3, 0.21, 4, server: s)).score
);
score.play;
//N.B. You may notice three "Node not found" warnings.
// This is expected behavior for this version of Ctk
)
/* End Figure 18.16 */
/* Figure 18.17 */
NRT_TimeFrame {

	var >starttime, >duration;
	
	*new {arg starttime, duration;
		^super.newCopyArgs(starttime, duration);
	}

	starttime {
		^starttime.value;
	}

	duration {
		^duration.value;
	}

	endtime {
		^(this.starttime != nil).if({
			(this.duration != nil).if({
				//call the getter methods rather than accessing
				// the variables directly
				this.starttime + this.duration;
			}, {nil})
		}, {nil});
	}

}
/* End Figure 18.17 */