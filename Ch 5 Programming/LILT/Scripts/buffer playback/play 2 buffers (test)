/* IZ 051230 
test for using BufParameter explicitly in the specification of ~controlSpecs */


~controlSpecs = [
[ 'out', 0, 4095, 'linear', 1, 0 ],
[ 'bufnum', 0, 1023, 'linear', 1, 0, { | val |
	~bufnum = val;
	if (~process.notNil) {
		~synth1.set(\bufnum, val);
	};
}],
[ 'rate', 0.125, 8, 'exp', 0, 1 ],
[ 'amp', 0, 1, 'amp', 0, 0.5 ],
[ 'stop', 0, 1, 'linear', 0, 0 ],
[ 'trigAt', 0, 1, 'linear', 0, 1 ],
[ 'trigID', 0, 1, 'linear', 0, 0 ],
[ 'BufParameter', 'buffer2', 0, 1, 'linear', 0, 0, { | val |
	~buffer2 = val;
	if (~process.notNil) {
		~synth2.set(\bufnum, val);
	};
}]
];

~start = {
	var group;
	group = Group(~target, #[\h, \t, \addBefore, \addAfter, \addReplace][~addAction]);
	~synth1 = Synth("playbuf_looping", ['out', ~out, 'bufnum', ~bufnum, 'rate', ~rate, 
		'amp', ~amp, 'loop', 1], group); 
	~synth2 = Synth("playbuf_looping", ['out', ~out, 'bufnum', ~buffer2, 'rate', ~rate, 
		'amp', ~amp, 'loop', 1], group);
	group;
};
