~controlSpecs = [
[ 'out', 0, 4095, 'linear', 1, 0 ],
[ 'bufnum', 0, 1023, 'linear', 1, 0 ],
[ 'rate', 0.125, 8, 'exp', 0, 1 ],
[ 'amp', 0, 1, 'amp', 0, 1 ],
[ 'stop', 0, 1, 'linear', 0, 0 ],
[ 'trigAt', 0, 1, 'linear', 0, 1 ],
[ 'trigID', 0, 1, 'linear', 0, 0 ]
];

~start = { Synth("playbuf", ['out', ~out, 'bufnum', ~bufnum, 'rate', ~rate, 
	'amp', ~amp, 'stop', ~stop, 'trigAt', ~trigAt, 'trigID', ~trigID]) 
};
