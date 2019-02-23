/* IZ 2007-02-02 { SC3
Developing the saving of MIDI specs to script
} */

var w, master_button, save_button, numbers, array, responder_array;

~save_func = {
	responder_array.objects.collect({ | o |
		[o, o.getMIDIbinding]
	}).postln;
};

~start = {
	w = SCWindow("Responder Array", Rect(200, 400, 180, 140));
	w.view.decorator = FlowLayout(w.view.bounds, 2@2, 2@2);

	master_button = SCButton(w, Rect(0, 0, w.view.decorator.innerBounds.width - 6 / 2, 20));
	master_button.states = [["MIDI control on"], ["MIDI control off"]];

	save_button = SCButton(w, Rect(0, 0, w.view.decorator.innerBounds.width - 6 / 2, 20));
	save_button.states = [["Save"]];
	save_button.action = ~save_func;

	numbers = { SCNumberBox(w, Rect(0, 0, 40, 20)).value_(0) } ! 3 /* 16 */;
	array = numbers.collect({ | n | [n, nil]}).flat;
	responder_array = MIDIResponderArray(w, array);
	responder_array.learnMIDI;
	master_button.action = { | me |
	[{ responder_array.activate }, { responder_array.deactivate }][me.value].value;
	};
	w.onClose = {
		MIDIHandler.stopLearning;
		responder_array.removeMIDI;
	};
	w.front;
};

~stop = { w.close; w = nil; }
