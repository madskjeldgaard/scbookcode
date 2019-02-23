// This part added by hand for buffer allocation: 

~script.allocBuffer;

// Based on code generated on Fri Oct 28 23:19:56 2005

~controlSpecs = [
[ 'in', 0, 127, 'linear', 1, 0 ],
[ 'out', 0, 4095, 'linear', 1, 0 ],
// [ 'bufnum', 0, 1023, 'linear', 1, 0 ],
[ 'trigger', 0, 1, 'linear', 1, 0 ]
];

~start = {
	Synth("PV_MagFreeze", ['in', ~in, 'out', ~out, 'bufnum', 
		~buffer.bufnum, 'trigger', ~trigger],
	~target, #[\h, \t, \addBefore, \addAfter, \addReplace][~addAction]
	);
};
