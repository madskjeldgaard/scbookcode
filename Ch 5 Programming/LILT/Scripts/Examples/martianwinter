// Generated from SynthDesc on: Sun Jul 31 14:45:04 2005 (SC3)

~controlSpecs = [
[ 'out', 0, 4095, 'linear', 1, 0 ],
[ 'freq', 20, 20000, 'exp', 0, 500 ],
[ 'amp', 0, 1, 'amp', 0, 0.1 ],
[ 'attack', 0.001, 5, 'exp', 0, 2 ],
[ 'release', 0, 16, 'linear', 0, 3 ],
[ 'vibFreq', 0, 6, 'linear', 0, 3 ],
[ 'vibAmp', 0, 2, 'linear', 0, 1 ],
[ 'gate', 0, 1, 'linear', 1, 1 ]
];

~start = { Synth("martianwinter", ['out', ~out, 'freq', ~freq, 'amp', ~amp, 'attack', ~attack, 'release', ~release, 'vibFreq', ~vibFreq, 'vibAmp', ~vibAmp, 'gate', ~gate], ~target,#[\h, \t, \addBefore, \addAfter, \addReplace][~addAction]) };

~stopMessage = [\set, \gate, 0];

