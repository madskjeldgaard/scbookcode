// (SC3) Script for session 'MIDI Test'
// Generated on Tue Feb 13 02:11:01 2007

~sessionName = "MIDI Test";
~server = Server.named['localhost'] ? { Server.default };
~serverAddressDetails = [ "127.0.0.1", 57110 ];	// Servers address saved for reference

//List of paths of samples to be loaded for this session:
~samplePaths = [
	"./sounds/a11wlk01.wav",
	"./sounds/a11wlk01-44_1.aiff",
	"./sounds/break",
	"./sounds/break2",
	"./sounds/SinedPink.aiff",
];

// Specs for creating scripts: names of Groups, Folders, Files, Scripts
~scriptGroups = [
	["Scripts",
		[ 'About Scripts', '05 group+synths', '05 group+synths', 0, 541, true ],
		[ 'About Scripts', '05 group+synths', '05 group+synths[2]', 50, 503, true ],
		[ 'About Scripts', '04 synth+controls', '04 synth+controls', 650, 495, true ],
		[ 'kr (control rate)', 'k_saw', 'k_saw', 350, 472, true ],
		[ 'kr (control rate)', 'k_lfdnoise1', 'k_lfdnoise1', 352, 625, true ],
	],
];

// Specs for creating links between scripts:
~linkSpecs = [
	[ 'Scripts', 'k_saw', 'k_out', 'Scripts', '04 synth+controls', 'freq' ],
	[ 'Scripts', 'k_lfdnoise1', 'k_out', 'Scripts', 'k_saw', 'mul' ],
];

// Specs to create MIDIResponders for each Script:
~midiBindings = [
['Scripts', '05 group+synths',
	['script', NoteOnOffResponder(nil, nil, 0, 16, nil, false)],
	['freq', CCResponder(nil, nil, 0, 32, nil, false)],
	['amp', CCResponder(nil, nil, 0, 33, nil, false)],
	['pulse', CCResponder(nil, nil, 0, 34, nil, false)],
	['attack', CCResponder(nil, nil, 0, 35, nil, false)],
	['decay', CCResponder(nil, nil, 0, 36, nil, false)],
	['vibfreq', CCResponder(nil, nil, 0, 37, nil, false)],
	['vibamp', CCResponder(nil, nil, 0, 38, nil, false)],
],
['Scripts', '05 group+synths[2]',
	['script', NoteOnOffResponder(nil, nil, 0, 17, nil, false)],
	['freq', CCResponder(nil, nil, 0, 40, nil, false)],
	['amp', CCResponder(nil, nil, 0, 41, nil, false)],
	['pulse', CCResponder(nil, nil, 0, 42, nil, false)],
	['attack', CCResponder(nil, nil, 0, 43, nil, false)],
	['decay', CCResponder(nil, nil, 0, 44, nil, false)],
	['vibfreq', CCResponder(nil, nil, 0, 45, nil, false)],
	['vibamp', CCResponder(nil, nil, 0, 46, nil, false)],
],
];

// Specs for the parameter snapshots of the scripts:
~snapshots = [
['Scripts', '04 synth+controls',	[ "0", [ 0, 440, 0, 0.1 ] ],

],
['Scripts', 'k_saw',	[ "0", [ 1, 1.5873015873016, 0, 634.92063492063, 1190.4761904762 ] ],

],
['Scripts', 'k_lfdnoise1',	[ "0", [ 2, 1, 1190.4761904762, 873.01587301587 ] ],

],
];

// -------------- SCRIPT-SPECIFIC DATA: --------------
~scriptData = [
];
// -------------- END SCRIPT-SPECIFIC DATA --------------

// the common part of the script for starting and stopping is included as template:
~script.include('Includes', 'SessionLoad');
