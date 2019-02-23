// (SC3) Script for session 'MIDI Debug 3'
// Generated on Tue Apr 17 12:53:51 2007

~sessionName = "MIDI Debug 3";
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
		[ 'About Scripts', '04 synth+controls', '04 synth+controls', 0, 325, true ],
		[ 'About Scripts', '04 synth+controls', '04 synth+controls[2]', 300, 325, true ],
		[ 'About Scripts', '04 synth+controls', '04 synth+controls[3]', 600, 325, true ],
	],
];

// Specs for creating links between scripts:
~linkSpecs = [
];

// Specs to create MIDIResponders for each Script:
~midiBindings = [
['Scripts', '04 synth+controls',
	['script', CCResponder(nil, nil, 0, 65, nil, false)],
	['freq', CCResponder(nil, nil, 0, 81, nil, false)],
	['phase', CCResponder(nil, nil, 0, 82, nil, false)],
	['amp', CCResponder(nil, nil, 0, 83, nil, false)],
],
['Scripts', '04 synth+controls[2]',
	['script', CCResponder(nil, nil, 0, 66, nil, false)],
	['freq', CCResponder(nil, nil, 0, 89, nil, false)],
	['phase', CCResponder(nil, nil, 0, 90, nil, false)],
	['amp', CCResponder(nil, nil, 0, 91, nil, false)],
],
['Scripts', '04 synth+controls[3]',
	['script', CCResponder(nil, nil, 0, 67, nil, false)],
	['freq', CCResponder(nil, nil, 0, 97, nil, false)],
	['phase', CCResponder(nil, nil, 0, 98, nil, false)],
	['amp', CCResponder(nil, nil, 0, 99, nil, false)],
],
];

// Specs for the parameter snapshots of the scripts:
~snapshots = [
];

// -------------- SCRIPT-SPECIFIC DATA: --------------
~scriptData = [
];
// -------------- END SCRIPT-SPECIFIC DATA --------------

// the common part of the script for starting and stopping is included as template:
~script.include('Includes', 'SessionLoad');
