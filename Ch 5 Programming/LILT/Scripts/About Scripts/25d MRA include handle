/* IZ 2007-01-31 { SC3

This example shows that the object that is the "handle" under which the MIDIResponderArray is stored can also be included as an object in the array, and therefore itself be controlled by MIDI. This is useful to control a Script's on/off by MIDI control and thus include it with its other parameters in the array, while having the Script as a handle of the MIDI array in order to activate / deactivate MIDI control of both the Script and its Parameters. 

The master-button at the top of the window activates MIDI control of all numbers when it receives MIDI value 50, deactivates it when it receives MIDI value 100 and deactivates reception of the entire array - including itself - when it receives value 0. 

} */

var w, master_button, numbers, array, responder_array;
var adapter;

adapter = { | me, how |
	switch(how,
		\waitingForMIDIbinding, { { me.stringColor = Color.yellow }.defer },
		\midiActive, { { me.stringColor = Color.red }.defer },
		\midiInactive, { { me.stringColor = Color.grey(0.7) }.defer }
	)
};

~start = {
	w = SCWindow("Responder Array", Rect(200, 400, 180, 49));
	w.view.decorator = FlowLayout(w.view.bounds, 2@2, 2@2);
	master_button = SCButton(w, Rect(0, 0, w.view.decorator.innerBounds.width, 20));
	master_button.states = [["Number control on"],
		["Number control off"], ["Handle removed: END!"]
	];
	numbers = { SCNumberBox(w, Rect(0, 0, 40, 20)).value_(0) } ! 4;
	numbers do: _.addDependant(adapter);
	array = [master_button, { | src, chan, key, val |
		{ switch (val, 
			0, { master_button.valueAction = 2 },
			50, { master_button.valueAction = 0 }, 
			100, { master_button.valueAction = 1 }
		  );
		}.defer;
	}] ++ numbers.collect({ | n | [n, nil]}).flat;
	responder_array = MIDIResponderArray(master_button, array);
	responder_array.learnMIDI;
	master_button.action = { | me |
	[	{ responder_array.activate(*(1..4)) },
		{ responder_array.deactivate(*(1..4)) },
		{ responder_array.deactivate }
	][me.value].value;
	};
	w.onClose = {
		numbers do: _.release;
		MIDIHandler.stopLearning;
		responder_array.removeMIDI;
	};
	w.front;
};

~stop = { w.close; w = nil; }
