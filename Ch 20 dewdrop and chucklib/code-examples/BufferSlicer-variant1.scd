
//////// First step in evolving the buffer slicer from Figure 20.9 into a more flexible prototype.
//////// Here we add support for multiple buffers, from arrays of paths and coordinates.
//////// A pattern, ~buffer, will choose which buffer to use for each event.

// This is a copy of the Figure 20.9 prototype with additions, highlighted by comments.

(
SynthDef(\bufGrainPan, { |start, time, bufnum, pan, rate = 1, amp = 1,
		attack = 0.001, decay = 0.02, outbus|
	var sig;
	sig = PlayBuf.ar(1, bufnum, rate * BufRateScale.kr(bufnum), 1, start, 0)
		* EnvGen.kr(Env.linen(attack, time, decay), doneAction:2);
	Out.ar(outbus, Pan2.ar(sig, pan, amp));
}).add;

PR(\abstractProcess).clone({
	~event = (eventKey: \singleSynthPlayer);
	~quant = 0;

	~delta = Pwhite(1, 5, inf) * 0.1;
	~tfactor = 1;
	~maxStart = Pfunc({ |ev| ev[\bufnum].duration }) - Pkey(\time);
	~startSec = Pwhite(0.0, BPStream(\maxStart), inf);
	~pan = Pwhite(-1.0, 1.0, inf);
	~amp = 1;
	~rate = 1;
	~buffer = Pfunc({ ~bufs.choose });

	~asPattern = {
		Pbind(
			\chan, ~chan,
			\instrument, \bufGrainPan,
			\rate, BPStream(\rate),	// reference to ~rate stream
			\delta, BPStream(\delta),
			\bufnum, BPStream(\buffer),
			\time, BPStream(\tfactor) * Pkey(\delta),
			\start, BPStream(\startSec) * Pfunc({ |ev| ev[\bufnum].sampleRate }),
			\pan, BPStream(\pan),
			\amp, BPStream(\amp)
		)
	};

	~paths = ["sounds/a11wlk01.wav"];	// now an array of strings
		// [startFrame, numFrames] for each path in ~paths
		// if this array is shorter, the path index will be wrapped
		// this default will load the complete soundfile for every path
	~bufCoords = [[0, -1]];

	~prep = {
		~chan = MixerChannel(~collIndex, s, 2, 2);
		~loadBufs.();	// move buffer loading to a separate method for modularity
	};

	~loadBufs = {
		~bufs.do({ |buf| buf.free });
		~bufs = ~paths.collect({ |path, i|
			~loadOneBuf.(path, *~bufCoords.wrapAt(i));
		});
	};
	
	~loadOneBuf = { |path, startFrame, numFrames|
		if(thisProcess.platform.name == \linux and: { path[..5] == "sounds" }) {
			path = Platform.systemAppSupportDir +/+ path;
		};
		"loading %\n".postf(path);
		Buffer.read(s, path, startFrame, numFrames,
			action: { "done loading %\n".postf(path) });
	};
	
		// why not? you should be able to add and remove buffers on the fly
		// removing a buffer if a synth is using it is maybe risky, though
	~addBuf = { |path, startFrame, numFrames|
		~bufs = ~bufs.add(~loadOneBuf.(path, startFrame, numFrames));
	};
	
	~removeBufAt = { |index|
		~bufs[index].free;
		~bufs.removeAt(index);
	};

	~freeCleanup = {
		[~chan, ~bufs].free;
	};
}) => PR(\bufSlicer);
)

PR(\bufSlicer).chuck(BP(\z), parms: (paths: ["sounds/a11wlk01.wav", "sounds/a11wlk01-44_1.aiff"]));

BP(\z).play;

BP(\z).tfactor = sin(Pseries(0, 0.1, inf)) * 0.45 + 0.55;
BP(\z).delta = Pstutter(Pwhite(4, 12, inf), Pxrand((1..6) * 0.05, inf));
BP(\z).startSec = Pclutch(Pwhite(0.0, BPStream(\maxStart), inf), Pdiff(Pkey(\delta)).abs > 0);
BP(\z).buffer = Pclutch(Pfunc({ ~bufs.choose }), Pdiff(Pkey(\delta)).abs > 0);
BP(\z).pan = sin(Ptime(inf));

BP(\z).stop;

BP(\z).free;
