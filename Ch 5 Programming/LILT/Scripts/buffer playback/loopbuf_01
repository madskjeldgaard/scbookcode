// (SC3:) Generated from SynthDesc on: Thu Jun  7 12:06:40 2007

~controlSpecs = [
[ 'out', 0.0, 4095.0, 'linear', 1.0, 0.0 ],
[ 'bufnum', 0.0, 1023.0, 'linear', 1.0, 0.0 ],
[ 'rate', 0.125, 8.0, 'exp', 0.0, 1.0 ],
[ 'glide', 0.0, 20.0, 'linear', 0.0, 0.0 ],
[ 'gate', 0.0, 1.0, 'linear', 1.0, 1.0 ],
[ 'start', 0.0, 1.0, 'linear', 0.0, 0.0 ],
[ 'end', 0.0, 1.0, 'linear', 0.0, 1.0 ],
[ 'amp', 0.0, 1.0, 'amp', 0.0, 0.5 ]
];

~start = { Synth("loopbuf_01", ['out', ~out, 'bufnum', ~bufnum, 'rate', ~rate, 'glide', ~glide, 'gate', ~gate, 'start', ~start, 'end', ~end, 'amp', ~amp],
	~target, #[\h, \t, \addBefore, \addAfter, \addReplace][~addAction]) };
