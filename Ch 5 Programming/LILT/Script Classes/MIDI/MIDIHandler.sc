/* IZ 2007-01-29 { SC3
MIDIHandler registers objects that want to want to bind actions to MIDI and makes their MIDIresponders for them with the help of MIDIListener. Its instance variable 'bound' is an IdentityDictionary where each object is associated to its MIDIbinding. That way, any object can have a MIDIresponder bound to it, without having to store it as an own instance variable. (This is similar to the way that 'dependants' are implemented for Object.) 

Usage:
MIDIHandler.teach(anObject, aFunction); // adds object to Array of objects to add
MIDIHandler.teachArray(... pairs); // Adds array of pairs of objects and functions to add
MIDIHandler.learn;	// start the listening process for adding objects registered with 'teach'
					// to incoming midiResponders created by MIDIListener
MIDIHandler.remove(... objects); // removes all objects in the argument list from the 

MIDIHandler.activate(... objects);  // 
MIDIHandler.deactivate(... objects); 

/// Methods added to Object: 
anObject.learnMIDI(aFunction);	// Add anObject to the list of learning objects 
		// if aFunction is nil, then MIDIHandler will ask the Object to 
		// give its default MIDI function by sending it the message: anObject.midiAction.
// To add an object and start the binding process from incoming MIDI in one statement, 
// chain the messages: 
anObject.learnMIDI(aFunction).learn;

anObject.removeMIDI;		// Remove anObject's MIDIresponder and remove anObject from 'bound'
anObject.activateMIDI;		// add anObject's MIDIresponder (i.e. make it active)
		// and add it to the set of active objects. 
		// Also notify dependants of that object with: anObject.changed(\midiActive);
		// If learning is on, it will be stopped.
anObject.deactivateMIDI;	// remove anObject's MIDIresponder (i.e. make it inactive)
		// and 
		// Also notify dependants of that object with: anObject.chnged(\midiInactive);

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

[b, s] do: _.learnMIDI;	
MIDIHandler.learn;

// Alternatively: 
// MIDIHandler.teachArray(b, nil, s, nil).learn;
)

Note: This is a Singleton Class, i.e. it works with a single instance stored in classvar "handler".

}
*/

MIDIHandler {
	classvar handler;	// single instance of MIDIhandler that does the work.
	var <bound; 		// all objects that have MIDIresponders. 
						// IdentityDictionary: Object->MIDIresponder
	var <active;		// those objects whose MIDIresponders are active. Set.
	var <learningArray;	// Array of associations of objects and functions that are currently 
						// "learning" their MIDI bindings 
						// The Handler will go through successive items in the array and 
						// assign each to a responder created from a different controller,
						// until notified to stop the learning mechanism or until all items in 
						// the array have received a binding.
	*handler {
		if (handler.isNil ) { ^handler = this.new };
		^handler;
	}
	*new {
		^this.newCopyArgs(IdentityDictionary.new, Set.new, [], Set.new);
	}
	*teachArray { | ... objActionPairs | this.handler.teachArray(*objActionPairs) }
	teachArray { | ... objActionPairs |
		// teachArray removes any items left from previous unfinished learnings
		learningArray = [];
		objActionPairs pairsDo: { | obj, action | this.teach(obj, action) };
	}
	*teach { | object, action | this.handler.teach(object, action) }
	teach { | object, action |
		action = action ?? {
			if (object respondsTo: \midiAction) {
				object.midiAction;
			}{
				{ | ... midiArgs | Post << object <<* midiArgs << "\n" }
			}
		};
		learningArray = learningArray addFirst: [object, action];
	}
  	*getMIDIbinding { | anObject | ^this.handler.getMIDIbinding(anObject) }
	getMIDIbinding { | anObject | ^bound[anObject] }
	*isActive { | object | ^this.handler.isActive(object) }
	isActive { | object |
		^active.includes(object) }
	*learn { ^this.handler.learn }
	learn {
/*		if (this.isLearning) {
			^thisMethod.report("Already learning");
		};
*/		if (learningArray.size == 0) {
			^thisMethod.report("There are no objects waiting to be bound. Exiting learning now")
		};
		MIDIListener.init.startListening;	// may be improved!
		MIDIListener.addDependant(this);
		this.markLearningItem;
	}
	isLearning { ^MIDIListener.isListening }
	// bind next object in Array 'learning' to responder received from MIDIListener
	*addNextResponder { | responder |
		this.handler.addNextResponder(responder);
	}
	update { | who, what |
		//		thisMethod.report(who, what);
		// these checks will be improved when MIDIListener is rewritten to use 
		// Singleton pattern and be a kind of Model.
		if (what.isKindOf(Array)) {
		}{
			this.addNextResponder(what);
		}
	}
	addNextResponder { | responder |
		this.add(responder, *this.getNextLearningItem).install;
	}
	*add { | responder, object, action | ^this.handler.add(responder, object, action) }
	add { | responder, object, action |
		if (action.notNil) { responder.function = action };
		this.register(responder, object);
		this.activate(object);
		^responder;
	}
	*register { | responder, object | this.handler.register(responder, object) }
	register { | responder, object |
		if (bound[object].notNil) { bound[object].remove };
		bound[object] = responder;
	}
	getNextLearningItem {
		var next;
		next = learningArray.pop;
		this.markLearningItem;
		^next;
	}
	markLearningItem {
		var waiter;
		if (learningArray.size == 0) {
			this.stopLearning;
		}{
			waiter = learningArray.last[0];
			if (waiter.isKindOf(Ref)) { waiter = waiter.value };
			{ waiter.changed(\waitingForMIDIbinding); }.defer;
		}
	}
	skipNextLearningItem {
		// only called from user command - not via MIDIListener. 
		var current = learningArray.last[0];
		if (this.isReallyActive(current)) {
			current.changed(\midiActive)
		}{
			current.changed(\midiInactive)
		};
		this.getNextLearningItem;
	}
	*remove { | object |
		this.handler.remove(object);
	}
	remove { | object |
		this.deactivate(object);
		bound[object] = nil;
	}
	*stopLearning { this.handler.stopLearning }
	stopLearning {
		var current, state;
		if (this.isLearning.not) {
			^thisMethod.report("Learning is not active.");
		};
		if (learningArray.size > 0) {
			current = learningArray.last[0];
	//		thisMethod.report("active objects:", active, "current:", current);
			state = if ( this.isReallyActive(current) )
				{ \midiActive } { \midiInactive };
			if (current.isKindOf(Ref)) { current = current.value };
			{ current.changed(state); }.defer;
		};
		MIDIListener.stopListening;
		MIDIListener.removeDependant(this);
		thisMethod.report("DONE!");
	}
	isReallyActive { | object |
		// glitch with MIDIResponderArray activating all its objects at the 
		// onset of learning. Not corrected yet, but patched here: 
		^bound[object].notNil and: { this.isActive(object) }
	}
	*activate { | ... objects | this.handler.activate(*objects) }
	activate { | ... objects |
		if (objects.size == 0) { objects = bound.keys };
//		thisMethod.report(this, objects);
		objects do: this.activate1(_);
	}
	activate1 { | object |
//		thisMethod.report(this, object);
		this.prActivate(object);
		active.add(object);
	}
	prActivate { | object |
		var responder;
//		thisMethod.report(this, object, "RESPONDER!!!!!!!!!!!!!!!!!!!!!!!!!:", responder = bound[object]);
		if ((responder = bound[object]).notNil) {
//			thisMethod.report(this, responder, if (responder.isKindOf(MIDIResponder)) { [responder.function, responder.function.def.sourceCode] });
			responder.install;
//			thisMethod.report(this, "the responder has been installed: ", responder);
			if (object.isKindOf(Ref)) { object = object.value };
			{ object.changed(\midiActive); }.defer;
		};
	}
	*deactivate { | ... objects | this.handler.deactivate(*objects) }
	deactivate { | ... objects |
		if (objects.size == 0) { objects = bound.keys };
		objects do: this.deactivate1(_)
	}
	deactivate1 { | object |
		this.prDeactivate(object);
		active.remove(object);
	}
	prDeactivate { | object |
		var responder;
		if ((responder = bound[object]).notNil) {
			responder.remove;
			if (object.isKindOf(Ref)) { object = object.value };
			{ object.changed(\midiInactive); }.defer;
		};
	}
}

