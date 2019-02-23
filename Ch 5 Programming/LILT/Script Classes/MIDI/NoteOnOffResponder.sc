/* (IZ 2005-08-24) {
Responds to both Note on and Note of MIDI messages on the same key and using the same action function. The function should usually check the velocity ("c") value and switch a process off when this is 0, otherwise scale some other parameter by the velocity. 

This responder is used especially by MIDIListener for automatic assignment of actions to a process on one pressing of a key that will send both note-on and note-off messages. 

install: Make responders active
remove: Make responders inactive
} */

NoteOnOffResponder {
	// TODO: responder instances should be protected from writing for install safety?
	var <>noteOnResponder, <>noteOffResponder;
	*new { | functions, src, chan, num, veloc, install = true |
		var onFunc, offFunc;
		// optionally provide separate offFunc if functions is array:
		#onFunc, offFunc = functions.asArray;
		^super.newCopyArgs(NoteOnResponder(onFunc, src, chan, num, veloc, install),
			NoteOffResponder(offFunc?onFunc, src, chan, num, veloc, install)
		);
	}
	install {
		noteOnResponder.install; noteOffResponder.install;
	}
	remove {
		noteOnResponder.remove; noteOffResponder.remove;
	}
	function {
		if (noteOnResponder.function === noteOnResponder.function) {
			^noteOnResponder.function
		} {
			^[noteOnResponder.function, noteOffResponder.function]
		}
	}
	function_ { | onFunc, offFunc |
		// optionally provide different functions for note on and on off
		this.noteOnFunction = onFunc;
		this.noteOffFunction = offFunc ? onFunc;
	}
	noteOnFunction_ { | func |
		noteOnResponder.function = func;
	}
	noteOffFunction_ { | func |
		noteOffResponder.function = func;
	}
	matchEvent { ^noteOnResponder.matchEvent }
	matchEvent2 { ^noteOffResponder.matchEvent }
//	getSaveData {
//		^(class: this.class.name, matchEvent: noteOnResponder.matchEvent.getSaveData);
//	}
	matchEvent_ { | matchEvent |
		// set matchEvent for both responders. Used by MIDIResponder-fromSaveData
		noteOnResponder.matchEvent = matchEvent;
		noteOffResponder.matchEvent = matchEvent;
	}
	copy {
		// when copying, make copies of sub-responders to ensure independence
		// from original. 
		^super.copy
			.noteOnResponder_(noteOnResponder.copy)
			.noteOffResponder_(noteOffResponder.copy)
	}
	isKindOf { | what |
		// following may be obsolete ... 070202 ???? check!
		// lie about your class: Say that you are a kind of MIDIResponder
		// this is used by Parameter for MIDI bindings in its canReceiveDrag method
		^what === MIDIResponder;
	}
	asScriptString {
		^String.streamContentsLimit({ | stream |
			stream << this.class.name
				<< "("
				<<* [nil, nil, this.noteOnResponder.matchEvent.chan,
					this.noteOnResponder.matchEvent.b,
					nil, false]
				<<")"
		})
	}
	displayString {
		var m;
		m = noteOnResponder.matchEvent;
		^format("%: %-%-%-%", this.class.name, m.port, m.chan, m.b, m.c);
	}
	printOn { | stream |
		this.storeOn(stream);
	}
	storeArgs {
		^[noteOnResponder.matchEvent.port, noteOnResponder.matchEvent.chan,
		noteOnResponder.matchEvent.b, noteOnResponder.matchEvent.c]
	}

}

