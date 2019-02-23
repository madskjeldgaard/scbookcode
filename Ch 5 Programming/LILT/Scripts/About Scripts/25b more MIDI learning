/* IZ 2007-01-31 { SC3
} */

var w, b, s, c, t;

~start = {
	w = SCWindow("MIDI Learning", Rect(100, 400, 400, 150));
	w.view.decorator = FlowLayout(w.view.bounds, 2@2, 2@2);
	t = SCTextView(w.view, Rect(0, 0, w.view.decorator.innerBounds.width, 120));
	t.font = Font("Helvetica", 12);
	t.string = "This script demonstrates how to make any object learn to react to incoming MIDI interactively. To start, move 3 different MIDI controllers.  The button will learn to react to the first one, the number box to the second one and the slider to the third one. When the value of the first controller is above 20, the number boxes response to MIDI will be activated and that of the slider deactivated, and vice versa. ";
	c = SCButton(w, Rect(0, 0, 120, 20))
		.states_([["number button", Color.black, Color.white], ["slider", Color.black, Color.white]])
		.action_({ |me|
			[{ b.activateMIDI; s.deactivateMIDI; },
			 { b.deactivateMIDI; s.activateMIDI; },
			][me.value].value;
		});
	c.addDependant { | who, what |
		switch (what,
		\waitingForMIDIbinding, {
			c.states = c.states do: { | s | s[2] = Color.yellow };
			c.refresh;
		},
		\midiActive, {
			c.states = c.states do: { | s | s[1] = Color.black; s[2] = Color.red };
			c.refresh;
		},
		\midiInactive, { "inactive BUTTON".postln;
			c.states = c.states do: { | s | s[1] = Color.grey; s[2] = Color.clear };
			c.refresh;
		}
		)
	};
	b = SCButton(w, Rect(0, 0, 50, 20))
		.states_((_.asString ! 128).bubble(1) +++ [[Color.black, Color.white]]);
	b.addDependant { | who, what |
		switch (what,
		\waitingForMIDIbinding, {
			[b, \waitingForMIDIbinding].postln;
			b.states = b.states do: { | s | s[2] = Color.yellow };
			b.refresh;
		},
		\midiActive, {
			b.states = b.states do: { | s | s[1] = Color.red; s[2] = Color.white };
			b.refresh;
		},
		\midiInactive, { "inactive BUTTON".postln;
			b.states = b.states do: { | s | s[1] = Color.grey; s[2] = Color.clear };
			b.refresh;
		}
		)
	};
	s = SCSlider(w, Rect(0, 0, 200, 20));
	s.addDependant { | who, what | 
		switch (what,
		\waitingForMIDIbinding, {
			s.knobColor = Color(1, 1, 0, 0.5);
		}, 
		\midiActive, {
			s.knobColor = Color.red;
		},
		\midiInactive, {  "inactive SLIDER".postln;
			s.knobColor = Color.grey;
		}
		)
	};

	w.front;
	w.onClose = {
		MIDIHandler.stopLearning; // just in case MIDI learning was not completed
		s.release.removeMIDI;
		b.release.removeMIDI;
		c.release.removeMIDI;
	};

	MIDIHandler.teachArray(c, { | src, ch, key, val |
		{ c.valueAction = (val > 20).not.binaryValue }.defer;
	}, b, nil, s, nil).learn;
};

~stop = { w.close; w = nil; };
