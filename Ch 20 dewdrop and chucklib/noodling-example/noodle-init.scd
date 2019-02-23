
//////// Algorithmic Noodling - H. James Harkins
//////// Definition file - load once at beginning of performance



// For ease of clean-up, all these objects will be identified with the subType \noodle
// future object creation should not be affected, so I have to save the current subType
var	saveSubtype = AbstractChuckArray.defaultSubType, master;

AbstractChuckArray.defaultSubType = \noodle;

protect {	// to reset the subType even in case of error

// I always keep a single master MixerChannel
// a previous piece in the same performance might have created it,
// so check before making a new one
master = ~master = ~master ?? { MixerChannel(\master, s, 2, 2, level: 1) };

// The separate environment is for shared resources.
// See the performance script file to see how it's used.
~noodle = Environment.make({

~master = master;	// master needs to be in this environment as well as topEnvironment

~rvbmc = MixerChannel(\reverb, s, 2, 2, level: 1, outbus: ~master, completionFunc: { |chan|
	chan.playfx({ |outbus|
		var	l, r;
		#l, r = In.ar(outbus, 2);
		FreeVerb2.ar(l, r, 1.0, 0.6, 0.5)
	});
});

// I know the path of the audio files relative to this file
// this gets the directory where this file is located
~root = PathName(thisProcess.nowExecutingPath).pathOnly;

TempoClock.default.tempo = 76/60;

//////// VOICER FACTORIES

/***
The factory contains an environment. When you instantiate the factory, the environment is copied and new resources, such as ~target, ~cbuf, ~mbuf in the first factory here, are saved into this environment. Both the voicer and the environment go into the VC storage object. When the voicer is no longer needed, the VC can enter its unique environment and call the ~free function to clean up these other objects as well.
***/

// "keys" are environment variables in the current environment to pass into the factory at creation time
(keys: #[master, rvbmc], make: {
	var	out;
		// ~master, one of the keys, is used here
		// the Voicer's output MixerChannel should always be called ~target in the factory
	~target = MixerChannel(\bass, s, 1, 2, level: -11.dbamp, outbus: ~master);
	~target.newPostSend(~rvbmc, 0.368);
		// other resources stored in the factory environment
	~cbuf = Buffer.alloc(s, 1024, 1, completionMessage: { |buf|
		buf.sine1Msg(#[1]);
	});
	~mbuf = Buffer.alloc(s, 1024, 1, completionMessage: { |buf|
		buf.sine1Msg(#[1, 1]);
	});
		// fm is more pronounced in a higher register
		// this SynthDef compensates by scaling mod_lev according to note frequency
		// if f = frequency, b = base frequency, m = mod lev, and k = scaling sensitivity,
		// scaled modulation is m / (((f / b) - 1) * k + 1)
		// fewer UGens as the algebraic equivalent m * b / ((f * k) + (b * (1-k)))
		// here b=102.2 and k=0.5 are hardcoded
	SynthDef(\fmbass, { |freq, gate, freqlag, cbuf, mbuf, mod_ratio, mod_lev, detune, car_vs, mod_vs, outbus|
		var sig, mod, car_amp, mod_amp;
		freq = Lag.kr(freq, freqlag);	// for monophonic portamento
		mod_lev = mod_lev * 102.2 / ((freq * 0.5) + (102.2*0.5));
			// "sensitivity" formula - scaling relative to 1.0, not 0
			// see the ddwSensitivity quark
		car_amp = Latch.kr((gate-1) * car_vs + 1, gate);
		mod_amp = Latch.kr((gate-1) * mod_vs + 1, gate)
			* EnvGen.kr(Env.adsr(0.01, 0.2, 0.4, 0.21), gate);
		mod = Osc.ar(mbuf, freq * mod_ratio + detune, 0, mod_amp * mod_lev);
		sig = Osc.ar(cbuf, freq + (mod * freq), 0, car_amp)
			* EnvGen.kr(Env.adsr(0.01, 1, 0.9, 0.05), gate, doneAction:2);
		Out.ar(outbus, sig);
	}).add;
	out = MonoPortaVoicer(1, \fmbass, [cbuf: ~cbuf, mbuf: ~mbuf, mod_ratio: 0.5,
		detune: 0.35, car_vs: 0.37, mod_vs: 0.56], target: ~target)
		.portaTime_(0.08);
	out.mapGlobal(\mod_lev, nil, 3.3, #[1.0, 12.0, \exp]);
	out	// ~make function must return the Voicer itself
}, free: { [~target, ~cbuf, ~mbuf].free }, type: \voicer) => Fact(\bass_v);


(keys: #[master, rvbmc], make: {
	var	f;
	~target = MixerChannel(\jumpy, s, 2, 2, -14.dbamp, outbus: ~master, completionFunc: nil);
	~target.newPostSend(~rvbmc, 0.9);
	
	f = { |i| (i / 14) * 0.3pi - 0.2 };
	~bufs = Buffer.allocConsecutive(6, s, 2048, 1, completionMessage: { |buf, j|
		buf.sine1Msg({ |i| (i%6 == j).if(0, { cos(f.(i)) }) } ! 14);
	});
	
	SynthDef(\jumpy, { arg freq, gate, bufbase, bufmod, vsens, detune, pan,
			ffreq, rq, lag = 0.1, outbus;
		var sig, amp, buf;
		amp = Latch.kr((gate-1) * vsens + 1, gate);
			// the bufenv should know how many buffers it's covering
		buf = bufbase + bufmod;
		sig = VOsc3.ar(buf, freq/detune, freq, freq*detune, amp)
			* EnvGen.kr(Env.adsr(0.01, 0.07, 0.4, 0.08), gate, doneAction:2);
		ffreq = Lag.kr(ffreq, lag);
		rq = Lag.kr(rq, lag);
		pan = Lag.kr(pan, lag);
		Out.ar(outbus, Pan2.ar(RLPF.ar(sig, ffreq, rq), pan));
	}).add;
	
	MonoPortaVoicer(1, \jumpy, [bufbase: ~bufs.first, vsens: 0.677, detune: 0.99606], target:~target).portaTime_(0.1);
}, free: { [~target, ~bufs].free }, type: \voicer) => Fact(\jumpy_v);


(keys: #[master, rvbmc], make: {
	var	out;
	~target = MixerChannel(\pad, s, 2, 2, level: -20.dbamp, outbus: ~master, completionFunc: { |chan|
//		chan.playfx(Instr("busfx.chorus2"), [20, 1, 2, 3, 0.0099076721080885, 0.12388629239938, 0.0026448039935914, 0.7306029426953, 0.4]);
				// standard mono-to-stereo chorusing technique
		var	chorus = chan.playfx({ |outbus|
			var	sig = In.ar(outbus, 1) * 0.4,
				mods = { |i|
					SinOsc.kr(0.12388629239938 * rrand(0.9, 1.1),
						0.7306029426953 * i,
						0.0026448039935914, 0.0099076721080885);
				} ! (3 * 2);
			sig = DelayC.ar(sig, 0.5, mods);
			Mix(sig.clump(2))
		});
		chan.newPostSend(~rvbmc, 0.6);
			// "set it and forget it" multiband EQ class in my library
			// there is also MultiEQ for EQs whose parameters you'll need to change
		StaticEQ.new(2, \eq, 551.972, -6.383.dbamp, 1.054, \hishelf, 1944.083, 4.681.dbamp, 1)
			.play(chan);
			// make sure chorus precedes EQ
		SystemClock.sched(0.5, { chorus.moveToHead(chan.effectgroup) });
	});
	
		// This is a trick I like to use to save CPU for pads.
		// Usually you play thick chords on pads, and band limited oscillators 
		// are not CPU cheap. But, wavetable oscillators alias at higher
		// frequencies. This technique uses an array of wavetables, with
		// successively decreasing harmonic content. Note frequency
		//maps onto the buffer with the right number of partials,
		// so you get speed and a clean sound.
	~bufs = Buffer.allocConsecutive(8, s, 2048, 1, completionMessage: { |buf, i|
		var	base = 48 + (i * 12),		// max here is 132, just above highest MIDI note number
			numHarmonics = (20000 / base.midicps).asInteger;
		buf.sine1Msg((1..numHarmonics).reciprocal)
	});
	
	SynthDef(\padmap, { |freq, gate, bufbase, numbufs, vsens, detune,
			ffreq, rq, outbus|
		var	amp = Latch.kr((gate-1) * vsens + 1, gate),
			top = (127.midicps / 48.midicps).log2,
			freqmap = (freq / 48.midicps).log2,
			bufmod = LinLin.kr(freqmap, 0, top, 0, numbufs-1).clip(0, numbufs-1.01),
			buf = bufbase + bufmod,
			sig;
		sig = VOsc3.ar(buf, freq/detune, freq, freq*detune, amp);
		sig = RLPF.ar(sig, ffreq, rq)
			* EnvGen.kr(Env.adsr(0.4, 0.5, 0.8, 1.8), gate, doneAction:2);
		Out.ar(outbus, sig)
	}).add;
	
	out = Voicer(20, \padmap, [bufbase: ~bufs.first, numbufs: ~bufs.size, vsens: 0.3,
		detune: 1.00394], target: ~target);
	out.mapGlobal(\ffreq, nil, 1200, \freq);
	out.mapGlobal(\rq, nil, 1.0, [1.0, 0.05, \exp]);
	
	out
}, free: { [~target, ~bufs].free }, type: \voicer) => Fact(\pad_v);



//////// PROCESS FACTORIES

// silent process to determine harmonic context
// writes current bass note into Library under key \currentBassNote
// also sends notification -- we'll use this for a bass arpeggiator
(make: { |name|
	PR(\aiBass) => BP(name);
	MBM(0)[\roots] => BP(name);	// source melody data
	MBM(0)[\rootsadp] => BP(name);	// data for generating variations
	BP(name).useRh = true;
		// Normally in a melodic process, you want to preserve the original rhythm.
		// Here, the rhythm determines harmonic rhythm,
		// which I want to generate randomly within a specific range.
		// So I supply a rhythm profile, which is a Proto object that defines
		// a pattern to return durations and lengths.
	BP(name).rhythm = PR(\abstractProcess).copy.make({
		~asPattern = {
			~deltaStream ?? { ~deltaStream = ~delta.asStream };
			Pfunc(e { ~deltaStream.next.dup });
		};
		~delta = Pwhite(5, 14, inf) * 0.5;	// (2.5, 3.0 .. 7.0)
	});
		// the adaptation techniques to use
	Prand(#[absSplice, intSplice], inf) =>.adapt BP(name);
		// validation: all notes in a new variation must be between
		// scale degrees 17 and 28
	BP(name).eugTest = \eugRangeOnlyTest;
	BP(name).range = NumericRange(17, 28);
		// timing correction for data sharing - see readme
	BP(name).leadTime = 0.1;	// roots must fire before everyone else
	BP(name)
}, type: \bp) => Fact(\roots);


// The bassline is technically an arpeggiator process that spins out figuration over the last root note chosen by the \roots process. It is a nested process -- the inner layer, PR(\arpeg1), generates the notes, and the outer layer, PR(\macroRh), populates data per harmony event that \arpeg1 needs.

(make: { |name|
	BP(name).free;
	PR(\arpeg1) => BP(name);	// child process (inner layer)
		// Func(\fitToBass) defines how to fit figuration to harmonic context
	BP(name).fitFunc = \fitToBass;
	PR(\macroRh) => BP(name);	// wrap inner layer inside outer layer
	BP(name).bassUpdate = false;
	MBM(0)[\bassfigs] => BP(name);	// source "chords"
		// a dummy "macrorhythm" that reads the data from the last root note
	Pfunc({ [~parentDelta, ~parentLength] }) =>.macro BP(name);
	\bass1 =>.micro BP(name);	// per-note rhythm
	\xrand =>.arpeg BP(name);	// order to arpeggiate notes
	\gdor.asMode => BP(name);	// mode, or "key"
	BP(name).leadTime = 0.1;	// schedules the same as roots
	BP(name)
}, type: \bp) => Fact(\bass);

// This defines the observer process that listens for new root notes, and passes the information to the bass process.
(make: { |name|
		// BP(\roots) is the process whose events we're listening for
		// it must exist before initializing the link process
	BP(\roots).exists.not.if({
		Fact(\roots).makev;
	});
	PR(\basslink).chuck(BP(name), parms: (
		driver: \roots,	// I'm listening to \roots
		children: (
				// I could have any number of child processes
				// here there is just one, for the bass
				// quant: 0 means to trigger the base event immediately
				// when receiving the notification
				// func declares how to pass information from parent to child
			bass: (quant: 0, func: { |name, parentEvent|
					// func communicates parent event data into child
				BP(name).parentDelta = parentEvent[\delta];
				BP(name).parentLength = parentEvent[\delta];
			})
		)
	));
}, type: \bp) => Fact(\basslink);

// Pad process -- Func(\makeCh) is a shortcut that sets the most important arpeggiator properties in one step. (I do this whole lot, so it makes sense to have a macro.)

// This is not so directly tied to the root process, but it uses special techniques to make sure it starts on the next root note and remains in sync thereafter.
(make: { |name|
	Func('makeCh').doAction(name, \arpeg1, \macroRh, MBM(0)[\padch], nil, MacRh(\lagger), \pad2, \reshuf, nil, \gdor);

		// Harmonies can, if desired, follow the contour of a top note melody
		// this can be an instance of PR(\aiMel) for variations on given note data,
		// or, as here, a process that generates the pitches from a pattern
	BP(name).topNote = PR(\patternTop).v.copy.make({
		~deg = Pvbrown(37, 52, Pwhite(1, 3, inf), inf);
		~mode = \gdor;
	});

		// Func(\chordFitInt) rearranges chord intervals against the top note
		// for best consonance over the current bass note
	BP(name).child.fitFunc = \chordFitInt;
		// a function that calculates when to start based on the root's next event time
	BP(name).quant = Func(\syncToRoot).v;
		// since the lead time is smaller than that of roots,
		// this will execute later than the root process and can use its information
		// since it's nonzero, other processes can also grab information from the pad
	BP(name).leadTime = 0.05;
	BP(name)
}, type: \bp) => Fact(\pad);


// More special techniques, not only to stay synchronized with the root, but also to play the same notes as the pad.
(make: { |name|
	Func('makeCh').doAction(name, \arpeg1, \macroRh, MBM(0)[\padch], nil, MacRh(\lagger), \jumpy, \xrand, nil, \gdor);
	BP(name).quant = Func(\syncToRoot).v;
		// Since the child process generates the notes,
		// we can attach arbitrary parameters for the Voicer
	BP(name).child.argPairs = [\bufmod, Pwhite(0.0, 4.99, inf), \ffreq, Pexprand(1200, 14000, inf), \rq, Pexprand(0.05, 0.5, inf)];

		// look to the pad for the chord object to use
		// if that isn't populated or isn't being updated,
		// choose randomly from my own chord pool
	BP(name).child[\makeChordPattern] = {
		Pfunc({
			BP(\pad).isPlaying.if({ BP(\pad).child.currentChord }) ?? { ~chords.choose }
		})
	};
	BP(name).child.chordStream = nil;	// ... and make sure it takes effect
	BP(name).child.reset;

		// to get the same notes, the top note has to match also
		// the top note pattern is kept as a fallback in case the pad isn't running
	BP(name).topNote = PR(\patternTop).clone({
		~basePattern = ~asPattern;
		~deg = Pvbrown(37, 52, Pwhite(1, 3, inf), inf);
		~mode = \gdor;
		~asPattern = {
			var	base = ~basePattern.value.asStream;
			Pfunc({ |inevent|
				BP(\pad).isPlaying.if({ BP(\pad).lastEvent.top },
					{ base.next(inevent) })
			});
		};
	});
	BP(name)
}, type: \bp) => Fact(\jumpy);


// A couple of generative drum machines to round out the ensemble.
// All the setup takes place using the chuck operation's parameter list!
(keys: #[master, root], make: { |name|
	PR(\bufPerc).chuck(BP(name), parms: (
		master: ~master,
			// which sound file to play
			// one drum machine process can choose between several buffers
		bufPaths: [~root ++ "samples/mellowbd.aif"],
			// user specific initialization takes place when the MixerChannel is ready
		postMCCreation: { |chan|
			StaticEQ(1, \eq, 1879.042, 5.532.dbamp, 0.405, \eq, 82.098, 4.681.dbamp, 1).play(chan);
			chan.level = -8.4.dbamp;
		},
		amps: 0,
			// used as the basic rhythm for the generator function
			// the loudest stroke will be on the first beat of the bar
		ampBase: [1] ++ (0 ! 15),
			// This executes once per bar to generate a new rhythm.
		pbindPreAction: {
			var	pool = (1..15).scramble;
			~amps = ~ampBase.copy;
			rrand(1, 5).do({ |i|
				~amps[pool[i]] = rrand(0.2, 0.7);
			});
		}
	));
}, type: \bp) => Fact(\kik);

(keys: #[master, root, rvbmc], make: { |name|
	PR(\bufPerc).chuck(BP(name), parms: (
		master: ~master,
		rvbmc: ~rvbmc,
		bufPaths: [~root ++ "samples/springverb-rimshot.aif"],
		rates: [1.15],
		postMCCreation: { |chan|
			StaticEQ(1, \eq, 180.488, -14.61.dbamp, 0.052, \eq, 287.845, -15.745.dbamp, 0.052,
				\eq, 4180.489, 3.262.dbamp, 0.589).play(chan);
			chan.newPostSend(~rvbmc, 0.6);
			chan.level = -7.6.dbamp;
		},
		amps: 0,
		ampBase:
			#[	[0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0.4, 0, 0.8, 0, 0],
				[0, 0, 0, 0, 0, 0.4, 0, 0, 1, 0, 0, 0, 0, 0.5, 0, 0],
				[0, 0, 0, 0, 0.7, 0, 1, 0, 0, 0.4, 0, 0, 0, 0, 0, 0]
			],
		pbindPreAction: {
			var	pool;
			~amps = ~ampBase.choose.copy;
				// locate rests in the base rhythm
			pool = ~amps.collectIndicesOfItem(0).scramble;
				// then put extra notes only in the rests
			rrand(0, 3).do({ |i|
				~amps[pool[i]] = rrand(0.2, 0.7);
			});
		}
	));
}, type: \bp) => Fact(\stick);


//////// PROCESS PROTOTYPES

// bass needs to follow root process exactly
// this listens for the notification from \roots and transmits info to the bass process
// this implements the Observer design pattern
Proto({
		// set at chuck time
	~bassID = \currentBassNote;	// bass ID to listen for
	~children = ();	// e.g., (childBPName: (quant: 0, func: {}), ...)
	~clock = TempoClock.default;
	
	~niltime = NilTimeSpec.new;	// you should not change this
	
	~prep = {
		~clock = BP(~driver).tryPerform(\clock) ?? { ~clock };  // clocks must match
		~registerNotify.value;
	};
	
	~registerNotify = {
			// bass note notifications go through BP class
			// adding currentEnvironment means ~update below will respond
		BP.addDependant(currentEnvironment);
	};
	
	~unregisterNotify = {
		BP.removeDependant(currentEnvironment);
	};

		// This is what makes this process into an Observer
	~update = { |obj, changeID, parentEvent|
			// notification might be from something else
			// reject if not the right bassID
		(changeID == ~bassID).if({
			~playChildren.(parentEvent);
		});
	};

	~playChildren = { |parentEvent|
		~children.keysValuesDo({ |bpname, spec|
				// for each child, find out when it should trigger
				// at that time, pass information across and run exactly one event
			~clock.schedAbs(spec[\quant].asTimeSpec.nextTimeOnGrid(~clock), e {
				spec[\func].value(bpname, parentEvent);
				BP(bpname).triggerOneEvent(~niltime);
				nil
			});
		});
	};

		// dependents should not be left around when not in use
	~freeCleanup = {
		~unregisterNotify.value;
	};
}) => PR(\basslink);


//////// PROCESS COMPONENTS

// The arpeggiator prototype (used above for bass, pad and jumpy) uses other objects, pulled by name from their respective repositories.

// A "microrhythm" pattern supplies the note delta, length (for staccato or overlap) and gate (velocity) as a three element array.
Pwrand([1.0, 0.75, 0.5, Pn(0.25, 2), Pseq(#[0.25, 0.5, 0.25], 1)], #[2, 3, 2, 0.5, 0.4].normalizeSum, inf)
.collect({ |delta|
	[delta,
	delta * #[0.2, 0.5, 0.9, 1.001].wchoose(#[1, 2, 4, 6].normalizeSum),
	rrand(0.2, 0.8)]
}) => MicRh(\bass1);

{ |notes, event| [event.delta, event[\length], 0.5] } => MicRh(\pad);
{ |notepat, event| Ptuple([Pwhite(0.2, 0.6, inf), Pwhite(1.0, 3.0, inf), 0.5], inf) } => MicRh(\pad2);

{ Pwrand([
	Pn(0.125, 4),
	Pseq(#[0.125, 0.25, 0.125], 1),
	Pn(0.125, 2),
	0.25,
	Pseq(#[0.375, 0.125], 1)
], #[1, 2, 3, 4, 2].normalizeSum, inf).collect({ |delta|
	[delta, #[0.4, 0.9, 1.1].wchoose(#[1, 3, 5].normalizeSum) * delta, exprand(0.2, 0.8)]
})
} => MicRh(\jumpy);


// "Macrorhythm" supplies the time between chords in the arpeggiator.
// [delta, length], where length is the amount of time to arpeggiate:
// if length < delta, the arpeggiation will stop early and wait for the next parent event
Pfunc({ [~parentDelta, ~parentLength] }) => MacRh(\depn);
// This macrorhythm looks at the last bass note and returns the same amount of time
// so that a harmony process will run in lockstep with the root changes
Pfunc({ (Library.at(\currentBassNote).tryPerform(\dur) ?? { rrand(8, 16) * 0.5 }).dup }) => MacRh(\lagger);


// This pattern determines the order in which chord notes will play out.
{ |notes| Pn(Pshuf(notes, 1), inf) } => ArpegPat(\reshuf);


// Since I use this function in a couple of places, I save it in the function repository for convenience.
// This calculates when process should start to sound in sync with the next root change.
{ |bp|
	var	rootbeat = BP(\roots).nextBeat ?? { BP(\roots)[\eventSchedTime] };
	(rootbeat.notNil and: { rootbeat >= bp.clock.beats }).if({
		AbsoluteTimeSpec(rootbeat + BP(\roots).leadTime - bp.leadTime)
	}, {
		NilTimeSpec.new	// start now, but results might be unpredictable
	});
} => Func(\syncToRoot);


//////// SOURCE MIDI DATA

// What key?
ModalSpec(#[0, 2, 3, 5, 7, 9, 10], 12, 7) => Mode(\gdor);

// Global repository for MIDIRecBuf objects
MBM(0) ?? { MIDIBufManager.new => MBM.prNew(0) };

// given in MIDI note numbers
// when used in a BP, the process converts them to their modal representation
MIDIRecBuf(\roots, [
		// midi note numbers
	#[31, 38, 34, 41, 36, 26, 29, 31, 24, 33, 36] + 12,
		// note deltas
	#[6, 4, 2, 5, 1, 6, 3, 2, 4, 5, 3],
		// note lengths
	#[6, 4, 2, 5, 1, 6, 3, 2, 4, 5, 3],
		// velocities/gates
	0.5
		// parameters:
		// mode: \gdor --> Mode(\gdor) for modal conversion
		// splitFunc: divides raw note stream into phrases using Func(...)
].asNotes, (mode: \gdor, splitFunc: \defaultMelSplit)) => MBM(0);

MIDIRecBuf(\rootsadp, [
	#[36, 34, 33, 31, 33, 29, 31, 33, 38, 36] + 12,
	1, 1, 0.5
		// parameter type specifies how these data will be used
		// \adapt means this is not meant to play directly,
		// but will crossbreed with the main material for new variations
].asNotes, (mode: \gdor, type: \adapt)) => MBM(0);

MIDIRecBuf(\bassfigs, [
	#[31, 38, 41, 43,  31, 34, 36, 41,  31, 36, 38, 43], 
	#[1, 1, 1, 2,  1, 1, 1, 2,  1, 1, 1, 2],
	1,
	1
		// type: \ch means these are chord data
].asNotes, (mode: \gdor, type: \ch)) => MBM(0);

MIDIRecBuf(\padch, [
	#[55, 60, 62, 65, 70,  55, 58, 64, 65, 72,  61, 65, 70, 72,  57, 67, 70, 72, 77],
	#[1, 1, 1, 1, 2,  1, 1, 1, 1, 2,  1, 1, 1, 2,  1, 1, 1, 1, 2],
	1, 1
].asNotes, (mode: \gdor, type: \ch)) => MBM(0);

~cleanup = { |self|
	self[\rvbmc].free;
};
});

} {
	AbstractChuckArray.defaultSubType = saveSubtype;
};

~noodle.know = true;	// to facilitate cleanup, see script file
