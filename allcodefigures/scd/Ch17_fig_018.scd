Figure 17.18 

(
~tolerance={|a, b, t, max|
var c, d;
c=[];
d=[];
a.do({ |aitem, aindex|
b.do({ |bitem, bindex|
var x;
x = (aitem-bitem).abs;
if( (x > t) && (x < max),
{
c=c.add(aindex);
d=d.add(bindex);
//[aitem, bitem].post; " out of tune ".post; [aindex, bindex].postln;
//" ".postln;
})
})
});
[(0..a.size).difference(c), (0..b.size).difference(d)];
};
)

(
// use the function function with two tunings
var mintreshold, maxtreshold, int;

// two different equal tunings expressed linearly
a=Array.fill(12, { |i| (1/12)*(i) });
b=Array.fill(21, { |i| (1/21)*(i) });

int=1/21;				// smallest interval
mintreshold=int*0.15;
maxtreshold=int*0.85;
/*
intervals inferior to mintreshold are in tune
intervals between mintreshold and maxtreshold are out of tune
intervals superior to maxtreshold are in tune
*/

// print a list of notes from the two tunings which form a dissonant interval
~tolerance.value(a, b, mintreshold, maxtreshold);
)
//////////////////////////////////////////////////////

