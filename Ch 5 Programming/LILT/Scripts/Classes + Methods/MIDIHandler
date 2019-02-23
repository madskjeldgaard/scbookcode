/* IZ 2007-01-29 { SC3
MIDIHandler registers objects that want to want to bind actions to MIDI and makes their MIDIresponders for them with the help of MIDIListener. Its instance variable 'bound' is an IdentityDictionary where each object is associated to its MIDIbinding. That way, any object can have a MIDIresponder bound to it, without having to store it as an own instance variable. (This is similar to the way that 'dependants' are implemented for Object.) 

Usage:
MIDIHandler.add(anObject, aFunction); // adds object to Array of objects to bind
MIDIHandler.addArray(... pairs); // Adds array of pairs of objects and functions to bind
MIDIHandler.learn;	// start the listening process for binding objects registered with add
					// to incoming midiResponders created by MIDIListener
MIDIHandler.remove(... objects); // removes all objects in the argument list from the 

MIDIHandler.activate(... objects);  // 
MIDIHandler.deactivate(... objects); 

/// Methods added to Object: 
anObject.addMIDI(aFunction);	// Add anObject to the list of learning objects 
		// if aFunction is nil, then MIDIHandler will ask the Object to 
		// give its default MIDI function by sending it the message: anObject.defaultMIDIfunction.
// To add an object and start the binding process from incoming MIDI in one statement, 
// chain the messages: 
anObject.addMIDI(aFunction).learn;

anObject.removeMIDI;		// Remove anObject's MIDIresponder and remove anObject from 'bound'
anObject.activateMIDI;		// add anObject's MIDIresponder (i.e. make it active)
		// and add it to the set of active objects. 
		// Also notify dependants of that object with: anObject.changed(\midiActive);
		// If learning is on, it will be stopped.
anObject.deactivateMIDI;	// remove anObject's MIDIresponder (i.e. make it inactive)
		// and 
		// Also notify dependants of that object with: anObject.changed(\midiInactive);

Objects receive following change messages from MIDIHandler to notify their dependants,
in order for example to change the states of some of their dependant GUI items:

anObject.changed(\waitingForMIDIbinding); // started waiting for next MIDIresponder binding
anObject.changed(\midiActive); // MIDIresponder of this object was activated.
anObject.changed(\midiInactive); // MIDIresponder of this object was deactivated.

(
w = SCWindow.new;
w.view.decorator = FlowLayout(w.view.bounds, 2@2, 2@2);
b = SCButton(w, Rect(0, 0, 50, 20)).states_((_.asString ! 127).bubble(1));
s = SCSlider(w, Rect(0, 0, 200, 20));
w.front;	

[b, s] do: _.addMIDI;	
MIDIHandler.learn;

// Alternatively: 
// MIDIHandler.addArray(b, nil, s, nil).learn;
)

Note: This is a Singleton Class, i.e. it works with a single instance stored in classvar "handler".

} */