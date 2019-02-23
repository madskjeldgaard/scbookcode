/*****

This file contains all the code examples from Chapter 20 of The SuperCollider Book.

Most of them require the server to be started. A few use graphics -- if you are using SwingOSC, make sure the SwingOSC server is booted as well.

IMPORTANT NOTE FOR SWINGOSC USERS:
Please also execute this line before running the code examples. Otherwise, some '.gui' statements will fail.

Quarks.install("ddwGUIEnhancementsForSwingOSC");

Macintosh users should *not* install the GUI enhancements for SwingOSC.


See also the example files BufferSlicer-variant1.sc and BufferSlicer-variant2.sc, which take the buffer slicer process (Figure 20.9) to a higher degree of complexity and reusability.

Figure numbers missing from this file are graphics.

--- H. James Harkins

*****/



////////////////////////////////////////
//////// Figure 20.1.
//////// MixerChannel creation, SynthDef conventions, Synth playing

// create the mixer: one channel in, two channels out
s.boot;

m = MixerChannel(\fig1, s, 1, 2);

// bring up a mixing board -- you can play with the level and panning controls
// closing the window, or freeing all of its channels, removes the MixingBoard
b = MixingBoard(\Fig1, nil, m);

// a SynthDef should include an outbus (or out) argument
// so that the MixerChannel can tell it where to write its output
(
SynthDef(\fig1, { |out, lowfreq = 220, hifreq = 1200, decay = 0.05|
	var	trig = Impulse.kr(8);
		// Do not hard-code the outbus here!
		// Out.ar(0, ...) is not OK. Out.ar(outbus, ...) is good.
	Out.ar(out, SinOsc.ar(TExpRand.kr(lowfreq, hifreq, trig)) * Decay2.kr(trig, 0.01, decay));
}).add;
)

a = m.play(\fig1, [lowfreq: 100, hifreq: 2000, decay: 0.1]);

// automate panning - this is done with a control-rate synth
m.automate(\pan, { SinOsc.kr(LFNoise1.kr(0.5).exprange(0.4, 5.0)) });

// the GUI can show the automation
m.watch(\pan);

// add reverb using post-fader send
// auto-play the reverb synth in the completion function
(
r = MixerChannel(\rvb, s, 2, 2, level:1, completionFunc: { |chan|
	"creating reverb synth".postln;
	chan.playfx({ |outbus|
		var	sig = In.ar(outbus, 2);  // read from channel's bus
		FreeVerb2.ar(sig[0], sig[1], 1, 0.6, 0.5)
	});
});

b.add(r);  // add to MixingBoard
)

// feed the signal into the reverb
m.newPostSend(r, 0.6);

// prints out current state of all mixers in the board
// the board is accessible also by MixingBoard.at(0)
b.postSettings;

// fade to black
m.levelTo(0, 15);

// and release
// note that on m.free, the synth 'a' is removed also
// freeing the last MixerChannel in the MixingBoard closes the window
m.free;
r.free;



////////////////////////////////////////
//////// Figure 20.3.
//////// Basic Voicer creation; triggering and gating notes.

// Voicer(voices, things, args, bus, target, addAction)
// target may be a Group, Server, or MixerChannel
v = Voicer(10, \default);

v.trigger(440, 1);  // v.trigger(freq, gate, args, latency)
v.release(440);

v.gate(440, 2.0, 1, [pan: -0.5]);  // v.gate(freq, dur, gate, args, latency)

r = fork {
	loop {
			// play over a C major scale
		[60, 62, 64, 65, 67, 69, 71, 72].do({ |midi|
			v.gate(midi.midicps, 0.1, 1, [amp: rrand(0.05, 0.15)],
				lat: 0.1);
			0.125.wait;
		});
	}
};

r.stop;

v.free;



////////////////////////////////////////
//////// Figure 20.4.
//////// Global controls and GUI.

// let's put a pan global control on the voicer

v = Voicer(10, \default);
v.mapGlobal(\pan, nil, 0, \bipolar);	// v.mapGlobal(name, bus, value, spec, allowGUI)

v.gui;

// Pbind works with voicer, using 'voicerNote' event type
(
p = Pbind(
	\type, \voicerNote,
	\voicer, v,
	\degree, Pn(Pseries(0, 1, 8), inf),
	\amp, Pwhite(0.05, 0.15, inf),
	\dur, 0.125,
	\legato, 0.8
).play;
)

// Try moving the slider in the Voicer window

p.stop;

// Note, freeing the Voicer does not close the window
// because you might want to reuse the GUI
// But the Voicer area becomes "inactive"
v.free;



////////////////////////////////////////
//////// Figure 20.5.
//////// VoicerMIDISocket.

v = Voicer(10, \default);

// 0 = device 0, channel 0
// for a different device, use [device index, channel]
// after this, you can play on your keyboard
k = VoicerMIDISocket(0, v);

// modwheel to control pan
k.addControl(1, \pan, 0, \bipolar);
v.gui;	// pan is identified by '1' in the gui - MIDI controller ID

// a homegrown variety of just intonation
// release all notes before executing this!
k.midiToFreq = TuningRatios(12, tunings: [1, 135/128, 9/8, 6/5, 5/4, 4/3, 45/32, 3/2, 14/9, 27/16, 16/9, 15/8]);

