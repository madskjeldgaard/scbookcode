Figure 11.9
// GUI Example
(
	SynthDef(\gui, { arg out=0, freq=8, decay=0.11;
		var in = Formlet.ar(Impulse.ar(freq, 0, 0.4), 800, 0.01, decay);
		Out.ar(out, in ! 2);
	}).add;
)
(
	a = Synth.newPaused(\gui);
	w = Window.new( "A GUI window", Rect( 128, 64, 340, 250 ));
	w.view.decorator = FlowLayout( w.view.bounds );

	b = Button.new( w, Rect( 50, 20, 75, 24 ));
	b.states = [[ "Start"],[ "Stop"]];
	b.action_({ arg butt;
		if (butt.value == 1, { a.run }, { a.run(false) });	
	});

	c = Slider2D.new( w, Rect(50, 120, 330, 200));
	c.x_(0.5).y_(0.5).action_({|sl|
			a.setn(\freq, [sl.x * 10 + 3, sl.y / 5 + 0.01]);
	});
	w.front;
)

