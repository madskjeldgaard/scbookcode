// Generated from SynthDesc on: Sun Jul 24 16:23:41 2005

~controlSpecs = [
[ 'out', 0, 4095, 'linear', 1, 0 ],
[ 'freq', 20, 20000, 'exp', 0, 440 ],
[ 'amp', 0, 1, 'amp', 0, 0.1 ],
[ 'pulse', 0.1, 30, 'exp', 0, 0.5 ],
[ 'attack', 0.001, 5, 'exp', 0, 0.3 ],
[ 'decay', 0.001, 5, 'exp', 0, 1 ],
[ 'vibfreq', 1e-05, 30, 'exp', 0, 0.2 ],
[ 'vibamp', 0, 1000, 'linear', 0, 110 ]
];

~start = { Synth("decaysin", ['out', ~out, 'freq', ~freq, 'amp', ~amp, 'pulse', ~pulse, 'attack', ~attack, 'decay', ~decay, 'vibfreq', ~vibfreq, 'vibamp', ~vibamp], ~target,#[\h, \t, \addBefore, \addAfter, \addReplace][~addAction]) };
