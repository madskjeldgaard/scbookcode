
//////// Second step in evolving the buffer slicer from Figure 20.9 into a more flexible prototype.
//////// Upping the ante further... now we support multiple synthdefs.
//////// The main pattern does not know in advance which streams to use,
//////// until the specific def is chosen. So, I invent a data structure that holds
//////// the SynthDef and patterns for each argument. At initialization, these
//////// patterns are written into the environment and can be tweaked as in
//////// previous examples.

(
PR(\abstractProcess).clone({
	~event = (eventKey: \singleSynthPlayer);
	~quant = 0;

	~delta = Pwhite(1, 5, inf) * 0.1;
	~def = Pfunc({ ~defs.keys.choose });
	~buffer = Pfunc({ ~bufs.choose });
	~maxStart = Pfunc({ |ev| ev[\bufnum].duration }) - Pkey(\time);

	~basePattern = {
		Pbind(
			\chan, ~chan,
			\def, BPStream(\def),
			\instrument, Pfunc({ |ev| ~defs[ev[\def]].name }),
			\delta, BPStream(\delta),
			\bufnum, BPStream(\buffer)
		)
	};
	
	~userPattern = { nil };
	
	~asPattern = {
		var	userpat = ~userPattern.();
		userpat.notNil.if({
			Pchain(userpat, ~basePattern.())
		}, {
			~basePattern.()
		}).collect({ |ev|
			~defs[ev[\def]].argStreams.pairsDo({ |name, value|
				ev.put(name, value.next(ev));
			});
			ev
		});
	};

	~paths = ["sounds/a11wlk01.wav"];
	~bufCoords = [[0, -1]];

	~prep = {
		~chan = MixerChannel(~collIndex, s, 2, 2);
		~loadBufs.();
		~initSynthDefs.();
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
	~addBuf = { |path, startFrame, numFrames|
		~bufs = ~bufs.add(~loadOneBuf.(path, startFrame, numFrames));
	};
	~removeBufAt = { |index|
		~bufs[index].free;
		~bufs.removeAt(index);
	};

	~initSynthDefs = {
		var	sdef;
		~defs = ~defs.value;
		~defs.keysValuesDo({ |id, defDict|
			sdef = defDict[\def];
			defDict[\name] = sdef.tryPerform(\name) ? sdef;
			sdef.tryPerform(\send, ~chan.server);
			sdef.tryPerform(\add);
			~initArgsForSynthDef.(id);
		});
	};
	
	~initArgsForSynthDef = { |id|
		var	streamarray = Array.new(~defs[id][\args].size), fullname;
		~defs[id][\args].pairsDo({ |name, pattern|
				// assigning a Pattern to a BP environment variable automatically creates the stream
			fullname = (id ++ name).asSymbol;
			fullname.envirPut(pattern.asPattern);
			streamarray.add(name).add(BPStream(fullname).asStream);
		});
		~defs[id][\argStreams] = streamarray;
	};
	
	~defs = (
		basic: (def: SynthDef(\bufGrainPan, { |start, time, bufnum, pan, rate = 1, amp = 1,
						attack = 0.001, decay = 0.02, outbus|
					var sig;
					sig = PlayBuf.ar(1, bufnum, rate * BufRateScale.kr(bufnum), 1, start, 0)
						* EnvGen.kr(Env.linen(attack, time, decay), doneAction:2);
					Out.ar(outbus, Pan2.ar(sig, pan, amp));
				}),
			args: [
				rate: 1,
				tfactor: 1,
				time: Pkey(\tfactor) * Pkey(\delta),
				startSec: Pwhite(0.0, BPStream(\maxStart), inf),
				start: Pkey(\startSec) * Pfunc({ |ev| ev[\bufnum].sampleRate }),
				pan: Pwhite(-1.0, 1.0, inf),
				amp: 1
			]),
		stretch: (def: SynthDef(\bufStretch, { |start, time, bufnum, pan, rate = 1, amp = 1,
						stretch = 1, attack = 0.001, decay = 0.02, outbus|
					var sig;
					sig = PlayBuf.ar(1, bufnum, rate * BufRateScale.kr(bufnum) / stretch, 1, start, 0);
					sig = PitchShift.ar(sig, pitchRatio: stretch)
						* EnvGen.kr(Env.linen(attack, time, decay), doneAction:2);
					Out.ar(outbus, Pan2.ar(sig, pan, amp));
				}),
			args: [
				rate: 1,
				tfactor: 1,
				stretch: Pexprand(2.0, 4.0, inf),
				delta: Pkey(\delta) * Pkey(\stretch),
				time: Pkey(\tfactor) * Pkey(\delta),
				startSec: Pwhite(0.0, Pfunc({ |ev| ev[\bufnum].duration }) - (Pkey(\time) / Pkey(\stretch))),
				start: Pkey(\startSec) * Pfunc({ |ev| ev[\bufnum].sampleRate }),
				pan: Pwhite(-1.0, 1.0, inf),
				amp: 1
			])
	);

	~freeCleanup = {
		[~chan, ~bufs].free;
	};
}) => PR(\bufSlicer);
)

PR(\bufSlicer).chuck(BP(\z), parms: (paths: ["sounds/a11wlk01.wav", "sounds/a11wlk01-44_1.aiff"]));

BP(\z).play;

BP(\z).def = Pwrand(#[basic, stretch], [0.9, 0.1], inf);

// tfactor, startSec and pan are defined separately for each synthdef
// so, based on the Proto definition, I must prepend the synthdef's key
// delta and buffer are global for all synthdefs, so no prepend
BP(\z).basictfactor = sin(Pseries(0, 0.1, inf)) * 0.45 + 0.55;
BP(\z).delta = Pstutter(Pwhite(4, 12, inf), Pxrand((1..6) * 0.05, inf));
BP(\z).basicstartSec = Pclutch(Pwhite(0.0, BPStream(\maxStart), inf), Pdiff(Pkey(\delta)).abs > 0);
BP(\z).buffer = Pclutch(Pfunc({ ~bufs.choose }), Pdiff(Pkey(\delta)).abs > 0);
BP(\z).basicpan = sin(Ptime(inf));

BP(\z).stop;

BP(\z).free;


/*
Here, the chucking parms dictionary is used to add a new synthdef, not as part of the prototype but just in the instance. To avoid polluting the original defs dictionary, the parameter should copy the original and put new item(s) into it -- PR(\bufSlicer).defs.copy.put(\grains, ...).
*/

(
PR(\bufSlicer).chuck(BP(\z), parms: (
	paths: ["sounds/a11wlk01.wav", "sounds/a11wlk01-44_1.aiff"],
	defs: PR(\bufSlicer).defs.copy.put(
		\grains, (def: SynthDef(\grainspray, { |center = 0.5, width = 0.1,
				durStart = 0.01, durEnd = 0.01, density = 5,
				attack = 0.001, time = 1, decay = 0.02, amp = 1, bufnum, outbus|
			var	trig = Dust.kr(density),
				rate = TExpRand.kr(0.25, 4.0, trig),
				dur = XLine.kr(durStart, durEnd, time),
				pan = TRand.kr(-1.0, 1.0, trig),
				posRand = TRand.kr(width.neg, width, trig),
				sig = TGrains.ar(2, trig, bufnum, rate, center * BufDur.kr(bufnum) + posRand,
					dur, pan, amp);
			Out.ar(outbus, sig * EnvGen.kr(Env.linen(attack, time, decay), doneAction: 2))
		}),
		args: [
			center: Pwhite(0.05, 0.95, inf),
			width: min(Pwhite(0.05, 0.2, inf), min(Pkey(\center), 1.0 - Pkey(\center))),
			time: Pkey(\delta),
			density: Pwhite(20.0, 50.0, inf)
		])
	)
));
)

BP(\z).play;

BP(\z).def = Pwrand(#[basic, stretch, grains], [0.8, 0.15, 0.05], inf);

// Manipulating other streams is an exercise for the reader.

BP(\z).stop;

BP(\z).free;
