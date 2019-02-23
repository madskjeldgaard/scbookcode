Figure 20.5
v = Voicer(10, \default);

// 0 = device 0, channel 0
// for a different device, use [device index, channel]
k = VoicerMIDISocket(0, v);

// modwheel to control pan
k.addControl(1, \pan, 0, \bipolar);
v.gui;

// a homegrown variety of just intonation
// release all notes before executing this!
k.midiToFreq = TuningRatios(12, tunings: [1, 135/128, 9/8, 6/5, 5/4, 4/3, 45/32, 3/2, 14/9, 27/16, 16/9, 15/8]);

// per-note argument generation using a Pbind
// here used to map velocity data onto the amp input
k.noteOnArgsPat = Pbind(\amp, Pkey(\velocity).linlin(0, 127, 0.01, 0.2));

// if you have a footswitch (controller number 64), this handles it properly
l = VoicerSusPedal(0, 64, v);

// by default, freeing the voicer automatically frees the socket and all attached MIDI controllers
v.free;


