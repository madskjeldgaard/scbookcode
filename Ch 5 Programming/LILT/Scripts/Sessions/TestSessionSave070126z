// (SC3) Script for session 'Test Session'
// Generated on Wed Feb  7 22:54:02 2007

~sessionName = "Test Session";
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
		[ 'basic+simple', 'smoothsine', 'smoothsine', 0, 492, true ],
		[ 'basic+simple', 'smoothsine', 'smoothsine[xxx2]', -1, 359, false ],
		[ 'examples', 'decaysin', 'decaysin', 302, 407, true ],
	],
	["Scripts 2",
		[ 'basic+simple', 'smoothsine', 'smoothsine', 0, 625, true ],
		[ 'basic+simple', 'smoothsine', 'smoothsine[xxxxxxx2]', 300, 625, true ],
		[ 'Fx', '16randomallpasses', '16randomallpasses', 600, 625, true ],
		[ 'About Scripts', '05 group+synths', '05 group+synths', 900, 541, true ],
	],
];

// Specs for creating links between scripts:
~linkSpecs = [
	[ 'Scripts 1', 'decaysin', 'out', 'Scripts 2', '16randomallpasses', 'in' ],
];

// Specs for the parameter snapshots of the scripts:
~snapshots = [
['Scripts 2', 'smoothsine',	[ "0", [ 0, 400, 0.1, 1 ] ],
	[ "1", [ 0, 10942.718665342, 0.1, 1 ] ],
	[ "2", [ 0, 1520.4858281685, 0.1, 1 ] ],

],
['Scripts 2', '16randomallpasses',	[ "0", [ 32, 0, 0.76984126984127, 0.5 ] ],
	[ "1", [ 32, 0, 0.36507936507937, 0.5 ] ],
	[ "2", [ 32, 0, 0.36507936507937, 0.88888888888889 ] ],

],
];

// -------------- SCRIPT-SPECIFIC DATA: --------------
~scriptData = [
];
// -------------- END SCRIPT-SPECIFIC DATA --------------

// the common part of the script for starting and stopping is included as template:
~script.include('Includes', 'SessionLoad');
