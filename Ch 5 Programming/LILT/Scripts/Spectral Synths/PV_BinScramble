// This part added by hand for buffer allocation: 

~script.allocBuffer;

// This part adapted from SynthDesc on: Fri Oct 28 21:38:11 2005

~controlSpecs = [
[ 'in', 0, 127, 'linear', 1, 0 ],
[ 'out', 0, 4095, 'linear', 1, 0 ],
// [ 'bufnum', 0, 1023, 'linear', 1, 0 ], // done internally with allocBuffer
[ 'wipe', 0, 10, 'linear', 0, 0.5 ],
[ 'width', 0, 10, 'linear', 0, 0.2 ],
[ 'trigger', 0, 1, 'linear', 1, 0 ]
];

~start = {
	Synth("PV_BinScramble", [\in, ~in, \out, ~out, \bufnum, ~buffer.bufnum, 
		\wipe, ~wipe, \width, ~width, \trigger, ~trigger],
		~target, #[\h, \t, \addBefore, \addAfter, \addReplace][~addAction]
	);
};

