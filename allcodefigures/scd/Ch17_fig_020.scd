Figure 17.20 

(
f = {|steps| Array.fill(steps, { |i| 2.pow(i/steps) }) };
//	Calculation of the twelve equal-note temperament
x =  f.(12);
//	mapping the tuning to a new range beyond an octave
y = x.linlin(1, 2, 1, 2.25);
//	multiplying by a root frequency
a=y*440;

Pbind(
\freq, Pfunc({ a.choose }),
\dur, 0.25,
\octave, 5,
\amp, 0.5,
\sustain, 1.1,
\instrument, \tone2
).play
)
//////////////////////////////////////////////////////

