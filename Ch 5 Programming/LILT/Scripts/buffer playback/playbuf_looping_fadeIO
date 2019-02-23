// Generated from SynthDesc on: Fri Mar  3 15:49:19 2006

~controlSpecs = [
[ 'out', 0, 4095, 'linear', 1, 0 ],
[ 'bufnum', 0, 1023, 'linear', 1, 0 ],
[ 'rate', 0.125, 8, 'exp', 0, 1 ],
[ 'trigger', 0, 1, 'linear', 1, 1 ],
[ 'startPos', 0, 1, 'linear', 0, 0 ],
[ 'loop', 0, 1, 'linear', 1, 1 ],
[ 'amp', 0, 1, 'amp', 0, 1 ],
[ 'ampLag', 0, 3, 'linear', 0, 1.5 ]
];

~start = { Synth("playbuf_looping_fadeIO", ['out', ~out, 'bufnum', ~bufnum, 'rate', ~rate, 'trigger', ~trigger, 'startPos', ~startPos, 'loop', ~loop, 'amp', ~amp, 'ampLag', ~ampLag],
	~target, #[\h, \t, \addBefore, \addAfter, \addReplace][~addAction]) };
