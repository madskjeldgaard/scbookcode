/* IZ 2007-01-31 { SC3

Example of activating/deactivating multiple items' MIDI bindings with MIDIResponderArray. 
You have to teach the items with input from MIDI controllers first. 

} */

var w, master_button, numbers, array, responder_array;

~start = {
	w = SCWindow("Responder Array", Rect(200, 400, 180, 140));
	w.view.decorator = FlowLayout(w.view.bounds, 2@2, 2@2);
	master_button = SCButton(w, Rect(0, 0, w.view.decorator.innerBounds.width, 20));
	master_button.states = [["MIDI control on"], ["MIDI control off"]];
	numbers = { SCNumberBox(w, Rect(0, 0, 40, 20)).value_(0) } ! 16 /* 3 */;
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
