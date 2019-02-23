/* IZ 050713
A Bus that maintains lists of the scripts whose processes write to and read from it. 
Additionally, it has methods for creating BusLinks, that copy from one LinkedBus to another when many-to-many connections between links require this. 
(And other stuff...)
*/

LinkedBus : Bus {
	classvar linkClasses;
	var <writers; // array of parameters of scripts writing to this bus
	var <readers; // array of parameters of scripts reading from this bus

	*initClass {
		linkClasses = (audio: ArBusLink, control: KrBusLink)
	}

	*new { arg rate = \audio, numChannels = 1, server;
		^this.newCopyArgs(rate, nil, numChannels,
			server ?? { Server.default }).init;
	}
	init {
	// reallocate yourself when server boots, and NOW if server running
		writers = []; readers = [];	// containsCycle : readers.includes ok
		this.onBoot({ this.alloc }, server, true);
	}
	alloc {
//		[this, "allocating for server:", server, "server state:", server.serverRunning].postln;
//		if (server.serverRunning) { // prevent double allocs on startup (server bug?)
			super.alloc;
			writers do: _.resetOutputBusIndex(index);
			readers do: _.resetInputBusIndex(index);
//		}
	}
	free {
		if (index.notNil) { super.free }; // prevent double free
		// stop reallocating yourself. This bus no longer used:
		this.removeServer(server);
	}
	freeIfUnlinked {
		if (writers.size == 0 and: { readers.size == 0}) {
			this.free;
		}
	}
	isRunning {
	// answer if one of my writers is running. For BusLink:init
//		writers.collect({|w| [w, w.isRunning]}).postln;
		^writers.detect(_.isRunning).notNil;
	}
	getWriterParameters { ^writers collect: _.asWriterParameter }
	getReaderParameters { ^readers collect: _.asReaderParameter }
	includesReader { | reader |
		^(readers.size > 0) and: { readers.detect({|r| r === reader }).notNil }
	}
	containsCycle { | writer |
		if (readers.includes(writer)) {
			^true
		}{
			^readers.detect({ | r |
				r.output.notNil and: { r.output.containsCycle(writer) }
			}).notNil
		}
	}
	addReader { | reader |
		readers = readers.add(reader);
		reader.input = this;	// this also maps/sets the reader
	}
	addWriter { | writer, param = \out |
		writers = writers.add(writer);
		writer.output = this;	// this also sets the writer
	}
	removeReader { | reader |
		// called by OutputParameter:removeReader and BusLink:unLink
		readers.remove(reader);
		if (readers.size == 0) { this.free };
	}
	removeWriter { | writer |
		// called by OutputParameter:removeReader and BusLink:unLink
		writers.remove(writer);
		if (writers.size == 0) { this.free };
	}
	moveReader2BusLink { | writer, reader, target |
	// used when adding a writer to a bus that already has other readers
	// see Parameter:getInputBusFor
	/* Remove reader from my readers, add it to a new LinkedBus reading from
	writer, and copy my signal to the new bus with a BusLink. */
//		this.removeReader(reader);
		readers.detect(_.hasReader(reader)).removeFromReaders(readers, reader);
		^linkClasses[rate].new(
			this,
			LinkedBus(rate, nil, numChannels, server, writer, reader),
			writer.target
		)
	}
	moveReadersAfter { | argGroupIndex, argTarget |
		// argTarget is needed for BusLink
		readers do: _.moveScriptAfter(argGroupIndex, argTarget)
	}
/*	writerReaderAdded { | writer, reader |
		writers = writers.add(writer);
		readers = readers.add(reader);
	}
*/
	writerStarted {
	// if any of my writers start, notify all my readers
	// this notifies BusLinks to start for copying my output signal
		readers do: _.writerStarted;
	}
	writerStopped {
		if (this.isRunning.not) { readers do: _.writerStopped }
	}
}

