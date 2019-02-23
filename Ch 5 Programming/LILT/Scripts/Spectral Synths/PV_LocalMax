// (SC3:) Generated from SynthDesc on: Thu Jun  7 14:33:13 2007

~script.allocBuffer;

~controlSpecs = [
[ 'in', 0.0, 127.0, 'linear', 1.0, 0.0 ],
[ 'out', 0.0, 4095.0, 'linear', 1.0, 0.0 ],
// [ 'bufnum', 0.0, 1023.0, 'linear', 1.0, 0.0 ],
// [ 'threshold', 0.0, 50.0, 'linear', 0.0, 25.0 ],
[ 'threshold', 0.0001, 50.0, 'exponential', 0.0, 25.0 ],
[ 'source_level', 0.0, 1.0, 'linear', 0.0, 0.0 ],
[ 'fx_level', 0.0, 2.0, 'linear', 0.0, 1.0 ]
];

~start = { Synth("PV_LocalMax", ['in', ~in, 'out', ~out, 'bufnum', ~buffer.bufnum, 'threshold', ~threshold, 'source_level', ~source_level, 'fx_level', ~fx_level],
	~target, #[\h, \t, \addBefore, \addAfter, \addReplace][~addAction]) };
