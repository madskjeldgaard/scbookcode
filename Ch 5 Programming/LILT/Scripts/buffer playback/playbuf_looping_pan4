// Generated from SynthDesc on: Sat Sep 24 18:21:49 2005

~controlSpecs = [
[ 'out', 0, 4095, 'linear', 1, 0 ],
[ 'bufnum', 0, 1023, 'linear', 1, 0 ],
[ 'rate', 0.125, 8, 'exp', 0, 1 ],
[ 'trigger', 0, 1, 'linear', 1, 1 ],
[ 'startPos', 0, 1, 'linear', 0, 0 ],
[ 'loop', 0, 1, 'linear', 1, 0 ],
[ 'amp', 0, 1, 'amp', 0, 1 ],
[ 'pos', -1, 1, 'linear', 0, 0 ],
[ 'level', 0, 2, 'linear', 0, 1 ],
[ 'width', 0, 1, 'linear', 0, 2 ],
[ 'orientation', 0, 1, 'linear', 0, 0.5 ]
];

~start = { Synth("playbuf_looping_pan4", ['out', ~out, 'bufnum', ~bufnum, 'rate', ~rate, 'trigger', ~trigger, 'startPos', ~startPos, 'loop', ~loop, 'amp', ~amp, 'pos', ~pos, 'level', ~level, 'width', ~width, 'orientation', ~orientation],
	~target, #[\h, \t, \addBefore, \addAfter, \addReplace][~addAction]) };
