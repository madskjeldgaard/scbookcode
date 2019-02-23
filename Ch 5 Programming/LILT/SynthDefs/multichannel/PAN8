

SynthDef("pan8", { | in = 0, out = 0, pos = 0, level = 1, width = 2, orientation = 0.5 |
	Out.ar(out, PanAz.ar(8, In.ar(in), pos, level, width, orientation));
})

/*

f = FX.new;
p = f.addFX("pan8");
m = f.addSource("martianWinter");
p.set(\pos, 0.4);
c = p.addControl(\pos, "krsaw");
c.set(\freq, 0.091);

f.free; // frees all fx and synths and any synths that control these and were 
		// added with addControl
a = Synth("martianWinter");
f = FXgroup.new;
*/