/*

(
w = SCWindow.new;
w.view.decorator = FlowLayout(w.view.bounds, 2@2, 2@2);
b = SCButton(w, Rect(0, 0, 50, 20))
	.states_((_.asString ! 127).bubble(1) +++ [[Color.black, Color.white]]);
s = SCSlider(w, Rect(0, 0, 200, 20));
b.addDependant { | who, what | 
	switch (what,
	\waitingForMIDIbinding, {
		b.postln;
		b.states = b.states do: { | s | s[2] = Color.red }
	}, 
	\midiActive, {
		b.states = b.states do: { | s | s[1] = Color.red; s[2] = Color.white }
	})
};
s.addDependant { | who, what | 
	switch (what,
	\waitingForMIDIbinding, {
		s.postln;
		s.knobColor = Color(1, 0, 0, 0.5);
	}, 
	\midiActive, {
		s.knobColor = Color.red;
	})
};
w.front;
w.onClose = { s.release.removeMIDI; b.release.removeMIDI };
[b, s] do: _.learnMIDI;	
MIDIHandler.learn;

)


(
w = SCWindow.new;
w.view.decorator = FlowLayout(w.view.bounds, 2@2, 2@2);
b = SCButton(w, Rect(0, 0, 50, 20))
	.states_((_.asString ! 127).bubble(1) +++ [[Color.black, Color.white]]);
s = SCSlider(w, Rect(0, 0, 200, 20));
c = SCButton(w, Rect(0, 0, 50, 20))
	.states_([["button"], ["slider"]])
	.action_({ |me|
		[{ b.activateMIDI; s.deactivateMIDI; }, 
		 { b.deactivateMIDI; s.activateMIDI; }, 
		][me.value].value;
	});
b.addDependant { | who, what | 
	switch (what,
	\waitingForMIDIbinding, {
		b.states = b.states do: { | s | s[2] = Color.red }
	}, 
	\midiActive, {
		b.states = b.states do: { | s | s[1] = Color.red; s[2] = Color.white };
		b.refresh;
	},
	\midiInactive, { "inactive BUTTON".postln;
		b.states = b.states do: { | s | s[1] = Color.blue; s[2] = Color.green };
		b.refresh;
	}
	)
};
s.addDependant { | who, what | 
	switch (what,
	\waitingForMIDIbinding, {
		s.knobColor = Color(1, 0, 0, 0.5);
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
w.onClose = { s.release.removeMIDI; b.release.removeMIDI; c.release.removeMIDI };

MIDIHandler.teachArray(b, nil, s, nil, c, { | src, ch, key, val |
	{ c.valueAction = (val > 20).not.binaryValue }.defer;
}).learn;
)

*/