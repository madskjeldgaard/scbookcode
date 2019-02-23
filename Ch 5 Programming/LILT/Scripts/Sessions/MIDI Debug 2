// (SC3) Script for session 'MIDI Debug 2'
// Generated on Tue Apr 17 12:10:59 2007

~sessionName = "MIDI Debug 2";
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
	["Scripts 1",
		[ 'Fx', '16randomallpasses', '16randomallpasses', 0, 325, true ],
		[ 'Fx', '16randomallpasses', '16randomallpasses[2]', 300, 325, true ],
		[ 'Fx', '16randomallpasses', '16randomallpasses[3]', 600, 325, true ],
	],
];

// Specs for creating links between scripts:
~linkSpecs = [
];

// Specs to create MIDIResponders for each Script:
~midiBindings = [
['Scripts 1', '16randomallpasses',
	['script', CCResponder(nil, nil, 0, 65, nil, false)],
	['sourcegain', CCResponder(nil, nil, 0, 81, nil, false)],
	['wet', CCResponder(nil, nil, 0, 82, nil, false)],
],
['Scripts 1', '16randomallpasses[2]',
	['script', CCResponder(nil, nil, 0, 66, nil, false)],
	['sourcegain', CCResponder(nil, nil, 0, 89, nil, false)],
	['wet', CCResponder(nil, nil, 0, 90, nil, false)],
],
['Scripts 1', '16randomallpasses[3]',
	['script', CCResponder(nil, nil, 0, 67, nil, false)],
	['sourcegain', CCResponder(nil, nil, 0, 97, nil, false)],
	['wet', CCResponder(nil, nil, 0, 98, nil, false)],
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
