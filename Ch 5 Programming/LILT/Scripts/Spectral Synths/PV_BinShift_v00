// This part added by hand for buffer allocation: 

~script.allocBuffer;

// Generated from SynthDesc on: Thu Dec  8 13:48:18 2005

~controlSpecs = [
[ 'in', 0, 127, 'linear', 1, 0 ],
[ 'out', 0, 4095, 'linear', 1, 0 ],
// [ 'bufnum', 0, 1023, 'linear', 1, 0 ], // done internally with BufArray
[ 'stretch', 0.0, 10, 'linear', 0, 1.5 ],
[ 'shift', -128, 128, 'linear', 0, 100 ]
];

~start = {
      Synth("PV_BinShift", 
         ['in', ~in, 'out', ~out, 'bufnum', ~buffer.bufnum, 'stretch', ~stretch, 'shift', ~shift],
	 ~target, #[\h, \t, \addBefore, \addAfter, \addReplace][~addAction])
};
