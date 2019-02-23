/* IZ 050722 The specs were edited for generally useable ranges 
from the Formlet help file, last example */

~controlSpecs = [
[ 'in', 0, 127, 'linear', 1, 0 ],
[ 'out', 0, 4095, 'linear', 1, 0 ],
[ 'freq', 300, 3000, 'exp', 0, 1000 ],
[ 'attacktime', 0, 0.2, 'linear', 0, 0.005 ],
[ 'decaytime', 0, 0.5, 'linear', 0, 0.05 ],
[ 'mul', 0, 1, 'linear', 0, 1 ],
[ 'add', 0, 1, 'linear', 0, 0 ]
];

~start = { Synth("formlet", ['out', ~out, 'in', ~in, 'freq', ~freq, 'attacktime', ~attacktime, 'decaytime', ~decaytime, 'mul', ~mul, 'add', ~add], ~target, #[\h, \t, \addBefore, \addAfter, \addReplace][~addAction]) };
