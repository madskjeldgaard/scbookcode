/* IZ 050725 {

Make and return a MIDIResponder based on the first incoming midi message - with GUI.
070130: Reworking MIDIListener to work with MIDIHandler for binding any object to MIDI. 
All GUI aspects of this class will be removed when a MIDI Monitor script is completed.

MIDIListener.makeGui;

Drag and drop a Script onto the drag sink of the top line to start a new binding. Function editing is for general purposes but does not work with Scripts. A script must make its own function and include itself in the function in order to be notified.

As soon as the first MIDI message is received, a MIDIResponder is created that will match that message. All details of the MIDIResponder can be set via editable text fields.

There is a menu on the last line before the text field for selecting all kes stored in the dictionary stored in the instance variable midiResponders of the receiver object. This means that one can view, edit and or replace any of the existing MIDIresponders that are used by the receiver object, and one can add new responders bound to any new key in the midiResponders dictionary of the receiver.

050906 completed coupling to MIDIConfig. Aftertouch listening disabled because it interferes with note-on/off listening from touch-sensitive keyboards.

TODO: Change responder-type display to a menu so that the user can chose the type of responder they want to create, in manual (non-listening) mode. This will enable making + editing of Aftertouch responders.

} */

MIDIListener {
	// { responders that listen to catch the next message and create responder for assignment:
	classvar <noteon, <noteoff, <noteonoff, <cc, <aftertouch, <pitchbend;
	classvar <responder;	// the responder that was created by the newest caught midi input
	classvar <responderTypes; // provides string displaying the type of Responder received
	classvar <recentInputVector; 	// most recent input 
						// compare to next input, only react if different one received
	classvar <receiver;	// the object that will receive the next responder made
	classvar <>verbose = false; //if verbose post every message caught by own listening responders
	classvar <noteOnOffPairs = true;
	classvar <waitBeforeNext = 0.5;	// time to wait before listening to next input
	/* GUI ITEMS */
	classvar <window;
	classvar activationButton, typeDisplay, srcDisplay, chanDisplay,
		numDisplay, velDisplay, velAltDisplay;
	classvar receiverDisplay, funcDisplay, verboseButton, resendButton;
	classvar keyMenu, keyDisplay;
	classvar <noteOnOffButton;
	classvar statusDisplay; // TODO: for status messages: MIDI received, Receiver set etc.
	// ignore messages that have the same number as the last received MIDI command:
	classvar <sameNumFilterButton;
	classvar <noteOnOffPairsButton;	// control+display if noteOnOffPairs func is active
	classvar <waitBeforeNextView;
	classvar <responderDragSource;
	// }
	*initClass {
		responderTypes = (
			NoteOnResponder: "Note On",
			NoteOffResponder: "Note Off",
			NoteOnOffResponder: "Note On-Off",
			CCResponder: "Control Change",
			TouchResponder: "After Touch",
			BendResponder: "Pitch Bend"
		);
	}

	*init {
		this.initMIDI;	// always make sure MIDI is inited
		this.makeListeningResponders;
	}

	// { init 16 input and 16 output ports on MIDIClient.
	// that should cover most hardware/software configurations }
	*initMIDI { | doNotRescan = true |
		var inPorts = 16;
		var outPorts = 16;
		if (doNotRescan and: { MIDIClient.initialized }) { ^this };
		thisMethod.report(this, "INITIALIZING MIDI INPUT: SCANNING PORTS AND ACTIVATING INPUTS");
		MIDIClient.init(inPorts,outPorts);			// explicitly intialize the client
		inPorts.do({ arg i;
			MIDIIn.connect(i, MIDIClient.sources.at(i));
		});
	}

	*makeListeningResponders {
		noteon = NoteOnResponder({ | src, chan, num, vel|
			this.midiReceived(NoteOnResponder, src, chan, num, vel);
		}, nil, nil, nil, nil, false);
		noteoff = NoteOffResponder({ | src, chan, num, vel|
			this.midiReceived(NoteOffResponder, src, chan, num, vel);
		}, nil, nil, nil, nil, false);
		noteonoff = NoteOnOffResponder({ | src, chan, num, vel|
			this.midiReceived(NoteOnOffResponder, src, chan, num, vel);
		}, nil, nil, nil, nil, false);
		cc = CCResponder({ | src, chan, num, vel|
			this.midiReceived(CCResponder, src, chan, num, vel);
		}, nil, nil, nil, nil, false);
		// aftertouch listening impractical when using keyboards
		// because AfterTouch comes right after a NoteOn so one 
		// cannot uncouple it to assign it to an object of ones choice
/*		aftertouch = TouchResponder({ | src, chan, num, vel |
			this.midiReceived(TouchResponder, src, chan, nil, num);
		}, nil, nil, nil, false);
*/		pitchbend = BendResponder({ | src, chan, num, vel|
			this.midiReceived(BendResponder, src, chan, nil, num);
		}, nil, nil, nil, false);
	}

	*midiReceived { | type, src, chan, num, vel |
		if (verbose) {[type, src, chan, num, vel].postln;};
		// React only when a different controller has been activated:
		if ([src, type, chan, num] == recentInputVector) { ^this };
		recentInputVector = [src, type, chan, num];
		{	this.makeResponder(type, src, chan, num);
			if (window.notNil){
			this.displayResponder(type, src, chan, num, vel);
			};
			this.setResponder(type);
		}.defer;
/*		{
			this.resetInputVector;
			this.changed(\next);
		}.defer(waitBeforeNext);
*/	}

	*makeResponder { | type, src, chan, num, vel |
		// Make a responder that corresponds to the MIDI message just received.
		// This will be assigned to an object to make a MIDI binding for it.
		if (type.isKindOf(TouchResponder)) {
		// Function is assigned from object or other sources
		// Source is nil to match any source by default.
		// Only channel and num will be matched.  
			responder = type.new(nil, nil, chan, num, install: false);
		}{
			responder = type.new(nil, nil, chan, num, vel, install: false);
		};
		if (window.notNil) {
			responderDragSource.object = responder;
		};
//		MIDIHandler.addNextResponder(responder);
		this.changed(responder);
//		thisMethod.report(this.dependants);
	}

	*setResponder { | type |
		var key;
		if (receiver.isNil) { ^this };
		// the receiver will make the function based on the funcDisplay string.
 		key = keyDisplay.string.compile.value;
 		if (key.isNil) {
 			key = type.asSymbol;
 			keyDisplay.setString("", 0, 1000).string = key.asString;
 		};
		receiver.addMIDIResponder(responder, funcDisplay.string, key);
	}

	*addReceiver { | argReceiver, actionString |
		receiver = argReceiver;
		if (window.isNil) { ^nil };
		funcDisplay.setString("", 0, 50000);
		funcDisplay.string = actionString ?? {
			if (receiver.respondsTo(\defaultMIDIAction)) { receiver.defaultMIDIAction } { this.defaultAction; }
		};
		receiverDisplay.string = receiver.asCompileString;
		this.reset;
	}

	*reset {
		this.startListening;
		this.resetResponderDisplays;
	}

	// add responders back to listen to input for another binding
	*startListening {
		this.addListeningResponders;
		this.resetInputVector; // used separately by midiReceived
		this.changed([\isLearning, true]);
		if (window.notNil) { activationButton.value = 1 };
	}
	*addListeningResponders {
		if (noteOnOffPairs) {
		// install is a safe method: it does not add the responder if it is already added
			[noteonoff, cc, /* aftertouch, */ pitchbend] do: _.install;
			noteonoff.noteOffResponder.remove;	// ignore note offs
		}{										// install: safe!
			[noteon, noteoff, cc, /* aftertouch, */ pitchbend] do: _.install;
		}
	}
	*isListening { ^CCResponder.ccr.notNil and: { CCResponder.ccr.includes(cc) } }
	// reset after waiting to ignore input for set interval
	*resetInputVector { recentInputVector = nil; }
	*resetResponderDisplays {
		var mev;
		if (receiver.isNil or: { (responder = receiver.midiResponder).isNil } ) {
			srcDisplay.string_("     -      ");
			typeDisplay.string_("     -      ");
			chanDisplay.setString("", 0, 1000).string_("nil");
			numDisplay.setString("", 0, 1000).string_("nil");
			velDisplay.setString("", 0, 1000).string_("nil");
		} {
			mev = responder.matchEvent;
			this.displayResponder(responder.class, mev.port, mev.chan, mev.b, mev.c);
		}
	}

	*displayResponder { | type, src, chan, num, vel |
		typeDisplay.string = responderTypes[type.name];
		srcDisplay.string = if (src.isNil) { "nil" } {
			(MIDIClient.sources.detect { |s| s.uid == src }).name.asString;
		};
		chanDisplay.setString("", 0, 100).string = chan.asString;
		numDisplay.setString("", 0, 100).string = num.asString;
		velDisplay.setString("", 0, 100).string = "nil";
		velAltDisplay.string = "  (" ++ vel.asString ++ ")";
		keyMenu.items = ["-Item:"] ++
			if (receiver.respondsTo(\midiResponderKeys)) {
				receiver.midiResponderKeys;
			}{ nil };
	}

	*makeAction {
		var func;
		func = funcDisplay.string.compile.value;
		^{ | src, chan, num, vel |
			func.value(receiver, src, chan, num, vel)
		};
	}

	/* as soon as MIDI was received, remove the responders.
		add them back upon request for another binding. */
	*stopListening {
		 if (window.notNil) {
			activationButton.value = 0;
		};
		this.changed([\isLearning, false]);
		this.removeListeningResponders;
	}

	*removeListeningResponders {
		[noteon, noteoff, noteonoff, cc, /* aftertouch, */ pitchbend] do: _.remove;
	}

// ======================= GUI STUFF ======================
// TODO: Clean up and rework Gui code, possibly move to other class or to a Script (!)
	*makeGui {
		if (window.notNil) { window.front; ^this; };
		this.init;
		this.makeWindow;
	}
	*closeGui { if (window.notNil) { window.close } }
	*makeWindow {
		var button;
		window = SCWindow("MIDI Listener", Rect(0, 420, 500, 235).fromTop);
		window.view.decorator = FlowLayout(window.view.bounds, 3@3, 1@1);
		this.changed(\windowOpened); 
		window.onClose = {
			this.changed(\windowClosed); 
			this.closed;
		};
		SCStaticText(window, Rect(0, 0, 100, 20)).string = "Receiving object:";
		receiverDisplay = SCDragSink(window, Rect(0, 0, 290, 20)).string = "Drag object here";
//		receiverDisplay.object = 13245;
		SCStaticText(window, Rect(0, 0, 70, 20)).string = "Learn mode:";
		activationButton = SCButton(window, Rect(0, 0, 30, 20)).states_([
			["OFF", Color.blue, Color.white], ["ON", Color.red, Color.white]
		]).font_(Font("Helvetica-Bold", 12))
		.action_ { | me |
			if (me.value > 0) { this.startListening } { this.stopListening }
		};
		window.view.decorator.nextLine;
		SCStaticText(window, Rect(0, 0, 30, 20)).string = "Port:";
		srcDisplay = SCStaticText(window, Rect(0, 0, 280, 20));
		srcDisplay.background = Color(0.7, 0.9, 1.0, 0.7);
		SCStaticText(window, Rect(0, 0, 50, 20)).string = " Type:";
		typeDisplay = SCStaticText(window, Rect(0, 0, 124, 20));
		typeDisplay.background = Color(0.5, 0.9, 0.9);
		window.view.decorator.nextLine;
		button = SCButton(window, Rect(0, 0, 55, 20)).states = [["Channel:"]];
		chanDisplay = SCTextView(window, Rect(0, 0, 80, 20)).keyDownAction_(this.enterKeyAction);
		button.action = { chanDisplay.setString("", 0, 1000).string_("nil"); };
		button = SCButton(window, Rect(0, 0, 55, 20)).states = [["Number:"]];
		numDisplay = SCTextView(window, Rect(0, 0, 80, 20)).keyDownAction_(this.enterKeyAction);
		button.action = { numDisplay.setString("", 0, 1000).string_("nil"); };
		button = SCButton(window, Rect(0, 0, 50, 20)).states = [["Value:"]];
		velDisplay = SCTextView(window, Rect(0, 0, 80, 20)).keyDownAction_(this.enterKeyAction);
		button.action = { velDisplay.setString("", 0, 1000).string_("nil"); };
		velAltDisplay = SCStaticText(window, Rect(0, 0, 80, 20))
			.stringColor_(Color.red);
		velAltDisplay.background = Color.grey(0.8);
		velAltDisplay.font = Font("Helvetica-Bold", 12);
		SCStaticText(window, Rect(0, 0, 98, 20))
			.background_(Color.black).stringColor_(Color.grey(0.8))
			.string = "Edit action below:";
		keyMenu = SCPopUpMenu(window, Rect(0, 0, 50, 20))
			.action_ { |me|
				this.displayResponderFromKey(me.items[me.value]);
			};
		keyDisplay = SCTextView(window, Rect(0, 0, 150, 20)).string_("nil")
			.action_ { |me| me.string.postln; };
		resendButton = SCButton(window, Rect(0, 0, 70, 20)).states_([["Resend"]]).action_({ this.resend });
		SCStaticText(window, Rect(0, 0, 70, 20)).string = "Post input:";
		verboseButton = SCButton(window, Rect(0, 0, 30, 20))
			.states_([["OFF", Color.blue, Color.white], ["ON", Color.red, Color.white]])
			.action_ { |me| verbose = me.value > 0 };
		verboseButton.value = verbose.binaryValue;
		funcDisplay = SCTextView(window, Rect(0, 0, 496, 127))
			.hasVerticalScroller_(true).hasHorizontalScroller_(true).autohidesScrollers_(true)
			.string_(this.defaultAction)
			.keyDownAction_(this.enterKeyAction);
		receiverDisplay.canReceiveDragHandler = {
			SCView.currentDrag.respondsTo(\addMIDIResponder)
		};
		receiverDisplay.action = { | me |
//			[this, me, me.object.asArray].postln;
			this.addReceiver(*me.object.asArray);
		};
		SCStaticText(window, Rect(0, 0, 70, 20)).string = "Note On/Off Pairs:";
		noteOnOffPairsButton = SCButton(window, Rect(0, 0, 30, 20))
			.states_([["no", Color.blue, Color.white], ["yes", Color.red, Color.white]])
			.action_ { |me| this.noteOnOffPairs = me.value > 0 }
			.font_(Font("Helvetica-Bold", 12));
		this.noteOnOffPairs = noteOnOffPairs; // update display with current state
		SCStaticText(window, Rect(0, 0, 100, 20)).string = "Wait before next:";
		waitBeforeNextView = SCNumberBox(window, Rect(0, 0, 30, 20))
			.action_({|me|waitBeforeNext = me.value})
			.value_(waitBeforeNext)
			.keyDownAction_{ | me, char, mod, uni, key |
				switch (uni, // increment/decrement with cursor keys <-, ->, pageup, pagedown
					63232, { this.waitBeforeNext = me.value + 0.1 },
					63233, { this.waitBeforeNext = me.value - 0.1 },
					63234, { this.waitBeforeNext = me.value - 1 },
					63235, { this.waitBeforeNext = me.value + 1 },
					{ me.defaultKeyDownAction(char, mod, uni, key)}
				);
			};
		SCButton(window, Rect(0, 0, 150, 20))
			.states_([["Rescan MIDI ports !!!!"]])
			// force MIDIClient init to connect to any new/changed devices
			.action_ { this.initMIDI(false)};
		responderDragSource = SCDragSource(window, Rect(0, 0, 107, 22));
		window.front;
//		this.reset;
		this.resetResponderDisplays;
	}

	*waitBeforeNext_ { | time = 0.5 |
		waitBeforeNext = time max: 0;
		waitBeforeNextView.value = waitBeforeNext;
	}
	*resend {
		this.setResponder(
			if (responder.isNil) { NoteOnResponder } { responder.class },
			nil, // srcDisplay.string.compile.value,
		 	chanDisplay.string.compile.value,
		 	numDisplay.string.compile.value,
		 	velDisplay.string.compile.value
		 );
	}

/////////////////////////////////////////////////////////////////////////////
	// under construction

	*displayResponderFromKey { | key |
		key = key.asSymbol;
		if (receiver.respondsTo(\midiResponder)) {
			receiver.midiResponder[key].postln;
		}
	}
	*enterKeyAction {
		// always immediately save function source code in receiver item
		^{ |view, char, modifiers, unicode, keycode|
			view.defaultKeyDownAction(char,modifiers,unicode,keycode);
			if (receiver.notNil, { receiver.funcSourceCode = view.string });
//			if (char == 3.asAscii) { this.resend; };
		}
	}

	*defaultAction {
		^"{ | myself, src, chan, num, vel|\n\t[myself, src, chan, num, vel].postln;\n}";
	}

	*closed {
		window = nil;
		this.stopListening;
	}

	*closeWindow { if (window.notNil) {window.close} }

	*noteOnOffPairs_ { | onP = true |
		noteOnOffPairs = onP;
		noteOnOffPairsButton.value = onP.binaryValue;
		this.removeListeningResponders;
		this.addListeningResponders;
		this.changed([\noteOnOffPairs, onP]);
	}
}

