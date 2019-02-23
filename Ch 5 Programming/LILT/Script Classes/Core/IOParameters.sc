/* (IZ 2006-08-13) {
Parameters with outputs
ArOutput ->  	Kr input, ar output			red
KrOutput ->  	Kr input, kr output			green
} */

ArInput : Parameter {
	inputRate { ^\audio }
	busLinkClass { ^ArBusLink }
	acceptableWriterClass { ^ArOutput }
	labelBackground { ^Color.red.alpha_(0.1) }
	setScriptAttributes {	// set flag for script label color
		script.hasAudioInput = true;
	}
	resetInputBusIndex { | argIndex |
		this.set(argIndex) }
	muteInput {
		MuteBus.muteInput(this, script.session.server);
	}
	unmuteInput {
		MuteBus.unmuteInput(this, script.session.server);
	}
	setPreset { // disabled for the moment - ArInputs dont change 
		// their numeric value directly but should be implemented as 
		// re-linking to a different script (hairy ...)
	}
	add2MIDIList {
		// called by Script:defaultMIDIobjects
		// add self to the array of objects controllable by MIDI for your Script.
		// IO Parameters skip this because they are not controllable by MIDI
	}
}

OutputParameter : Parameter {
	var <output; 	// a LinkedBus with links to all my readers
	init { | argAction |
		super.init(argAction);
		script.output = this;
		this.setScriptAttributes; // for script label color
	}
	// ================ LINKING ==============
	getReaderParameters {
		if (output.isNil) { ^[] } { ^output.getReaderParameters.flat }
	}
	doesNotIncludeReader { | reader |
		// called by Parameter-canLinkTo to prevent double links
		^output.isNil or: {
			output.readers.detect(_.includesReader(reader)).isNil
		}
	}
	containsCycle { | argScript |
		^output.notNil and:
		{ output.readers.detect(_.containsCycle(argScript)).notNil }
	}
	moveReadersAfter { | argGroupIndex, argTarget |
		// argTarget is needed for BusLink
		if (output.notNil) { output.moveReadersAfter(argGroupIndex, argTarget) }
	}
	addReader { | reader |
	// Top method for adding links. See removeReader for the reverse. 
	/* make a Parameter reader read data from me, i.e. link it to my output.
	usually as result of user dragging output of script to a parameter.
	(1) the readers script's nodes must move after this script's nodes.
	(2) the output bus "ob" of this parameter must be (created and) connected 
	to the input bus "ib" of the reader parameter. "ob" and "ib" may be identical 
	- in which case no linking is needed. */
		var wBus, rBus; // writer's and reader's LinkedBus instances
		// move reader's script's target group after your script's target group:
		reader.moveScriptAfter(script.groupIndex);
		// create "private" busses if required: 
		wBus = this.getOutput4NewReader(reader);
		rBus = reader.getInput4NewWriter(this);
		wBus = wBus ? rBus ?? { // provide bus instances where absent
			rBus = LinkedBus(this.outputRate, 1, script.session.server);
		};
		// update reader / writer and input / output variables if needed:
		if (output != wBus) { wBus.addWriter(this) };
		if (reader.input != (rBus = rBus ? wBus)) { rBus.addReader(reader) };
		// make link copying signal from writers bus to readers bus if needed:
		if (wBus != rBus) {
			this.busLinkClass.new(wBus, rBus, script.envir.target);
		};
		// notify scripts of links being added:
		script.changed(\readers);
		reader.script.changed(\writers);
	}
	getOutput4NewReader { | reader |
		// if my output has other writers also, then create new output for 
		// linking to reader and add bus-link from new output to old output
		var newOutput;
		if (output.isNil) { ^nil }; // output will be provided later
		if (output.writers.size > 1) {
			newOutput = LinkedBus(this.outputRate, 1, script.session.server);
			this.busLinkClass.new(newOutput, output, script.envir.target);
			output.removeWriter(this);
			^newOutput;
		};
		^output;
	}
	moveToPrivateBus {
		var newOutput;
		newOutput = LinkedBus(this.outputRate, 1, script.session.server);
		this.busLinkClass.new(newOutput, output, script.envir.target);
		output.removeWriter(this);
		this.output = newOutput;
		^newOutput;
	}
	output_ { | argOutput |
		if (argOutput.isNil) {
			this.muteOutput;
		}{
			this.unmuteOutput;
			// catch nil index when server has not started yet. Provide 0
			output = argOutput;
			this.resetOutputBusIndex(output.index ? 0);
		}
	}
	resetOutputBusIndex { | argIndex | script.set(name, argIndex) }
	muteOutput {
		output.writers.remove(this);
		output.freeIfUnlinked;
		this.muteBusClass.muteOutput(this, script.session.server);
		output = nil;
	}
	unmuteOutput {	// removes self from writers of MuteBus instance
		this.muteBusClass.unmuteOutput(this, script.session.server);
	}
	// !!!!!!! Top method for removing links: !!!!!!!
	removeReader { | argReader | // argReader is a Parameter
	// Top of algorithm tree for removing / reallocating busses at removal
/* { Algorithm:
1	Find the last node in the path from the writer to the reader which still has only one writer in its input
Meaning specifically: Traverse the graph of interconnections starting from the writer and going towards the reader, and stopping at either one of the following conditions:
•	1.1	the node which was reached has more than 1 writers.
•	1.2	the node which was reached is identical to the reader.
2	Let n be the node found as a result of the above traversal and n-1 be the node previous to n in the path of the traversal. Then: 
2.1	Remove n-1 from the inputs of n.
•	2.1.1	If n is an input parameter, then set the input of n to nil (which mutes the input of n)
2.2	Remove n from the outputs of n-1.
•	2.2.1	If n-1 is an output parameter, then set the outpout of n-1 to nil (which mutes the output of n-1)
3	Remove redundant output paths from the output of the writer.
This consists of:
3.1	 If the output bus o of the writer w is not nil, then
3.1.1	If o has no readers,
•	3.1.1.1	Set the output of w to nil (which mutes the output of w)
•	3.1.1.2	Free o.
3.1.2	Else if o has only one reader, and this reader is a BusLink l (bus interconnecting synth) then, let o2 be the output bus of l, and:
•	3.1.2.1	set the output of w to o2
•	3.1.2.2	add w to the writers of o2
•	3.1.2.3	stop l (if it is running)
4	Remove redundant input paths from the input of the reader.
This consists of:
4.1	 If the input bus i of the reader r is not nil, then
4.1.1	If i has no writers,
•	4.1.1.1	Set input of r to nil (which mutes the input of r)
•	4.1.1.2	Free i.
4.1.2	Else if i has only one writer, and this reader is a BusLink l (bus interconnecting synth) then, let i2 be the input bus of l, and:
•	4.1.2.1	set the input of r to i2
•	4.1.2.2	add r to the readers of i2
•	4.1.2.3	stop l (if it is running)
} */
		var lastWriter, lastReader;
		#lastWriter, lastReader = this.findLastSingleWriterReader(argReader);
		if (lastReader === argReader) {
			lastWriter.removeReader(argReader); // is always a LinkedBus
			lastReader.input = nil;				// mute input
		}{
			if (lastWriter === this){
				lastWriter.output = nil;
				lastReader.removeWriter(lastWriter);
			}{	// disconnect BusLink from its reader LinkedBus
				lastWriter.unLink;
				lastReader.removeWriter(lastWriter);
			}
		};
		this.removeRedundantOutputPaths;
		argReader.removeRedundantInputPaths;
		script.changed(\readers);
		argReader.script.changed(\writers);
	}
	findLastSingleWriterReader { | argReader |
		var found;
		if (output.writers.size > 1) { ^[this, output] };
		if (output.readers.includes(argReader)) { ^[output, argReader] };
		found = output.readers detect: { | r |
			r.isKindOf(BusLink) and: { r.output.readers includes: argReader }
		};
		^[found, found.output]
	}
	removeRedundantOutputPaths {
		var link;
		if (output.isNil) { ^this };
		if (output.readers.size == 0) { ^this.output = nil };
		if (output.readers.size == 1 and: {
			(link = output.readers.first) isKindOf: BusLink
		}) {
			this.output = link.output;
			link.unLink;
		};
	}
	muteBusClass { ^KrMuteBus }
	writerStarted {
	// Received from my script when it starts. Notify all my readers.
	// this notifies BusLinks to start for copying my output signal
		if (output.notNil) { output.writerStarted };
	}
	writerStopped {
		if (output.notNil) { output.writerStopped }
	}
	setPreset { // disabled for the moment - outputParameters dont change 
		// their numeric value directly but should be implemented as 
		// re-linking to a different script (hairy ...)
	}
	// ================ DISPLAY ================
	stringColor { ^if (output.isNil) { Color.black } { Color.red } }
	makeOutputDragView { | window, adapter |
	// make drag view for linking output to inputs of other scripts
		var outputView;
		outputView = SCDragSource(window, Rect(0, 0, 20, 20))
			.string_("o")
			.align_(\center)
			.canFocus_(true)
			.background_(this.outBoxBackground)
			.stringColor_(if (output.isNil) { Color.black } { Color.red })
			.setBoth_(false)
			.object_(this)
			.canReceiveDragHandler_(false);
		adapter.outputLinkAdded = { outputView.stringColor_(Color.red) };
		adapter.outputLinkRemoved = { outputView.stringColor_(Color.black) };
	}
	// ================ SAVING LINKS IN SESSIONS ===================
	saveLinks { | file |
		this.getReaderParameters do: { | r |
			file putString: "\t";
			file putString: [script.scriptGroup.name, script.name, name,
				r.script.scriptGroup.name, r.script.name, r.name
			].asCompileString;
			file putString: ",\n"
		}
	}
	// ================ MIDI ===================
	add2MIDIList {
		// called by Script:defaultMIDIobjects
		// add self to the array of objects controllable by MIDI for your Script.
		// IO Parameters skip this because they are not controllable by MIDI
	}
}

ArOutput : OutputParameter {
	setScriptAttributes {	// set flag for script label color
		script.hasAudioOutput = true;
	}
	outputRate { ^\audio; }
	busLinkClass { ^ArBusLink }
	outBoxBackground { ^Color.red.alpha_(0.1); }
	labelBackground { ^Color.red.alpha_(0.1) }
	muteBusClass { ^MuteBus }
}

KrOutput : OutputParameter {
	setScriptAttributes {	// set flag for script label color
		script.hasControlOutput = true;
	}
	outputRate { ^\control; }
	outBoxBackground { ^Color.black.alpha_(0.1) }
	labelBackground { ^Color.green.alpha_(0.1) }
//	resetOutputBusIndex { | argIndex | script.map(name, argIndex) }
}

