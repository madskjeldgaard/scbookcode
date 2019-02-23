// (SC3:) Generated from SynthDesc on: Sat Feb  3 17:18:50 2007
// For testing various input and output channels in the system.
// Needs further development: An extra GUI for connecting to different Scripts
// in the system and perhaps also other stuff.

~controlSpecs = [
[ 'in', 0, 127, 'linear', 1, 0 ],
[ 'out', 0, 4095, 'linear', 1, 0 ],
[ 'amp', 0, 1, 'amp', 0, 1 ]
];

~start = { Synth("input_monitor", ['in', ~in, 'out', ~out, 'amp', ~amp],
	~script.session.server.asTarget, \t) };