// If some notes got stuck by changing the tuning, do this:
v.panic;

// per-note argument generation using a Pbind
// here used to map velocity data onto the amp input
k.noteOnArgsPat = Pbind(\amp, Pkey(\velocity).linlin(0, 127, 0.01, 0.2));

// if you have a footswitch (controller number 64), this handles it properly
// make sure to use the same device:
// if you used [1, 0] for the channel ID before, use [1, 0] here
l = VoicerSusPedal(0, 64, v);

// by default, freeing the voicer automatically frees the socket and all attached MIDI controllers
v.free;



////////////////////////////////////////
//////// Figure 20.6.
//////// Proto for prototypes.

// standard "Hello World" greeter classes

(
~greeter = Proto({
	~sayhi = { |name|
		"%, %.\n".postf(~greeting.value, name ? ~name);
	};
	~name = "Monique";		// default name
	~greeting = "Hello";		// default greeting
});

// make a subclass with .clone
~frenchGreeter = ~greeter.clone({
	~greeting = "Bonjour";
});

~timeAwareFrenchGreeter = ~frenchGreeter.clone({
	~greeting = {
		var	hour;
		if(thisProcess.platform.name != \windows) {
			hour = Date.getDate.hour;
			if(hour < 18) { ~dayGreeting } { ~eveningGreeting };
		} { ~dayGreeting };
	};
	~dayGreeting = "Bonjour";
	~eveningGreeting = "Bon soir";
});
)

// Use the objects:
~greeter.sayhi;
~greeter.sayhi("Bob");
~frenchGreeter.sayhi("Isabelle");
	// note: Date does not work in Windows
	// this code ignores time check in Windows
~timeAwareFrenchGreeter.sayhi("Eric");



////////////////////////////////////////
//////// Figure 20.7.
//////// Greeter as a PR prototype, and usage through BP instances.

(
	// define PR prototype - Proto(...) => PR(\name)
Proto({
	~sayhi = { |name|
			// ~greeting.value is an internal pseudomethod call
		"%, %.\n".postf(~greeting.value, name ? ~name);
	};
	~name = "Monique";		// default name
	~greeting = "Hello";		// default greeting
}) => PR(\greeter);
)

// make working instance as BP
PR(\greeter) => BP(\greeter);
BP(\greeter).sayhi("Jacqueline");
BP(\greeter).name = "Zsa Zsa";
BP(\greeter).sayhi;
	// changing name in BP does not affect PR
PR(\greeter).name;

// override default at chuck time
// now this greeter belongs just to Bob
PR(\greeter).chuck(BP(\greetBob), parms: (name: "Bob"));
BP(\greetBob).name;	// ~name variable is overridden
BP(\greetBob).sayhi;

// remove both instances from the repository
BP([\greeter, \greetBob]).free;



////////////////////////////////////////
//////// Figure 20.9.
//////// Interactive session to build a soundfile slicer.

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
	~quant = 0;
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
		// default soundfile
	~path = Platform.case(
		\linux, { Platform.systemAppSupportDir +/+ "sounds/a11wlk01.wav" },
		{ "sounds/a11wlk01.wav" }
	);

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

// change process's stream references while it is playing
BP(\columbia).tfactor = 0.25;

BP(\columbia).tfactor = sin(Pseries(0, 0.1, inf)) * 0.45 + 0.55;

BP(\columbia).delta = Pstutter(Pwhite(4, 12, inf), Pxrand((1..6) * 0.05, inf));

BP(\columbia).startSec = Pclutch(Pwhite(0.0, BPStream(\maxStart), inf), Pdiff(Pkey(\delta)).abs > 0);

// also valid to chuck patterns into a BP
// ".pan" is an adverb indicating which variable to replace
sin(Ptime(inf)) =>.pan BP(\columbia);

BP(\columbia).stop;
BP(\columbia).free;	// MixerChannel and Buffer go away also



////////////////////////////////////////
//////// Figure 20.10.
//////// Factories for two variants of the buffer slicer process.

// In the definition file:
(
(make: { |name|
	PR(\bufSlicer) => BP(name);
}, type: \bp) => Fact(\basic);

(make: { |name|
	PR(\bufSlicer).chuck(BP(name), parms: (
		path: PR(\bufSlicer)[\path].dirname +/+ "a11wlk01-44_1.aiff",
		tfactor: sin(Pseries(0, 0.1, inf)) * 0.45 + 0.55,
		delta: Pstutter(Pwhite(4, 12, inf), Pxrand((1..6) * 0.05, inf)),
		startSec: Pclutch(Pwhite(0.0, Pfunc({ ~buf.duration }) - Pkey(\time)), Pdiff(Pkey(\delta)).abs > 0),
		pan: sin(Ptime(inf)),
		quant: 1	// quantize to next beat
	))
}, type: \bp) => Fact(\fancy);
)

// In the performance script:
Fact(\basic) => BP(\basic);

BP(\basic).play;

Fact(\fancy) => BP(\fancy);

BP(\fancy).play;

BP([\basic, \fancy]).stop;

BP([\basic, \fancy]).free;
