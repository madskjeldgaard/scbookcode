Ins and Outs - MIDI

// initialize the MIDI client:
MIDIClient.init;

// create a MIDI out connection:
m = MIDIOut(0, MIDIClient.destinations.at(0).uid);

// On linux, you may just want to use:
m = MIDIOut(0);
// and connect SC's first port to your device's port with for example QJackCtl, or use:
m.connect(1);
// to connect to the second port

// a task which will play a little melody:
(
t = Task( {
	[60, 64, 61, 60, 65, 61].do{ |it|
		m.noteOn(16, it, 60);
		1.0.wait;
		m.noteOff(16, it, 60);
	};
	[64, 65, 67].dup(4).flatten.do{ |it|
		m.noteOn(16, it, 120);
		0.25.wait;
		m.noteOff(16, it, 120);
	};
	[61, 65, 60, 61, 64, 60].do{ |it|
		m.noteOn(16, it, 60);
		1.0.wait;
		m.noteOff(16, it, 60);
	};
	m.noteOn(16, 60, 40); m.noteOn(16, 52, 40);
	2.0.wait;
	m.noteOff(16, 60, 40); m.noteOff(16, 52, 40);
	m.noteOn(16, 57, 40); m.noteOn(16, 52, 40);
	2.0.wait;
	m.noteOff(16, 57, 40); m.noteOff(16, 52, 40);
	m.noteOn(16, 53, 40);
	2.0.wait;
	m.noteOff(16, 53, 40);
	m.noteOn(16, 52, 40);
	4.0.wait;
	m.noteOff(16, 52, 40);
});
);

// play the task:
t.play;

// stop it:
t.stop;

// free any hung notes if necessary:
m.allNotesOff(16);



// using a pattern to send a MIDI event:
a = Pbind(\degree, Prand([1, 2, 3, [0, 5]], inf), \bend, Pwhite(0, 76, inf));
// chain a midi event into the pattern and play it (see Pchain)
(a <> (type: \midi, midiout: m)).play;

// stop the pattern with Cmd-. , Alt-., C-c C-s, F12 or ESC

// free any hung notes if necessary:
m.allNotesOff(16);
