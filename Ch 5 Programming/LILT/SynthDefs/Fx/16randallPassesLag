/* IZ 050723
From Groups.rtf help file 
*/

SynthDef("16randallpassesLag", { | in = 0, out = 0, sourcegain = 0.5, wet = 0.5, ampLag = 2.5 |
	var src, processed; 
	src = processed = In.ar(in);
	16.do {  processed = AllpassC.ar(processed, 0.04, Rand(0.001,0.04), 3) };
	Out.ar(out, (src * sourcegain) + ( Lag.kr(wet, ampLag) * processed)); 
})

