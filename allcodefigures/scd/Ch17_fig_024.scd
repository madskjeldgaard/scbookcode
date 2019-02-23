Figure 17.24 

a=(1..16)*100
(
e=Pbind(
\freq, Pseq( a, inf),
\dur, 0.2
).play
)

// a beautiful tuning system can be created from the harmonic series.
(
e.stream=Pbind(
\freq, Pn(Pshuf( a, 1)),
\dur, 0.2,
\sustain, 0.8
).asStream
)
//////////////////////////////////////////////////////

