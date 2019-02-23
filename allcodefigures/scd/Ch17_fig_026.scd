Figure 17.26 

a=(1..8);
b=(9..16);
(
a.size.do({ |i|
var x=a[i];
var y=b[i];
// harmonics below 8 remain in the first octave
while({x>2},{x=x/2});
// harmonics above 9 remain in the second octave
while({y>4},{y=y/2});
a.put(i, x);
b.put(i, y);
});
)
a;
b;
c=(a++b).asSet.asArray.sort;
c;

(
e=Pbind(
\freq, Pn(Pshuf( c*200, 1)),
\dur, 0.2,
\sustain, 1.1
).play
)
//////////////////////////////////////////////////////

