// (SC3) Script for session 'Helmholtz'
// Generated on Sat May 12 20:57:35 2007

~sessionName = "Helmholtz";
~server = Server.named['localhost'] ? { Server.default };
~serverAddressDetails = [ "127.0.0.1", 57110 ];	// Servers address saved for reference

//List of paths of samples to be loaded for this session:
~samplePaths = [
	"/Users/iani/scwork/sounds/converted/swallows10b.wav",
	"./sounds/a11wlk01.wav",
	"./sounds/a11wlk01-44_1.aiff",
	"./sounds/break",
	"/Users/iani/scwork/sounds/converted/swallows11b.wav",
	"./sounds/break2",
	"./sounds/SinedPink.aiff",
	"/Users/iani/scwork/sounds/converted/swallows12b.wav",
];

// Specs for creating scripts: names of Groups, Folders, Files, Scripts
~scriptGroups = [
	["Parallel",
		[ 'buffer playback', 'playbuf (looping)', 'playbuf (looping)', -1, 262, true ],
		[ 'buffer playback', 'playbuf (looping)', 'playbuf (looping)[2]', 300, 263, true ],
		[ 'buffer playback', 'playbuf (looping)', 'playbuf (looping)[3]', 600, 262, true ],
		[ 'Spectral Synths', 'PV_MagAbove', 'PV_MagAbove', -1, 150, true ],
		[ 'Spectral Synths', 'PV_BinShift', 'PV_BinShift', -1, 17, true ],
		[ 'Spectral Synths', 'PV_LocalMax', 'PV_LocalMax', 599, 39, true ],
		[ 'Spectral Synths', 'PV_MagFreeze', 'PV_MagFreeze', 300, 151, true ],
		[ 'Spectral Synths', 'PV_MagSmear', 'PV_MagSmear', 601, 149, true ],
		[ 'Spectral Synths', 'PV_RectComb', 'PV_RectComb', 299, -3, true ],
		[ 'Spectral Synths', 'PV_BinScramble', 'PV_BinScramble', 900, 108, true ],
		[ 'buffer playback', 'playbuf (looping)', 'playbuf (looping)[4]', 900, 261, true ],
	],
	["Serial",
		[ 'buffer playback', 'playbuf (looping)', 'playbuf (looping)', 0, 262, false ],
		[ 'Spectral Synths', 'PV_MagAbove', 'PV_MagAbove', 300, 346, false ],
		[ 'Spectral Synths', 'PV_LocalMax', 'PV_LocalMax', 600, 346, false ],
		[ 'Spectral Synths', 'PV_MagFreeze', 'PV_MagFreeze', 900, 346, false ],
		[ 'Spectral Synths', 'PV_MagSmear', 'PV_MagSmear', 299, 234, false ],
		[ 'Spectral Synths', 'PV_RectComb', 'PV_RectComb', 600, 192, false ],
		[ 'mixing+multichannel', 'PAN8', 'PAN8', 901, 170, false ],
	],
];

// Specs for creating links between scripts:
~linkSpecs = [
	[ 'Parallel', 'playbuf (looping)', 'out', 'Parallel', 'PV_MagAbove', 'in' ],
	[ 'Parallel', 'playbuf (looping)', 'out', 'Parallel', 'PV_BinShift', 'in' ],
	[ 'Parallel', 'playbuf (looping)[2]', 'out', 'Parallel', 'PV_MagFreeze', 'in' ],
	[ 'Parallel', 'playbuf (looping)[2]', 'out', 'Parallel', 'PV_RectComb', 'in' ],
	[ 'Parallel', 'playbuf (looping)[3]', 'out', 'Parallel', 'PV_MagSmear', 'in' ],
	[ 'Parallel', 'playbuf (looping)[3]', 'out', 'Parallel', 'PV_LocalMax', 'in' ],
	[ 'Parallel', 'playbuf (looping)[4]', 'out', 'Parallel', 'PV_BinScramble', 'in' ],
	[ 'Serial', 'playbuf (looping)', 'out', 'Serial', 'PV_MagAbove', 'in' ],
	[ 'Serial', 'PV_MagAbove', 'out', 'Serial', 'PV_MagSmear', 'in' ],
	[ 'Serial', 'PV_LocalMax', 'out', 'Serial', 'PV_RectComb', 'in' ],
	[ 'Serial', 'PV_MagFreeze', 'out', 'Serial', 'PAN8', 'in' ],
	[ 'Serial', 'PV_MagSmear', 'out', 'Serial', 'PV_LocalMax', 'in' ],
	[ 'Serial', 'PV_RectComb', 'out', 'Serial', 'PV_MagFreeze', 'in' ],
];

// Specs to create MIDIResponders for each Script:
~midiBindings = [
['Parallel', 'playbuf (looping)',
	['script', CCResponder(nil, nil, 0, 65, nil, false)],
	['bufnum', CCResponder(nil, nil, 0, 81, nil, false)],
	['rate', CCResponder(nil, nil, 0, 83, nil, false)],
	['trigger', CCResponder(nil, nil, 0, 66, nil, false)],
	['startPos', CCResponder(nil, nil, 0, 83, nil, false)],
	['loop', CCResponder(nil, nil, 0, 67, nil, false)],
	['amp', CCResponder(nil, nil, 0, 84, nil, false)],
],
['Parallel', 'playbuf (looping)[2]',
	['script', CCResponder(nil, nil, 0, 70, nil, false)],
],
['Parallel', 'PV_MagAbove',
	['script', CCResponder(nil, nil, 0, 68, nil, false)],
	['threshold', CCResponder(nil, nil, 0, 85, nil, false)],
],
['Parallel', 'PV_BinShift',
	['script', CCResponder(nil, nil, 0, 70, nil, false)],
	['stretch', CCResponder(nil, nil, 0, 86, nil, false)],
	['shift', CCResponder(nil, nil, 0, 87, nil, false)],
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
