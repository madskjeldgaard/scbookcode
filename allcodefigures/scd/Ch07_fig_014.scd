
Figure 7.14
(
Tdef(\a, { |in|
	in.at(\n).do { |i| 
		in = (instrument: \wave, detune: 5.rand2).putAll(in);
		in.postln.play; 
		in.delta.wait;
	} 
})
);

(
Tdef(\x, { |inevent|
	loop {
		Tdef(\a).embed((note: [15, 17], dur: 0.01, n: 13));
		1.wait;
		Tdef(\a).embed((note: 9, dur: 0.4, n: 4));
		1.wait;
	}
}).play;
)



