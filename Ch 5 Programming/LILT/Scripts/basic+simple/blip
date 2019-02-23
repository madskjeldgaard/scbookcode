

~controlSpecs = [
[ 'out', 0, 4095, 'linear', 1, 0 ],
[ 'freq', 20, 20000, 'exp', 0, 440 ],
[ 'numharm', 0, 400, 'linear', 0, 200 ],
[ 'mul', 0, 1, 'linear', 0, 1 ],
[ 'add', 0, 1, 'linear', 0, 0 ]
];

~start = { Synth("blip", ['out', ~out, 'freq', ~freq, 'numharm', ~numharm, 'mul', ~mul, 'add', ~add], ~target, #[\h, \t, \addBefore, \addAfter, \addReplace][~addAction]) };
