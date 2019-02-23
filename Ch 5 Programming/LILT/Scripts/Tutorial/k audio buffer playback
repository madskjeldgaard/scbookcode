~controlSpecs = [
[ 'out', 0, 4095, 'linear', 1, 0 ],
[ 'bufnum', 0, 1023, 'linear', 1, 0 ],
[ 'rate', 0.125, 8, 'exp', 0, 1 ],
[ 'trigger', 0, 1, 'linear', 1, 1 ],
[ 'startPos', 0, 1, 'linear', 0, 0 ],
[ 'loop', 0, 1, 'linear', 1, 1 ],
[ 'amp', 0, 1, 'amp', 0, 1 ]
];

~start = { Synth("playbuf_looping", ['out', ~out, 'bufnum', ~bufnum, 'rate', ~rate, 'trigger', ~trigger, 'startPos', ~startPos, 'loop', ~loop, 'amp', ~amp]) };
