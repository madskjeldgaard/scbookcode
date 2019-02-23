/* (IZ 2006-08-12) {
Copies the signal from one bus to another to serve as link in connecting paremeters of scripts. Required in n-to-n parameter link configurations where not all readers are reading from all writers. See doc ...

Uses synthdefs: ar_bus_copy, kr_bus_copy
} */

BusLink {
	var <input;		// instance of LinkedBus from Parameters that give me my input
	var <output;	// instance of LinkedBus to Parameers to which I write my output
	var <target;			// the group where the synth should be created
	var <synth;				// the synth that copies the signal

	*new { | input, output, target |
//		Post << this << input << output << target << "\n";
		^this.newCopyArgs(input, output, target).init;
	}
	init {
//		[this, input, output].postln;
		input.addReader(this);
		output.addWriter(this);
		if (input.isRunning) { this.writerStarted };
	}
	writerStarted {
		// if any of my writers start, i should start copying its signal 
		// and notify my readers to propagate it as needed
		output.writerStarted;
		this.start;
	}
	writerStopped {
		// my writing LinkedBus has no more active readers. Therefore stop
		this.stop; // do not notify readers: Scripts dont check for running inputs
	}
	resetInputBusIndex { | index |
		if (this.isRunning) { synth.set(\in, index) };
	}
	resetOutputBusIndex { | index |
		if (this.isRunning) { synth.set(\out, index) };
	}
	asReaderParameter { ^output.getReaderParameters }
	asWriterParameter { ^input.getWriterParameters }
	// since we always initialize input output at start, these are useless
	// but must be included for compatibility with LinkedBus:addReader/addWriter:
	input_ { | argInput |
		// does not update target. can it be used?
//		input = argInput;
//		if (synth.notNil) { input.set(\in, input.index) };
	}
	output_ { | argOutput |
/*		output = argOutput;
		if (synth.notNil) { output.set(\out, output.index) };
*/	}
	isRunning { ^synth.notNil }
	includesReader { | reader |
		/* part of check if a link already exists. see: Parameter:canLinkTo */
		^output.includesReader(reader);
//		^output.reader === reader;
	}
	containsCycles { | wrScript |
		^output.reader.containsCycles(wrScript)
	}
	start {
		var server = target.asTarget.server;
		synth = Synth(this.synthDefName, [\in, input.index, \out, output.index],
			target.asTarget, \addToTail);
		synth.onEnd({
			synth = nil;
			synth.removeServer(server);
//			[this, "I am informing my output that I stopped. My output is:",
//			 output].postln;
			output.writerStopped;
		}, server);
	}
	stop { // the synth sets synth to nil via its onEnd action (see start method)
		if (this.isRunning) { synth.free; };
	}
	moveScriptAfter { | argGroupIndex, argTarget |
	// I use the *same* target as given, but place my synth *at the tail* of the target
		output.moveReadersAfter(argGroupIndex);
		this.target = argTarget;
	}
	target_ { | argTarget |
		if (not(target === argTarget)) {
			target = argTarget;
			if (this.isRunning) { synth.moveToTail(target.asTarget) }
		}
	}
	// following 2 are for detecting and removing BusLinks in LinkedBus:removeReader
	hasWriter { | argWriter |
		thisMethod.report(argWriter, input.readers);
		^input.readers includes: argWriter;
	}
	unLink {
		// called by OutputParameter:removeReader
		output.removeWriter(this);
		input.removeReader(this);
		this.stop;
	}
/*
	removeReader { | reader |
		reader.inputs
		this.stop;
	}
*/

}

ArBusLink : BusLink {
	synthDefName { ^\ar_bus_copy }
}

KrBusLink : BusLink {
	synthDefName { ^\kr_bus_copy }
}

