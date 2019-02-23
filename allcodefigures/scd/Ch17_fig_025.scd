Figure 17.25

a=(1..11);
(
a.size.do({ |i|
var x=a[i];
while({x>2},{x=x/2});
a.put(i, x)
});
)

b=a.asSet.asArray.sort;
(
e=Pbind(
\freq, Pn(Pshuf( b*440, 1)),
\dur, 0.2,
\sustain, 0.8
).play
)
//////////////////////////////////////////////////////

