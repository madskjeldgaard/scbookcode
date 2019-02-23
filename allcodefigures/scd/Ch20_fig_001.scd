Figure 20.1
// create the mixer: one channel in, two channels out
m = MixerChannel(\fig1, s, 1, 2);

// bring up a mixing board -- you can play with the level and panning controls
// closing the window, or freeing all of its channels, removes the MixingBoard
b = MixingBoard(\Fig1, nil, m);

// a SynthDef should include an outbus argument
// so that the MixerChannel can tell it where to write its output
(
SynthDef(\fig1, { |outbus, lowfreq = 220, hifreq = 1200, decay = 0.05|
	var	trig = Impulse.kr(8);
		// Do not hard-code the outbus here!
		// Out.ar(0, ...) is not OK. Out.ar(outbus, ...) is good.
	Out.ar(outbus, SinOsc.ar(TExpRand.kr(lowfreq, hifreq, trig)) * Decay2.kr(trig, 0.01, decay));
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
// the board is accessible also through the MixingBoard.boards array
b.postSettings;

// fade to black
m.levelTo(0, 15);

// and release
// note that on m.free, the synth 'a' is removed also
// freeing the last MixerChannel in the MixingBoard closes the window
m.free;
r.free;


