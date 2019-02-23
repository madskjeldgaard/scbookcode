
// extensions for playing things onto mixers

+ Symbol {
		// maybe refactor this later
	playInMixerGroup { |mixer, target, patchType, args|
		var result;
		(args.notNil and: { args.includes(\outbus) }).not.if({ args = args
			++ [\outbus, mixer.inbus.index, \out, mixer.inbus.index,
				\i_out, mixer.inbus.index] });
		mixer.queueBundle({ result = Synth.tail(target, this, args) });
		^result
	}

	playOnGlobalControl { |gc, args, target, addAction = \addToTail|
		(args.notNil and: { args.includes(\outbus) }).not.if({ args = args 
			++ [\outbus, gc.bus.index, \out, gc.bus.index, \i_out, gc.bus.index] });
		^Synth(this, args, (target ?? { gc.server }).asTarget, addAction);
	}
}

+ String {
	playInMixerGroup { |mixer, target, patchType, args|
		^this.asSymbol.playInMixerGroup(mixer, target, patchType, args)
	}

	playOnGlobalControl { |gc, args, target, addAction = \addToTail|
		^this.asSymbol.playOnGlobalControl(gc, args, target, addAction)
	}
}

+ SynthDef {
	playInMixerGroup { |mixer, target, patchType, args|
		var result;
		(args.notNil and: { args.includes(\outbus) }).not.if({ args = args
			++ [\outbus, mixer.inbus.index, \out, mixer.inbus.index,
				\i_out, mixer.inbus.index] });
		mixer.queueBundle({ result = this.play(target, args, \addToTail) });
		^result
	}
	
	playOnGlobalControl { |gc, args, target, addAction = \addToTail|
		(args.notNil and: { args.includes(\outbus) }).not.if({
			args = args ++ [\outbus, gc.index, \out, gc.index, \i_out, gc.index]
		});
		^this.play(target, args, \addToTail)
	}
}

+ Instr {
	playInMixerGroup { |mixer, target, patchType, args|
		var	newPatch;
		mixer.addPatch(newPatch = patchType.new(this, args,
			outClass: if(target === mixer.effectgroup) { ReplaceOut } { Out }));
		^newPatch.playToMixer(mixer)
	}
	
	playOnGlobalControl { |gc, args, target|
		^Patch(this, args).playOnGlobalControl(gc, target)
	}
}

+ AbstractPlayer {
	playInMixerGroup { |mixer, target, patchType, args|
		mixer.addPatch(this);
		this.playToMixer(mixer)
	}
	playOnGlobalControl { |gc, target|
			// if I don't wrap gc's bus in a SharedBus,
			// freeing the patch will free the bus also... that's bad
		this.play((target ?? { gc.server }).asTarget, nil, SharedBus.newFrom(gc.bus, gc))
	}
}

+ Function {
	playInMixerGroup { |mixer, target, patchType, args|
		var result, def, updateFunc;
		mixer.queueBundle({
			def = this.asSynthDef(
				outClass: (target == mixer.effectgroup).if({ \ReplaceOut }, { \Out }));
			result = def.play(target, args ++ [\i_out, mixer.inbus.index, \out, mixer.inbus.index,
					\outbus, mixer.inbus.index], \addToTail);
			updateFunc = { |node, msg|
				if(msg == \n_end) {
					node.removeDependant(updateFunc);
					target.server.sendMsg(\d_free, def.name);
				};
			};
			NodeWatcher.register(result);
			result.addDependant(updateFunc);
		});
		^result
	}
	
	playOnGlobalControl { |gc, args, target, addAction = \addToTail|
		^this.asSynthDef.play((target ?? { gc.server }).asTarget,
			args ++ [\i_out, gc.bus.index, \out, gc.bus.index, \outbus, gc.bus.index],
			addAction);
	}
}

+ Pattern {
	playInMixerGroup { |mixer, target, patchType, args|
		var	protoEvent, quant;
		args ?? { args = () };
		protoEvent = args[\protoEvent] ?? { Event.default.copy };
		protoEvent.proto ?? { protoEvent.proto = () };
		protoEvent.proto.putAll((
			chan: mixer,
			server: mixer.server,
			group: target.tryPerform(\nodeID) ?? { target },
			bus: mixer.inbus,
			outbus: mixer.inbus.index,
			out: mixer.inbus.index,
			i_out: mixer.inbus.index
		));
		^this.play(args[\clock], protoEvent, args[\quant])
	}
}

+ Event {
	playInMixerGroup { |mixer, target, patchType, args|
		this.proto ?? { this.proto = () };
		this.proto.putAll((
			chan: mixer,
			server: mixer.server,
			group: target.tryPerform(\nodeID) ?? { target },
			bus: mixer.inbus,
			outbus: mixer.inbus.index,
			out: mixer.inbus.index,
			i_out: mixer.inbus.index
		));
		^this.play;
	}
}


// needed for type tests

+ Object {
	isMixerChannel { ^false }
}

// other misc extensions

+ Bus {
	asMixer {		// returns the mixer if "this" corresponds to a MixerChannel inbus,
				// nil otherwise
		^MixerChannel.servers.at(server).tryPerform(\at, index);
	}
}

+ Nil {
	asMixer {}
}

// felix won't like this b/c it "should" be private; but w/ busdict, shared buses have to
// be double-freed which breaks the bus allocator, so this is necessary for MixerChannel
+ SharedBus {
	released_ { arg bool; released = bool }
}
