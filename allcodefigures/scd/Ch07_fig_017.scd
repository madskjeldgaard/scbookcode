Figure 7.17

Pdef(\a).play; // play silence in sequence
Pdef(\a, Pbind(\instrument, \wave)); // insert a sequence of notes
Pdef(\a, Pbind(\instrument, \wave, \dur, Pseq([1, 3, 2, 3], inf) / 6)); // add some rhythm
Pdef(\a).pause;
Pdef(\a).resume;
Pdef(\a).stop;


