/* IZ 2005-08-11. Example of customizing the actions of gui to control multiple synths in one script.

Here, the frequencies of 4 synths are scaled by 1, 1.1, 1.2, 1.3 respectively. 

To make the control gui for freq update the frequencies of the 4 synths, a custom action is provided in the controlSpecs:

[ 'freq', 20, 20000, 'exp', 0, 440, { |freq| 
		~freqAction.(freq);
		~freq = freq;
} ]

This custom action uses another function stored in ~freqAction. The ~freqAction is only set while the synths are running, so that no errors will occur if trying to set the frequency from the gui while the script is not playing. Note that using the ~freqAction is needed to have access to the d1, d2, d3, d4 variable which are local to the start function that creates ~freqAction. Storing d1, d2, d3, d4 in envir would be more computationally expensive and require more code.
*/

~controlSpecs = [
[ 'out', 0, 4095, 'linear', 1, 0 ],
[ 'freq', 20, 20000, 'exp', 0, 440, { |freq|
		~freqAction.(freq);
		~freq = freq;
} ],
[ 'amp', 0, 1, 'amp', 0, 0.1 ],
[ 'pulse', 0.1, 30, 'exp', 0, 0.5 ],
[ 'attack', 0.001, 5, 'exp', 0, 0.3 ],
[ 'decay', 0.001, 5, 'exp', 0, 1 ],
[ 'vibfreq', 1e-05, 30, 'exp', 0, 8 ],
[ 'vibamp', 0, 1000, 'linear', 0, 50 ],
];

~start = {
	var group, d1, d2, d3, d4;
	group = Group(~target, #[\h, \t, \addBefore, \addAfter, \addReplace][~addAction]);
	d1 = Synth("decaysin", ['out', ~out, 'freq', ~freq, 'amp', ~amp, 'pulse', ~pulse,
		'attack', ~attack, 'decay', ~decay, 'vibfreq', ~vibfreq, 'vibamp', ~vibamp
	], group, \addToHead);
	d2 = Synth("decaysin", ['out', ~out, 'freq', ~freq * 1.1, 'amp', ~amp, 'pulse', ~pulse,
		'attack', ~attack, 'decay', ~decay, 'vibfreq', ~vibfreq, 'vibamp', ~vibamp
	], group, \addToHead);
	d3 = Synth("decaysin", ['out', ~out, 'freq', ~freq * 1.2, 'amp', ~amp, 'pulse', ~pulse,
		'attack', ~attack, 'decay', ~decay, 'vibfreq', ~vibfreq, 'vibamp', ~vibamp
	], group, \addToHead);
	d4 = Synth("decaysin", ['out', ~out, 'freq', ~freq * 1.3, 'amp', ~amp, 'pulse', ~pulse,
		'attack', ~attack, 'decay', ~decay, 'vibfreq', ~vibfreq, 'vibamp', ~vibamp
	], group, \addToHead);
	~freqAction = { | freq |
		[d1, d2, d3, d3].do { |s, i| s.set(\freq, (1 + (i / 10) * freq )) };
	};
	group;
};

~stop = {
	~process.free;
	~freqAction = nil;
}
