Figure 17.17 

(
a=Pbind(
\degree, Pfunc({
[
[0, 6, 12].choose, 12.rand
].choose;
}),
\dur, 0.5,
\octave, 4,
\amp, 0.4,
\stepsPerOctave, 12,
\instrument, \tone2
);
b=Pbind(
\degree, Pfunc({
[
[0, 7, 14].choose, 14.rand
].choose;
}),
\dur, 0.25,
\octave, 5,
\amp, 0.3,
\stepsPerOctave, 14,
\instrument, \tone2
);
Ppar([a, b]).play;
)
//////////////////////////////////////////////////////

