/* (IZ 2007-01-18) { 
Reserves one output bus and one input bus on each server used by any session with links, so that unliked processes can set their inputs/outputs to this bus. This means that these processes can keep running without being heard and without running danger of writing accidentally to some used bus.

Note that the input bus must be different from the output bus. The synths must be really disconnected.

It is a LinkedBus with one big difference: It stores its readers and writers and it updates them on server reboot, but it does not store itself in their inputs or outputs. This is so that they behave as if they have no LinkedBusses at their input or output - which is in fact the case. This is a "phantom" bus! 

} */

MuteBus : LinkedBus {
	classvar <inputs, <outputs;
	*initClass { inputs = (); outputs = () }
	*getBus { | connection, server |
		var bus;
		bus = connection[server];
		if (bus.isNil) {
			bus = this.makeBus(server);
			connection[server] = bus;
		};
		^bus;
	}
	*makeBus { | server | ^this.new(\audio, 1, server) }
	// make reader read from a mute (silent) input
	*doIfBusPresent { | connection, server, action |
		var bus;
		bus = connection[server];
		if (bus.notNil) { action.(bus, server) };
	}
	*muteInput { | reader |
		this.getBus(inputs, reader.server).addReader(reader);
	}
	addReader { | reader |
		readers = readers.add(reader);
		// Don't add self to reader, just set to bus index!
		if (index.notNil) { reader.resetInputBusIndex(index) }
	}
	*unmuteInput { | reader |
		this.doIfBusPresent(inputs, reader.server, { | bus |
			bus.readers.remove(reader);
		});
	}

	// make writer write to a mute (silent) input
	*muteOutput { | writer, server |
		this.getBus(outputs, server).addWriter(writer);
	}
	addWriter { | writer |
		writers = writers.add(writer);
		// Don't add self to writer, just set to bus index!
		if (index.notNil) { writer.resetOutputBusIndex(index) }
	}
	*unmuteOutput { | writer, server |
		this.doIfBusPresent(outputs, server, { | bus |
			bus.writers.remove(writer);
		});
	}
}

KrMuteBus : MuteBus {
	classvar <krOutputs;
	*initClass { krOutputs = () }
	*makeBus { | server | ^this.new(\control, 1, server) }
	*muteInput {} // taken care of elsewhere. Don't use bus: unmap
	*muteOutput { | writer, server |
		this.getBus(krOutputs, server).addWriter(writer);
	}
	*unmuteOutput { | writer, server |
		this.doIfBusPresent(krOutputs, server, { | bus |
			bus.writers.remove(writer);
		});
	}
}

