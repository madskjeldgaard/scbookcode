/* (IZ 2005-09-08) {
Encapsulates name, spec, action MIDIResponder, and source code for MIDIResponder function for a parameter of a script. Parameters of are created by initScript in Script. 
They are used to create control gui and to edit MIDIResponders for a script. 

Subclasses of Parameter (or even other classes) are used to create custom guis for different types of data. The name of the class to be used can be provided as first element of specs in method new. Types: 

Class			inputs + outputs			Label color on Gui

Parameter 		-> 	Kr input, no output			no color (gray)
BufParameter 	-> 	Kr input, no output			yellow
ArInput 		-> 	Ar input, no output			blue
[Note: no KrInput exists - use mapping to a Parameter instead]
ArOutput 		->  Kr input, ar output			red
KrOutput 		->  Kr input, kr output			green
        
When the input of a Parameter is linked to an output from another script, its label string color is red. 
                      
About MIDIResponder data: 
Source code for the MIDIResponder is needed for saving to file. Source code is built into another function code that sets the variable "script" to the script, in the shared scope of these two functions (see method: makeFunction) 

} */

Parameter : Model {
// {
	/* set by script at instance creation time: */
	var <>script;	// the script to which this parameter belongs
	var <name;		// name of parameter controlled. Is set by name_
	var <>spec;		// spec of parameter for scaling input value
	var <>action; 	// action performed when setting this parameter
	var midiAction;// If provided, overrides default MIDIResponder's action
	var <numChannels = 1;
	var <input;		// a LinkedBus with links to all my writers
// }
	*new { | script, name, spec, action, midiAction, numChannels |
		^this.newCopyArgs(nil, script, name, spec, action, midiAction, numChannels ? 1).init;
	}
	init {
		this.name = name;	// construct midi and link update messages
		// if given, action was stored in units:
		action = this.makeAction(action);
		this.reset;	// inititalize param value in scripts envir
	}
	makeAction { | argAction |
		/* function to call when parameter is sent a value from gui or elsewhere
		This method is called by Parameter-init */
		var envir;
		envir = script.envir;
		^if (argAction.notNil) {
			{ | val ... args |
				envir.use({ argAction.value(val, *args) });
				script.changed(name, val, *args);
			};
		}{
			{ | val ... args |
				envir[name] = val;
				script.setProcessParameter(name, val, *args);
				script.changed(name, val, *args);
			};
		};
	}
	setScriptAttributes {
	// set hasControlOutput, hasAudioOutput etc for script label color. 
	// This depends on a subclass type of me. See ArInput, KrOutput etc.  
	}
	*forScript { | script, specs |
		var name, myClass;
	/* { If class-name for the parameter is provided in specs[0], then create instance 
		of that named class. Otherwise try to guess the class by the parameter name  
		given in specs[0]
	} */
		#name ... specs = specs;// separate name from specs
		myClass = name.asClass;	// get the class if given
		if (myClass.isNil) {	// if no class was given, 
			myClass = switch (name,	// then guess class from parameter name: 
				\out, { ArOutput },
				\in, { ArInput },
				\bufnum, { BufParameter },
				\k_out, { KrOutput },
				{ this }	// default class is Parameter
			);
		}{					// else if class was actually given, then 
			#name ... specs = specs;	// re-separate name from specs
		}; // *specs[5..] are the action and the MIDI action functions:
		^myClass.new(script, name, ControlSpec(*specs), *specs[5..]);
	}
	name_ { | argName | name = argName.asSymbol }
	server { ^script.session.server }
	set { | ... args |
	// use my action to set parameter to value. used by ArInput. 
	// TODO: review parameter setting mechanism to *always* set parameters via 
	// this one method in Parameters, not via Script. So that there is consistency
	// with user-provided actions. 
		action.(*args);
		script.changed(name, *args);
	}
	reset { // reset scripts' parameter value to default value in spec
		script.envir use: { action.(spec.default, spec.unmap(spec.default)) };
	}
	getPreset {
	// return data that can be saved as preset for restoring
	// the current value of this paramter.
		^script.envir[name];
	}
	setPreset { | argValue |
		// restore value from data saved on preset.
		// Subclasses modify this method
		this.set(argValue);
	}
	makeGui { | gui, adapterEnvir |
		var label, slider, numbox /* , dragsink */ ;
		var midiDependant;
		label = SCDragSink(gui, Rect(0, 0, 100, 20))
			.string_(name)
			.background_(this.labelBackground)
			.stringColor_(this.stringColor)
			.canReceiveDragHandler_({ this.canLinkTo(SCView.currentDrag) })
			.receiveDragHandler_({ SCView.currentDrag.addReader(this) });
		slider = SCSlider(gui, Rect(0, 0, 140, 20));
		numbox = SCNumberBox(gui, Rect(0, 0, 50, 20));
		slider.action = { | me |
			var fullVal, normalizedVal;
			fullVal = spec.map(me.value);
			normalizedVal = me.value;
			this.set(fullVal, normalizedVal);
		};
		slider.keyDownAction = { | view, char, mod, unicode, key |
			switch (unicode,
			127, {    // backspace: gui for user to select/confirm removing an input
				this.removeInputGui;
			},
			{ view.defaultKeyDownAction(char, mod, unicode, key) }
			);
		};
		numbox.canReceiveDragHandler = true;
		numbox.action = { | me |
			var fullVal, normalizedVal;
			fullVal = spec.constrain(me.value);
			normalizedVal = spec.unmap(me.value);
			action.(fullVal, normalizedVal);
			script.changed(name, fullVal, normalizedVal);
		};
		numbox.keyDownAction = { | view, char, mod, unicode, key |
			// This re-enables typing in of values which is otherwise
 			// broken by the overall key bindings of the window.
			view.defaultKeyDownAction(char, mod, unicode);
		};
		numbox.value = script.envir[name];
		slider.value = spec.unmap(numbox.value);
		// update view values when this parameter changes value: 
		adapterEnvir[name] = { | fullVal, unmappedVal |
			{
				numbox.value = fullVal;
				slider.value = unmappedVal ?? { spec.unmap(fullVal); };
			}.defer;
		};
		midiDependant = { | what, how |
			{
				switch (how,
					\waitingForMIDIbinding, {
						slider.knobColor = Color.yellow;
					},
					\midiActive, {
						slider.knobColor = Color(0.8, 0.2, 0.2, 1);
					},
					\midiInactive, {
						slider.knobColor = Color.clear;
					}
				);
			}.defer;
		};
		this addDependant: midiDependant;
		slider.onClose = { this removeDependant: midiDependant };
		// update string color of label when links change
//		adapterEnvir[linkUpdateMsg] = { label.stringColor = this.stringColor; };
		// update slider knob color when midi responder activation state changes: 
/*		adapterEnvir[midiName] = { | isOn |
			if (isOn) {
				slider.knobColor = Color.red;
			}{
				slider.knobColor = Color.clear;
			}
		};
*/ //		this.makeDragSinkActions(dragsink, numbox, gui);
	}
	labelBackground { ^Color.clear }
	stringColor { ^if(input.isNil) { Color.black } { Color.red } }

/// ====================== LINKING ================
/*
	// obsolete as of Jan. '07 (!?)
	canReceiveDragHandler { | dragsink, numbox, gui |
		var object;
		object = SCView.currentDrag;
		^SCView.currentDrag.isKindOf(Script) and: {
			script.doesNotContainCycles(SCView.currentDrag);
		};
	}
	receiveDragHandler { | dragsink, numbox, gui |
		var object;
		object = SCView.currentDrag;
		if (object.isKindOf(MIDIResponder)) {
			^this.midiResponder_(object);
		};
		this.prReceiveDragHandler(dragsink, numbox, gui);
	}
	prReceiveDragHandler { | dragsink, numbox, gui |
		var size;
		script.addInput(dragsink.object, name, \out);
		size = script.inputs[name].writers.size;
		if (size <= 1) {
			dragsink.string = dragsink.object.name;
		}{
			dragsink.string = size.asString ++ " Scripts";
		};
	}
*/
	asReaderParameter { ^this } // BusLink returns the parameter in its output!
	asWriterParameter { ^this } // BusLink returns the parameter in its input!
	getWriterParameters {
		if (input.isNil) { ^[] } { ^input.getWriterParameters.flat }
	}
	isRunning {	// answer if my script is running. For BusLink
		^script.isRunning;
	}
	canLinkTo { | writer |
	/* Request from drag-sink or other to link a writer to my input.
	Test if writer is of right kind, is not already linked to me,  
	and would not create cycles if linked */
		^writer.isKindOf(this.acceptableWriterClass) and:
			{ writer.doesNotIncludeReader(this) } and:
			{ script.containsCycle(writer.script).not }
	}
	containsCycle { | argScript |
		^script.containsCycle(argScript);
	}
	acceptableWriterClass { ^KrOutput }
	includesReader { | reader |
	/* part of check if a link already exists. see: 
	OutputParameter-doesNotIncludeReader, BusLink-includesReader */
		^reader === this
	}
	getInput4NewWriter { | writer |
	// if my input has other readers also, then create new input for 
	// linking to writer and add bus-link from old input to new input
		var newInput;
		if (input.isNil) { ^nil }; // input will be provided later
/*		[this, "preparing to add writer:", writer].postln;
		[this, thisMethod.name, "readers:", input.readers, input.readers.size].postln;
*/		if (input.readers.size > 1) {
//			"making BusLink for readers".postln;
			newInput = LinkedBus(this.inputRate, 1, script.session.server, writer, this);
			this.busLinkClass.new(input, newInput, writer.script.envir.target);
			input.removeReader(this);
			^newInput;
		};
		^input;
	}
	busLinkClass { ^KrBusLink }
	inputRate { ^\control }
	removeAllWriters { // called by Script:close to remove all synth links
		this.getWriterParameters do: _.removeReader(this);
	}
	removeRedundantInputPaths {
		// part of the link removal algorithm. See: OutputParameter:removeReader
		var link;
		if (input.isNil) { ^this };
		if (input.writers.size == 0) { ^this.input = nil };
		if (input.writers.size == 1 and: {
			(link = input.writers.first) isKindOf: BusLink
		}) {
			this.input = link.input;
			link.unLink;
		}
	}
	input_ { | argInput |
	// set your input to a linked bus. Set or map yourself accordingly.
		input = argInput;
		if (input.isNil) {
			this.muteInput;	// ArInput overrides this method!
		}{
			this.unmuteInput;
			// catch nil index when server has not started yet. Provide 0
			this.resetInputBusIndex(input.index ? 0);
		}
	}
	muteInput { script.map(name, -1) } // ArInput adds self to MuteBus here!
	unmuteInput {} // ArInput removes itself from MuteBus here!
	resetInputBusIndex { | argIndex |
		// (Note: ArInput will use this.set instead!)
		script.map(this, argIndex)
	}
	krInputRunning {
		// mapping must only be done if I a kr synth is writing to me now, so find out:
		^this.getWriterParameters.detect({ | p | p.script.isRunning }).notNil;
	}
	moveScriptAfter { | argGroupIndex | script.moveAfter(argGroupIndex) }
	// notify my script of writers start / stop, thereby propagating notification
	// to BusLinks to start / stop copying my output signal
	writerStarted { script.writerStarted }
	writerStopped { /* only output parameters notify their output */ }
	getInputForSynth {
		// experimental: get your input bus number for a synth to connect directly to.
		// used by Script:addWriterSynth. NOT TESTED YET!
		if (input.isNil) {
				^LinkedBus(this.inputRate, 1, script.session.server).addReader(this)
		} { ^input }
	}

// =================== MIDI ==================
// midiAction will be called by MIDIhandler on request to create MIDIResponder
	midiAction {
		if (midiAction.isNil) {
			^this.defaultMIDIAction;
		}{
		// pass yourself as argument to the midi action, as "this" cannot
		// be included in code outside this instance.
			^{ | ... args | midiAction.(this, *args) }
		}
	}
	defaultMIDIAction { // subclasses may vary this
		^{ | src, chan, num, val |
			val = val / 127; // could further refine this by asking 
							// the MIDIResponder's range, if present
			this.set(spec.map(val), val);
		}
	}
	add2MIDIList { | list |
		// called by Script:defaultMIDIobjects
		// add self to the array of objects controllable by MIDI for your Script.
		// IO Parameters skip this because they are not controllable by MIDI
		list.add([this, nil])
	}
	saveMIDI { | file |
		var resp;
		if ((resp = this.getMIDIbinding).notNil) {
			file putAll: [
				"\n\t[",
				name.asCompileString, ", ", resp.asScriptString,
				"],"

/* [ */ //				"\n\t],"
			];
		}
	}
	printOn { | stream |
		super.printOn(stream);
		stream << " " << name << ":" << script.name;
	}
/*
	close {
		// sent by Script to its parameters when it closes
		// used by some parameters to release their dependency on other objects
		// TODO: I may want to add some bus i/o releasing actions to method close
	}
*/
}

