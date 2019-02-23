Figure 20.9
(
SynthDef(\bufGrainPan, { |start, time, bufnum, pan, rate = 1, amp = 1,
		attack = 0.001, decay = 0.02, outbus|
	var sig;
	sig = PlayBuf.ar(1, bufnum, rate * BufRateScale.kr(bufnum), 1, start, 0)
		* EnvGen.kr(Env.linen(attack, time, decay), doneAction:2);
	Out.ar(outbus, Pan2.ar(sig, pan, amp));
}).add;

PR(\abstractProcess).clone({
		// BEHAVIORAL components
		// eventKey refers to ProtoEvent(\singleSynthPlayer)
	~event = (eventKey: \singleSynthPlayer);
		// here, manufacture the pattern to play
	~asPattern = {
		Pbind(
			\chan, ~chan,
			\instrument, \bufGrainPan,
			\bufnum, ~buf.bufnum,
			\delta, BPStream(\delta),	// reference to ~delta stream
			\time, BPStream(\tfactor) * Pkey(\delta),
			\start, BPStream(\startSec) * ~buf.sampleRate,
			\pan, BPStream(\pan),
			\amp, BPStream(\amp)
		)
	};
		// default Pbind streams
	~delta = Pwhite(1, 5, inf) * 0.1;
	~tfactor = 1;
	~maxStart = Pfunc({ ~buf.duration }) - Pkey(\time);
	~startSec = Pwhite(0.0, BPStream(\maxStart), inf);
	~pan = Pwhite(-1.0, 1.0, inf);
	~amp = 1;

		// ARCHITECTURAL components
	~path = "sounds/a11wlk01.wav";	// default soundfile
	~startFrame = 0;
	~numFrames = -1;
		// constructor: auto-load soundfile and create mixer
	~prep = {
		~chan = MixerChannel(~collIndex, s, 2, 2);
		"loading %\n".postf(~path);
		~buf = Buffer.read(s, ~path, ~startFrame, ~numFrames,
			action: { "done loading buffer".postln });
	};
		// destructor, called on .free
	~freeCleanup = {
		[~chan, ~buf].free;
	};
}) => PR(\bufSlicer);
)

// That was the definition. In performance you only need this to play it.

PR(\bufSlicer) => BP(\columbia);
BP(\columbia).play;

// change process’s stream references while it is playing

BP(\columbia).tfactor = 0.25;

BP(\columbia).tfactor = sin(Pseries(0, 0.1, inf)) * 0.45 + 0.55;

BP(\columbia).delta = Pstutter(Pwhite(4, 12, inf), Pxrand((1..6) * 0.05, inf));

BP(\columbia).startSec = Pclutch(Pwhite(0.0, BPStream(\maxStart), inf), Pdiff(Pkey(\delta)).abs > 0);


// also valid to chuck patterns into a BP
// ".pan" is an adverb indicating which variable to replace
sin(Ptime(inf)) =>.pan BP(\columbia);

BP(\columbia).stop;
BP(\columbia).free;	// MixerChannel and Buffer go away also


