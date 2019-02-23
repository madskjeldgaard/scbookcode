// Generated from SynthDesc on: Sat May 13 19:31:27 2006

~controlSpecs = [
[ 'bufnum', 0, 1023, 'linear', 1, 0 ],
[ 'rate', 0.125, 8, 'exp', 0, 1 ],
[ 'trigger', 0, 1, 'linear', 1, 1 ],
[ 'startPos', 0, 1, 'linear', 0, 0 ],
[ 'loop', 0, 1, 'linear', 1, 1 ],
[ 'out', 0, 4095, 'linear', 1, 0 ],
[ 'amp1', 0, 2, 'linear', 0, 1 ],
[ 'amp2', 0, 2, 'linear', 0, 1 ],
[ 'ampLag', 0, 3, 'linear', 0, 1.5 ]
];

~start = { Synth("buf_loop_2Amp", ['bufnum', ~bufnum, 'rate', ~rate, 'trigger', ~trigger, 'startPos', ~startPos, 'loop', ~loop, 'out', ~out, 'amp1', ~amp1, 'amp2', ~amp2, 'ampLag', ~ampLag],
	~target, #[\h, \t, \addBefore, \addAfter, \addReplace][~addAction]) };
