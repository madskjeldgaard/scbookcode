/* IZ 2007-01-30 { SC3
Adding ability for any Object to bind any action with itself as argument to respond to a MIDI message. 
See classes MIDIHandler and MIDIListener. 

(
w = SCWindow.new;
w.view.decorator = FlowLayout(w.view.bounds, 2@2, 2@2);
b = SCButton(w, Rect(0, 0, 50, 20));
s = SCSlider(w, Rect(0, 0, 200, 20));
w.front;	

[b, s] do: _.learnMIDI;	
MIDIHandler.learn;

// Alternatively: 
// MIDIHandler.teachArray(b, nil, s, nil).learn;
	
)

} */

+ Object {
	// build my responder from incoming MIDI and action or a default action
	learnMIDI { | action |
		^MIDIHandler.teach(this, action)
	}
	// Return my MIDIresponder if it exists
	getMIDIbinding {
		^MIDIHandler.getMIDIbinding(this)
	}
	// deactivate my responder and remove myself from MIDIHandler
	// note: to add an object directly, use MIDIHandler.add(object, action, responder)
	removeMIDI {
		^MIDIHandler.remove(this)
	}
	// activate my responder
	activateMIDI {
		^MIDIHandler.activate(this)
	}
	// deactivate my responder 
	deactivateMIDI {
		^MIDIHandler.deactivate(this)
	}
}

+ SCControlView {
	midiAction {
		^{ | src, chan, ctrl, val | { this.valueAction = val }.defer }
	}
}

+ SCNumberBox {
	midiAction {
		^{ | src, chan, ctrl, val | { this.valueAction = val }.defer }
	}
}

+ SCSlider {
	midiAction {
		^{ | src, chan, ctrl, val | { this.valueAction = val / 127 }.defer }
	}
}