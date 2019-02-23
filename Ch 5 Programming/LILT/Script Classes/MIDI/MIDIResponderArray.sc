/* IZ 2007-01-30 { SC3
Holds an Array of objects paired with their actions to enable the teaching of the objects and their activation / deactivation as a group via MIDIHandler

MIDIresponderArray imitates parts of the behaviors of an object that is bound to midi and of a MIDIResponder. But instead of installing itself as the object it installs itself as the responder in order to handle the activation / deactivation of the responders of the array of objects it contains.

(
var w, master_button, numbers, array;
w = SCWindow("Responder Array", Rect(200, 400, 200, 180));
w.view.decorator = FlowLayout(w.view.bounds, 2@2, 2@2);
master_button = SCButton(w, Rect(0, 0, w.view.decorator.innerBounds.width, 20));
master_button.states = [["MIDI control on"], ["MIDI control off"]];
numbers = { SCNumberBox(w, Rect(0, 0, 40, 20)).value_(0) } ! 16;
array = numbers.collect({ | n | [n, nil]}).flat;
MIDIHandler.teachArray(*array).learn;
w.front;
)

(
var w, master_button, numbers, array, responder_array;
w = SCWindow("Responder Array", Rect(200, 400, 180, 140));
w.view.decorator = FlowLayout(w.view.bounds, 2@2, 2@2);
master_button = SCButton(w, Rect(0, 0, w.view.decorator.innerBounds.width, 20));
master_button.states = [["MIDI control on"], ["MIDI control off"]];
numbers = { SCNumberBox(w, Rect(0, 0, 40, 20)).value_(0) } ! 3; // 16;
array = numbers.collect({ | n | [n, nil]}).flat;
responder_array = MIDIResponderArray(w, array);
responder_array.learnMIDI;
master_button.action = { | me |
	[{ responder_array.activate }, { responder_array.deactivate }][me.value].value;
};
w.front;
)

} */

MIDIResponderArray {
	var <handle;	// object that needs to bind the group of other objects responders
	var <object_action_pairs; // array of objects paired to functions for MIDI.
	var <objects;	// the objects from the above array

	*new { | handle, object_action_pairs |
		^this.newCopyArgs(handle, object_action_pairs).init
	}
	init {
		// If my handle is also objects list, enclose it in Ref so that
		// when added, its responder will not replace myself as responder
		var handleIndex;
		handleIndex = object_action_pairs indexOf: handle;
		if (handleIndex.notNil) { object_action_pairs[handleIndex] = `handle;
//			thisMethod.report("made reference like this:", object_action_pairs[handleIndex])
		};
		objects = object_action_pairs.clump(2).flop.first;
	}
	// -------------------- object behavior part: --------------------
	learnMIDI { | startNow = true |
		MIDIHandler.teachArray(*object_action_pairs);
		// register myself as responder to my object:
		MIDIHandler.add(this, handle);
		if (startNow) { MIDIHandler.learn };
	}
	removeMIDI {
		handle.removeMIDI;
		objects do: _.removeMIDI;
	}
	activate { | ... indices |
//		thisMethod.report(this, handle, object_action_pairs, objects, indices);
		if (indices.size == 0) {
			MIDIHandler.activate(handle)
		}{
			MIDIHandler.handler.activate(*objects[indices]);
		}
	}
	deactivate { | ... indices |
		if (indices.size == 0) {
			MIDIHandler.deactivate(handle)
		}{
			MIDIHandler.handler.deactivate(*objects[indices]);
		}
	}
	getMIDIbinding {
		^objects collect: _.getMIDIbinding;
	}
	// -------------------- MIDIResponder behavior part: --------------------
	install {
		objects do: _.activateMIDI;
	}
	remove {
		objects do: _.deactivateMIDI;
	}
	function_ {} // ignore function
	saveMIDI { | file |
		objects do: { | obj | obj.saveMIDI(file) };
	}
}
