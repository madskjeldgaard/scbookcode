/* (IZ 2005-10-29) { 
Play a selected buffer with single - shot playbuf SynthDef, triggering it multiple times at random interval and random rates, where the time interval between triggers and the rate variation range are controlled interactively by parameters
} */

~controlSpecs = [
[ 'out', 0, 4095, 'linear', 1, 0 ],
[ 'bufnum', 0, 1023, 'linear', 1, 0 ],
[ 'rateMin', 0.125, 8, 'exp', 0, 1 ],
[ 'rateRange', 0.0, 8, 'linear', 0, 1 ],
[ 'ampMin', 0, 1, 'amp', 0, 0.1 ],
[ 'ampRange', 0, 1, 'amp', 0, 0.5 ],
[ 'waitMin', 0.33, 5, 'linear', 0, 0.5],
[ 'waitRange', 0.0, 5, 'linear', 0, 1.5],
[ 'randOut', 0, 1, 'linear', 1, 0]
];

~script.routine {
 	loop {
	Synth("playbuf", ['out', if (~randOut > 0) { 2 rrand: 10 } { ~out },
		'bufnum', ~bufnum,
		'rate', ~rateMin rrand: (~rateMin + ~rateRange),
		'amp', ~ampMin rrand: (~ampMin + ~ampRange),
		'stop', 0, 'trigAt', 1, 'trigID', 0
	]);
	wait(~waitMin rrand: (~waitMin + ~waitRange));
	};
};

