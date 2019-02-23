// (SC3) Script for session 'MIDI Test'
// Generated on Mon Feb 12 19:17:09 2007

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
		[ 'About Scripts', '05 group+synths', '05 group+synths[2]', 300, 541, true ],
		[ 'Spectral Synths', 'PV_BinScramble', 'PV_BinScramble', 600, 604, true ],
		[ 'Spectral Synths', 'PV_BinShift', 'PV_BinShift', 900, 625, true ],
		[ 'Spectral Synths', 'PV_LocalMax', 'PV_LocalMax', 595, 492, true ],
		[ 'Spectral Synths', 'PV_LocalMax_Amp', 'PV_LocalMax_Amp', 902, 471, false ],
		[ 'Spectral Synths', 'PV_MagAbove', 'PV_MagAbove', 901, 514, true ],
		[ 'Spectral Synths', 'PV_MagFreeze', 'PV_MagFreeze', 897, 403, true ],
		[ 'Spectral Synths', 'PV_MagSmear', 'PV_MagSmear', 597, 381, true ],
		[ 'Spectral Synths', 'PV_RectComb', 'PV_RectComb', 294, 385, true ],
	],
];

// Specs for creating links between scripts:
~linkSpecs = [
	[ 'Scripts', '05 group+synths[2]', 'out', 'Scripts', 'PV_BinScramble', 'in' ],
	[ 'Scripts', '05 group+synths[2]', 'out', 'Scripts', 'PV_BinShift', 'in' ],
	[ 'Scripts', '05 group+synths[2]', 'out', 'Scripts', 'PV_LocalMax', 'in' ],
	[ 'Scripts', '05 group+synths[2]', 'out', 'Scripts', 'PV_MagAbove', 'in' ],
	[ 'Scripts', '05 group+synths[2]', 'out', 'Scripts', 'PV_MagFreeze', 'in' ],
	[ 'Scripts', '05 group+synths[2]', 'out', 'Scripts', 'PV_MagSmear', 'in' ],
	[ 'Scripts', '05 group+synths[2]', 'out', 'Scripts', 'PV_RectComb', 'in' ],
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
['Scripts', '05 group+synths[2]',	[ "0", [ 0, 440, 0.1, 0.7, 0.3, 1, 1, 50 ] ],

],
];

// -------------- SCRIPT-SPECIFIC DATA: --------------
~scriptData = [
];
// -------------- END SCRIPT-SPECIFIC DATA --------------

// the common part of the script for starting and stopping is included as template:
~script.include('Includes', 'SessionLoad');
