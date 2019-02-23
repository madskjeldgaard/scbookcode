// (SC3:) Generated from SynthDesc on: Thu Jun  7 14:33:03 2007

~script.allocBuffer;

~controlSpecs = [
[ 'in', 0, 127, 'linear', 1.0, 0.0 ],
[ 'out', 0, 4095, 'linear', 1, 0.0 ],
// [ 'bufnum', 0, 1023, 'linear', 1, 0.0 ],
[ 'stretch', 0.0125, 4.0, 'linear', 0.0, 1.5 ],
[ 'shift', -128, 128, 'linear', 0.0, 100.0 ],
[ 'source_level', 0.0, 1.0, 'linear', 0.0, 0.0 ],
[ 'fx_level', 0.0, 2.0, 'linear', 0.0, 1.0 ]
];

~start = { Synth("PV_BinShift", ['in', ~in, 'out', ~out, 'bufnum', ~buffer.bufnum, 'stretch', ~stretch, 'shift', ~shift, 'source_level', ~source_level, 'fx_level', ~fx_level],
	~target, #[\h, \t, \addBefore, \addAfter, \addReplace][~addAction]) };
