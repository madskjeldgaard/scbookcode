Figure 17.22 

(
a=[ 1, 1.09375, 1.1875, 1.28125, 1.375, 1.46875, 1.5625, 1.65625];
b=a*440;
e=Pbind(
\freq, Pseq( b, inf),
\dur, 0.2,
\amp, 0.5,
\instrument, \tone2,
\sustain, 0.6
).play
)
// play in a different order
(
e.stream=Pbind(
\freq, Pn(Pshuf( b, 1)),
\dur, 0.2,
\amp, 0.5,
\instrument, \tone2,
\sustain, 0.6
).asStream
)
//////////////////////////////////////////////////////

