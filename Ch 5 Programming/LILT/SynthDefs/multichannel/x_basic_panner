/* (IZ 2005-09-21) { 
	6 PanAz x 4 channels. One for each output channel of x1, x2, x3 (Left+Right)
	numChans, in, pos = 0.0, level = 1.0, width = 2.0, orientation = 0.5;
	
} */

/*
SynthDef("x_basic_panner", { |
	in = 0, out = 2, 
	x1l_level = 1.0
	|
	
	Out.ar(out, In.ar(in + 8) * x1l_level);
	Out.ar(out, In.ar(in + 9) * x1l_level);
	Out.ar(out, In.ar(in + 10) * x1l_level);
	Out.ar(out, In.ar(in + 11) * x1l_level);
	Out.ar(out, In.ar(in + 12) * x1l_level);
	Out.ar(out, In.ar(in + 13) * x1l_level);
});
*/


SynthDef("x_basic_panner", { |
	input = 0, out = 2,
	x1l_pos = 0.0, x1l_level = 1.0, x1l_width = 2.0,
	x1r_pos = 0.0, x1r_level = 1.0, x1r_width = 2.0,
	x2l_pos = 0.0, x2l_level = 1.0, x2l_width = 2.0,
	x2r_pos = 0.0, x2r_level = 1.0, x2r_width = 2.0,
	x3l_pos = 0.0, x3l_level = 1.0, x3l_width = 2.0,
	x3r_pos = 0.0, x3r_level = 1.0, x3r_width = 2.0
	|
	Out.ar(out, PanAz.ar(4, In.ar(input + 8), x1l_pos, x1l_level, x1l_width, 0));
	Out.ar(out, PanAz.ar(4, In.ar(input + 9), x1l_pos, x1l_level, x1l_width, 0));
	Out.ar(out, PanAz.ar(4, In.ar(input + 10), x1l_pos, x1l_level, x1l_width, 0));
	Out.ar(out, PanAz.ar(4, In.ar(input + 11), x1l_pos, x1l_level, x1l_width, 0));
	Out.ar(out, PanAz.ar(4, In.ar(input + 12), x1l_pos, x1l_level, x1l_width, 0));
	Out.ar(out, PanAz.ar(4, In.ar(input + 13), x1l_pos, x1l_level, x1l_width, 0));
})
