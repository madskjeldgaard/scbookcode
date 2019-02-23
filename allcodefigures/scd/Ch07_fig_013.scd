Figure 7.13

(

Tdef(\a, { 10.do { (instrument: \wave, freq: 50.rand + 1500).play; 0.03.wait } });
Tdef(\b, { [1, 5, 1, 2, 8, 4, 12].do { |x| (instrument: \wave, note: x + 8).play; 0.1.wait } });
Tdef(\c, { "c is just a waiting message".postln; 2.wait; });


Tdef(\x, {
	loop {
		Tdef(\a).embed; // play in sequence
		1.wait;
		Tdef(\b).embed;
		2.wait;
		Tdef(\a).fork; // play in parallel
		Tdef(\b).fork;
		Tdef(\c).embed;
	}
}).play
);

// rewrite with infinite loop
Tdef(\a, { inf.do { (instrument: \wave, freq: 50.rand + 500).play; 0.1.wait } }); 
// rewrite with finite loop
Tdef(\a, { 10.do { (instrument: \wave, freq: 50.rand + 500).play; 0.1.wait } });


