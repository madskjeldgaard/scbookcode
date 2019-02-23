

SynthDef("pan2", { | in = 0, out = 0, pos = 0, level = 1 |
	Out.ar(out, Pan2.ar(In.ar(in), pos, level));
})

/*

f = FX.new;
p = f.addFX("pan2");
m = f.addSource("martianWinter");
p.set(\pos, 0.4);
c = p.addControl(\pos, "krsaw");
c.set(\freq, 0.091);

f.free; // frees all fx and synths and any synths that control these and were 
		// added with addControl
a = Synth("martianWinter");
f = FXgroup.new;
*/