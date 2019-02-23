Figure 17.16 

(
a=Pbind(
\degree, Pwhite(0, 12),
\dur, 0.5,
\octave, 5,
\amp, 0.4,
\stepsPerOctave, 12,
\instrument, \tone2
);
b=Pbind(
\degree, Pwhite(0, 14),
\dur, 0.25,
\octave, 4,
\amp, 0.4,
\stepsPerOctave, 14,
\instrument, \tone2
);
Ppar([a, b]).play;
)
//////////////////////////////////////////////////////

