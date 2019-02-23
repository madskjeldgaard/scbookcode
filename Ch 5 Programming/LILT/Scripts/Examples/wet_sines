// (SC3:) Generated from SynthDesc on: Wed Feb  7 04:10:42 2007

~controlSpecs = [
[ 'out', 0, 4095, 'linear', 1, 0 ],
[ 'noise_freq', 0, 8, 'linear', 0, 4 ],
[ 'noise_amp', 0, 48, 'linear', 0, 24 ],
[ 'saw_freq', 0, 14.46, 'linear', 0, 7.23 ],
[ 'saw_freq_d', 0, 1.54, 'linear', 0, 0.77 ],
[ 'saw_amp', 0, 6, 'linear', 0, 3 ],
[ 'saw_add', 0, 160, 'linear', 0, 80 ],
[ 'comb_time', 0, 1, 'linear', 0, 0.1 ]
];

~start = { Synth("wet_sines", ['out', ~out, 'noise_freq', ~noise_freq, 'noise_amp', ~noise_amp, 'saw_freq', ~saw_freq, 'saw_freq_d', ~saw_freq_d, 'saw_amp', ~saw_amp, 'saw_add', ~saw_add, 'comb_time', ~comb_time],
	~target, #[\h, \t, \addBefore, \addAfter, \addReplace][~addAction]) };
