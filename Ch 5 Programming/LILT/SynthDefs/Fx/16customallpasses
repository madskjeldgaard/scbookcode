/* IZ 050723
From Groups.rtf help file 
Not yet done 
*/

SynthDef("16customallpasses", { | in = 0, out = 0, sourcegain = 0.5, wet = 0.5,
	dl1 = 0.001, dl2 = 0.002, dl3 = 0.003, dl4 = 0.004, 
	dl5 = 0.005, dl6 = 0.006, dl7 = 0.007, dl8 = 0.008, 
	dl9 = 0.009, dl10 = 0.010, dl11 = 0.012, dl12 = 0.013, 
	dl13 = 0.014, dl14 = 0.015, dl15 = 0.016, dl16 = 0.017, 
	dc1 = 5.0, dc2 = 5.0, dc3 = 5.0, dc4 = 5.0, 
	dc5 = 5.0, dc6 = 5.0, dc7 = 5.0, dc8 = 5.0, 
	dc9 = 5.0, dc10 = 5.0, dc11 = 5.0, dc12 = 5.0, 
	dc13 = 5.0, dc14 = 5.0, dc15 = 5.0, dc16 = 5.0 |
	var src, processed; 
	src = processed = In.ar(in);
	[[dl1, dl2, dl3, dl4, 
	dl5, dl6, dl7, dl8, 
	dl9, dl10, dl11, dl12, 
	dl13, dl14, dl15, dl16], 
	[dc1, dc2, dc3, dc4, 
	dc5, dc6, dc7, dc8, 
	dc9, dc10, dc11, dc12, 
	dc13, dc14, dc15, dc16]].flop.do { | d |
		processed = AllpassC.ar(processed, 1.0, d[0], d[1]) 
	 };
	Out.ar(out, (src * sourcegain) + (processed * wet)); 
});

