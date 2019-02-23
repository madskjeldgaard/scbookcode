// This part added by hand for buffer allocation: 

~script.allocBuffer;

// Generated from SynthDesc on: Fri Dec  9 11:44:22 2005

~controlSpecs = [
[ 'in', 0, 127, 'linear', 1, 0 ],
[ 'out', 0, 4095, 'linear', 1, 0 ],
//[ 'bufnum', 0, 1023, 'linear', 1, 0 ],
[ 'threshold', 0, 50, 'linear', 0, 25 ]
];

~start = {

 Synth("PV_LocalMax", 
       ['in', ~in, 'out', ~out, 'bufnum', ~buffer.bufnum, 'threshold', ~threshold],
	~target, #[\h, \t, \addBefore, \addAfter, \addReplace][~addAction])
 };
