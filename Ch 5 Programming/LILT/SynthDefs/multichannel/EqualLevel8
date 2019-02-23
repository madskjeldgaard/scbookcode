/* (IZ 2005-10-29) { 
Send source from one input bus to 8 output busses at equal level.

} */

SynthDef('EqualLevel8', { | in = 0, out = 0, level = 0.5 |
	var src;
	src = In.ar(in) * level;
	Out.ar(out, [src, src, src, src, src, src, src, src]);
})