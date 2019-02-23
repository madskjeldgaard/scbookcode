Figure 17.13 

(
~rationames=[1/1, 8/7, 7/6, 6/5, 5/4, 4/3, 7/5, 10/7, 3/2, 8/5, 5/3, 12/7, 7/4];
~scale=[0,3,5,8,10,12];
e = Pbind(
\freq, Pseq([
Pfunc({
(~rationames.wrapAt(~scale).[~scale.size.rand])*440
})
],inf),
\dur, 0.25,
\amp, 0.5,
\instrument, \tone2
).play; // returns an EventStream
)
// set a new scale
~scale=[0,2,5,7,9,11];
~scale=[0,1,3,5,6,8,9];
~scale=[0,3,5,8,10,12];
//////////////////////////////////////////////////////

