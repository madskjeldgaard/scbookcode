// (SC3) Script for session 'Default Session'
// Generated on Wed Feb  7 17:15:12 2007

~sessionName = "Default Session";
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
	["Synth Control",
		[ 'About Scripts', '03 simple synth', '03 simple synth', -1, 432, true ],
		[ 'About Scripts', '04 synth+controls', '04 synth+controls', 4, 622, true ],
		[ 'About Scripts', '05 group+synths', '05 group+synths', 906, 538, true ],
		[ 'About Scripts', '06 synth+release env', '06 synth+release env', 609, 560, true ],
		[ 'examples', 'wet_sines', 'wet_sines', 306, 539, true ],
	],
	["Patterns + Routines",
		[ 'About Scripts', '07 routine', '07 routine', 2, 553, true ],
		[ 'About Scripts', '07b routine resume', '07b routine resume', 2, 481, true ],
		[ 'About Scripts', '08 pattern', '08 pattern', 198, 431, true ],
		[ 'About Scripts', '08b pattern pausing', '08b pattern pausing', 399, 428, true ],
		[ 'About Scripts', '09 pattern control', '09 pattern control', 605, 448, true ],
	],
	["Fx",
		[ 'Fx', '16randomallpasses', '16randomallpasses', 906, 407, true ],
	],
	["MIDI",
	],
	["Other",
		[ 'Utilities', 'Kill All Servers', 'Kill All Servers', 606, 401, true ],
		[ 'Utilities', 'Post Keyboard Codes', 'Post Keyboard Codes', 308, 487, true ],
		[ 'Utilities', 'Post Keyboard Codes 2', 'Post Keyboard Codes 2', 600, 709, false ],
		[ 'Utilities', 'Open Startup File', 'Open Startup File', 404, 486, true ],
	],
	["Tests",
	],
];

// Specs for creating links between scripts:
~linkSpecs = [
//	[ 'Synth Control', '05 group+synths', 'out', 'Synth Control', '16randomallpasses', 'in' ],
//	[ 'Synth Control', '05 group+synths', 'out', 'FX', '16randomallpasses', 'in' ],
];

// Specs for the parameter snapshots of the scripts:
~snapshots = [
['Synth Control', '05 group+synths',	[ "standard", [ 32, 440, 0.1, 0.7, 0.3, 1, 1, 50 ] ],
	[ "crazy picks", [ 32, 440, 0.1, 4.2830631458708, 0.023975878296373, 0.27328959566241, 6.4394212963517, 444.44444444444 ] ],
	[ "slow gliss", [ 32, 440, 0.1, 4.2830631458708, 0.023975878296373, 0.27328959566241, 0.003717801618874, 444.44444444444 ] ],

],
];

// -------------- SCRIPT-SPECIFIC DATA: --------------
~scriptData = [
];
// -------------- END SCRIPT-SPECIFIC DATA --------------

// the common part of the script for starting and stopping is included as template:
~script.include('Includes', 'SessionLoad');
