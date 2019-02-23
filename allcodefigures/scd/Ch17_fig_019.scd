Figure 17.19 

(
a=Pbind(
\degree, Pfunc({
// notes which clash with the other tuning have been removed
[0,4,8,12].choose
}),
\dur, 0.5,
\octave, 5,
\amp, 0.4,
\sustain, 0.85,
\stepsPerOctave, 12,
\instrument, \tone2
).play;
b=Pbind(
// notes which clash with the other tuning have been removed
\degree, Pfunc({
[0,7,14,21].choose
}),
\dur, 0.25,
\octave, 4,
\amp, 0.35,
\sustain, 0.85,
\stepsPerOctave, 21,
\instrument, \tone2
).play;
)

(
a.stream=Pbind(
// introducing more notes from that tuning after having changed the threshold
\degree, Pfunc({
[ 0, 1, 4, 7, 8, 9, 9, 12 ].choose
}),
\dur, 0.75,
\octave, 5,
\amp, 0.4,
\sustain, 0.85,
\stepsPerOctave, 12,
\instrument, \tone2
).asStream;
)
//////////////////////////////////////////////////////

