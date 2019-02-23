// Generated from SynthDesc on: Sat Jul 30 17:14:28 2005

~controlSpecs = [
[ 'out', 0, 4095, 'linear', 1, 0 ],
[ 'intensity', 0, 2, 'linear', 0, 1 ],
[ 'gravity', 0, 1, 'linear', 0, 0.1 ],
[ 'damping', 0, 1, 'linear', 0, 0.01 ],
[ 'friction', 0, 1, 'linear', 0, 0.01 ],
[ 'freq', 20, 20000, 'exp', 0, 1200 ],
[ 'ring', 0, 1, 'linear', 0, 0.1 ],
[ 'decayAmp', 0, 100, 'linear', 0, 50 ],
[ 'amp', 0, 1, 'amp', 0, 0.1 ],
[ 'attack', 0.001, 5, 'exp', 0, 0.002 ],
[ 'decay', 0.001, 5, 'exp', 0, 0.1 ],
[ 'pan', -1, 1, 'linear', 0, 0 ]
];

~start = { Synth("softbounce", ['out', ~out, 'intensity', ~intensity, 'gravity', ~gravity, 'damping', ~damping, 'friction', ~friction, 'freq', ~freq, 'ring', ~ring, 'decayAmp', ~decayAmp, 'amp', ~amp, 'attack', ~attack, 'decay', ~decay, 'pan', ~pan], ~target,#[\h, \t, \addBefore, \addAfter, \addReplace][~addAction]) };
