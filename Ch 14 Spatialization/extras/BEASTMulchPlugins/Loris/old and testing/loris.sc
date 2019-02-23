// Partial Players for loris partials. Created Toronto June 12/03


// Changed that each synthDef needs only a single Pan2, which can be set to a random channel offset. 
// Therefore removed the numChan from the build and search functions

// Should also have it automatically load defs if dir is not synthdefs?

// How much of this can be separated into an abstract superclass to ease maintenance? Also consider how to make it easiest to subclass these.

// Why is this using an Environment instead of an IdentityDictionary?

// Work out deltas for playRout and store them in myEnviron as an array? This should be one lookup

// Also use the Archive class to store list info. This may require additional methods for clearing, etc.

// Could add a check in free which clears this object from the currentEnvironment allowing gc

// is groupArg obsolete?

// Could make individual limiters with DetectSilence, or free?

// Do we need free?

PartialPlayer {
	classvar <myEnviron, <dir = "synthdefs/", <>latency = 0.05;
	var <server, <bus, <busIndex, <group, groupID, <clock, <>listName, <size, <numChannels;
	var playing = false, freed = false, times, <env, envDefName, decayTime;
	var <>intarget, <>inaddAction;
	
	// does require list, if server is nil then Server.local
	*new { arg name, list, numChan = 2, target = nil, addAction = \addToHead;
		^super.new.init(name, numChan, list, target, addAction);
	}
	
	*dir_ {arg path; if(path.last == $/, { dir = path; }, { dir = path ++ $/ });}
	
	*initClass {
		myEnviron = Environment.new;
	}

	init { arg name, numChan, list, target, addAction;
		var defs;
		
		numChannels = numChan.asInteger;
		server = target.asTarget.server;
		intarget = target;
		inaddAction = addAction;
		//group = groupArg ? Group.new(server);
		//groupID = group.nodeID;
		listName = name;
		//defs = (dir ++ listName.asString ++ "/" ++ numChannels.asString ++ "/*").pathMatch;
		defs = (dir ++ listName.asString ++ "-" ++ "*").pathMatch;
		//postln("defs dir:" + dir);
//		// allocate private bus
//		bus = Bus.audio(server, numChannels);
//		busIndex = bus.index;
		
		// def routes private bus to output and envelopes it
		// note that current implimentation prevents simultaneous players with
		// different numbers of channels
		// Could fix that by appending this.identityHash to the defname
		// or better yet numChannels
		envDefName = "system-PartialPlayerEnv" + numChannels;
		SynthDef(envDefName, {
			arg i_out=0, decay=0.1, attack, amp = 1.0, gate = 1, i_in;
			var input, output;
			input = In.ar(i_in, numChannels);
			output = input * EnvGen.kr(Env.asr(attack, 1.0, decay), gate, amp, 0, 1.0, 7);
			// free the nodes in the group when released
			Out.ar(i_out, output);
		}).send(server);
		
		if (myEnviron.at(listName.asSymbol).notNil, 
			{ size = myEnviron.at(listName.asSymbol).at(0);
			times = myEnviron.at(listName.asSymbol).at(1);
			},
			{ 
			if( defs.size == 0, 
//				{if (list.notNil, {this.class.build(server, name, numChannels, list); size = defs.size}, 
//					{"Init failed - No defs and list Nil".warn}) 
//				},
				{ this.class.build(name, numChannels, list, server); "Built".inform; size = list.size },
				{ 
					size = defs.size; 
					(size.asString ++ " defs found for " ++ listName.asString).inform; 
				}
			); 
			myEnviron.put(listName.asSymbol, [size, nil]);
			this.makeTimes(list);
			}
		);
	}
	
	// make a list of offsets between partials onsets
	// currently this is what requires the list arg to *.new
	
	makeTimes { arg list;
		// might be sensible to work out deltas here, or to store this info in myEnviron
		times = Array.fill(list.size, { arg i; list.at(i).at(0) });
		myEnviron.at(listName.asSymbol).put(1, times);
	}
	
	// class method that causes the necessary Synth defs to be built
	*build { arg name, numChan, list, server;
		var defDir;
		("Building Defs for " ++ name).inform;
		//defDir = dir ++ name.asString ++ "/" ++ numChan.asString ++ "/";
		//defDir.postln;

		//("mkdir -p " ++ defDir).unixCmd;

		//("cd /code/SuperCollider3/build/").unixCmd;
		//"Directory made".inform;
		list.do({ arg item, i;
			SynthDef(name.asString ++ "-" ++ i.asString, {
				arg i_out = 0, stretch = 1.0, pitch = 1.0, gate = 1, pos = 0;
				var partial;
				partial = SinOsc.ar(
					EnvGen.kr(Env.new(item.at(4), item.at(2)), gate, pitch, 0, stretch, 0),
					item.at(1),
					EnvGen.kr(Env.new(item.at(3), item.at(2)), gate, 1, 0, stretch, 2 )
				);
				// This pans each partial hard to a particular channel which is lower rent
				// than a pan. Also has the advantage that one SynthDef could be reused for 
				// multiple channels.
				
				// Changed to Pan2 (see note above); Res below not changed
				Out.ar(i_out, Pan2.ar(partial, pos))
			}).load(server, nil, dir);
			// for some reason this doesn't write to the directory argument
			//i.postln;
		});
		("Built" + list.size + "defs").inform;
	}
	
	// cause a server to load a directory of defs. Useful if you don't store them in synthdefs/
	loadDirectory { arg path;
		server.sendMsg("/d_loadDir", path);
	}
	
	play { arg out = 0, pitch = 1.0, stretch = 1.0, amp = 1.0, decay = 0.1, 
		off = 0, ioff = 0, attack = 0, releaseTime;
		var outfunc;
		if(numChannels == 2, {outfunc = 0}, {outfunc = {numChannels.rand}});
		// could have this return the clock so that multiple plays can happen
		playing.not.if({
			playing = true;
			CmdPeriod.add(this);
			// this allows overlapping plays
			group = Group.new(intarget, inaddAction); 
			groupID = group.nodeID;
					// allocate private bus
			bus = Bus.audio(server, numChannels);
			busIndex = bus.index;
			decayTime = decay;
			env = Synth.tail(group, envDefName, 
				["i_out", out, "decay", decay, "attack", attack, "amp", amp, "i_in", bus.index]);
			clock = TempoClock.new;
			times.do({arg item, i;
				clock.schedAbs(item + off.value + (i*ioff.value), { server.sendBundle(latency, 
					["/s_new", 
					listName.asString ++ "-" ++ i.asString, 
					server.nextNodeID,
					0,
					groupID,
					"i_out", busIndex + outfunc.value,
					"pos", 1.0.rand2,
					"stretch", stretch.value,
					"pitch", pitch.value
					])
				});
			});
			releaseTime.notNil.if({SystemClock.sched(releaseTime, {this.release}) });
		}, {"Already Playing".error});
	}
	
	// following added to prevent the lang app from choking on large numbers of partials
	playRout { arg out = 0, pitch = 1.0, stretch = 1.0, amp = 1.0, decay = 0.1, 
		off = 0, ioff = 0, attack = 0, releaseTime;
		var numPartials, outfunc;
		numPartials = times.size;
		if(numChannels == 2, {outfunc = 0}, {outfunc = {numChannels.rand}});
		// could have this return the clock so that multiple plays can happen
		playing.not.if({
			playing = true;
			CmdPeriod.add(this);
			// this allows overlapping plays
			group = Group.new(intarget, inaddAction); 
			groupID = group.nodeID;
			// allocate private bus
			bus = Bus.audio(server, numChannels);
			busIndex = bus.index;
			decayTime = decay;
			env = Synth.tail(group, envDefName, 
				["i_out", out, "decay", decay, "attack", attack, "amp", amp, "i_in", bus.index]);
			clock = TempoClock.new;
			Routine({arg item;
				numPartials.do({ arg i;
					server.sendBundle(latency, 
						["/s_new", 
						listName.asString ++ "-" ++ i.asString, 
						server.nextNodeID,
						0,
						groupID,
						"i_out", busIndex + outfunc.value,
						"pos", 1.0.rand2,
						"stretch", stretch.value,
						"pitch", pitch.value
						]);
					if(i < (numPartials - 1), 
						{(times.at(i + 1) - times.at(i) + off.value + (i*ioff.value)).wait;});
				});
			}).play(clock);
			releaseTime.notNil.if({SystemClock.sched(releaseTime, {this.release}) });
			
		}, {"Already Playing".error});
	}
	
	release { arg time;
		var oldbus, releaseTime;
		playing.if({
			playing = false;
			CmdPeriod.remove(this);
			releaseTime = time ? decayTime;
			server.sendBundle((releaseTime) + 0.05, group.freeMsg);
			group = nil;
			playing = false;
			env.release(time);
			env = nil;
			clock.stop;clock = nil;
			oldbus = bus;
			SystemClock.sched((releaseTime) + 0.05, {oldbus.free;});
			bus = nil;
			//SystemClock.sched(decayTime, {playing = false;}) 
			}, {"Not Playing".error}
		)
	}
	
	// call this before object is garbage collected
	free {
		freed.not.if({
			freed = true;
			playing.if({this.release(0); });
		}, { "Already Freed".error });
	}
	
	cmdPeriod { this.release(0); }
}

////////////////////// ResPlayer

// a SynthDef can be passed in which has a fixed and externally determined bus number.
// This allows variable control of parameters

// Source args can be passed in as an array

// SynthDefs passed in must have an argument named i_out which conrols the output bus

// you can also get the source IDs Set, and iterate over those IDs, or the Group, but be careful



PartialPlayerRes {
	classvar <myEnviron, <dir = "synthdefs/", <>latency = 0.05;
	var <server, <bus, <busIndex, <group, <groupID, <clock, <>listName, <size, <numChannels;
	var playing = false, freed = false, <singleSource, times, <env, envDefName, decayTime;
	var myinputDef, <sourceIDs, connectBus, <connectBusIndex, responder;
	var <>intarget, <>inaddAction;
	
	// does require list, if server is nil then Server.local
	*new { arg name, list, inputDef = "PPR-Dust2-Decay2", singleFlag = true, numChan = 2, 
		target = nil, addAction = \addToHead, limiter = false;
		^super.new.init(name, numChan, list, inputDef, singleFlag, target, addAction, limiter);
	}
	
	*dir_ {arg path; if(path.last == $/, { dir = path; }, { dir = path ++ $/ });}
	
	*initClass {
		myEnviron = Environment.new;
		
		// A few prebuilt input Defs
		
		SynthDef.writeOnce("PPR-Dust2", {
			arg i_out, density = 25, mul = 1.0;
			Out.ar(i_out, Dust2.ar(density, mul));
		});
		
		SynthDef.writeOnce("PPR-Dust2-Decay2", {
			arg i_out, density = 25, mul = 1.0;
			var impulses;
			impulses = Decay2.ar(Dust2.ar(density, mul), 0.001, 0.002, WhiteNoise.ar(1));
			//Out.ar(i_out, Decay2.ar(Dust2.ar(density, mul), 0.01, 0.1));
			Out.ar(i_out, impulses);
		});
		
		SynthDef.writeOnce("PPR-WhiteNoise", {
			arg i_out, mul = 0.25;
			Out.ar(i_out, WhiteNoise.ar(mul));
		});
		
		SynthDef.writeOnce("PPR-Crackle", {
			arg i_out, param = 1.95, mul = 0.5;
			Out.ar(i_out, Crackle.ar(param, mul));
		});
	}

	init { arg name, numChan, list, inputDef, singleFlag, target, addAction, limiter;
		var defs;
		singleSource = singleFlag;
		numChannels = numChan;
		// if you pass in a group, must use its server
		server = target.asTarget.server;
		intarget = target;
		inaddAction = addAction;
		
		listName = name ++ "R"; // prevents clashes with PartialPlayer synthdefs
		//defs = (dir ++ listName.asString ++ "/" ++ numChannels.asString ++ "/R*").pathMatch;
		defs = (dir ++ listName.asString ++ "-" ++ "*").pathMatch;
		
		myinputDef = inputDef;
		
//		// allocate private bus
//		bus = Bus.audio(server, numChannels);
//		busIndex = bus.index;
		
		
		// def routes private bus to output and envelopes it
		// have to build here as it's not clear at startup how many channels
		envDefName = "PartialPlayerResEnv" ++ numChan ++ limiter;
		SynthDef(envDefName, {
			arg i_out=0, attack, decay=0.1, amp = 1.0, gate = 1, i_in, limit = 1.0;
			var input, output;
			input = In.ar(i_in, numChannels);
			output = input * EnvGen.kr(Env.asr(attack, 1.0, decay), gate, amp, 0, 1.0, 7);
			limiter.if({output = Limiter.ar(output, limit)});
			//limiter.if({output = CompanderD.ar(output, limit, 1, 0.1, 0.01, 0.01)});
			// free the nodes in the group when released
			Out.ar(i_out, output);
		}).send(server);
		
		if (myEnviron.at(listName.asSymbol).notNil, 
			{ size = myEnviron.at(listName.asSymbol).at(0);
			times = myEnviron.at(listName.asSymbol).at(1);
			//this.makeConnectBus;
			//^this 
			},
			{ 
			if( defs.size == 0, 
//				{if (list.notNil, {this.class.build(server, name, numChannels, list); size = defs.size}, 
//					{"Init failed - No defs and list Nil".warn}) 
//				},
				// use name to allow build to add "R" again
				{ this.class.build(name, numChannels, list, server); size = list.size },
				{ 
					size = defs.size; 
					(size.asString ++ " defs found for " ++ name.asString).inform;
					//this.makeConnectBus; 
				}
			); 
			myEnviron.put(listName.asSymbol, [size, nil]);
			this.makeTimes(list);
			//^this;
			}
		);
	}
	
	makeConnectBus {
		singleSource.if({connectBus = Bus.audio(server, 1);},
			{connectBus = Bus.audio(server, size);});
		connectBusIndex = connectBus.index;
		//connectBusIndex.postln;
	}
	
	// make a list of offsets between partials onsets
	// currently this is what requires the list arg to *.new
	
	makeTimes { arg list;
		// might be sensible to work out deltas here, or to store this info in myEnviron
		times = Array.fill(list.size, { arg i; list.at(i).at(0) });
		myEnviron.at(listName.asSymbol).put(1, times);
	}
	
	// class method that causes the necessary Synth defs to be built
	*build { arg name, numChan, list, server;
		var defDir;
		("Building Defs for " ++ name).inform;
		//defDir = dir ++ name.asString ++ "/" ++ numChan.asString ++ "/";
		//("mkdir -p " ++ defDir).unixCmd;
		list.do({ arg item, i;
			SynthDef(name.asString ++ "R" ++ "-" ++ i.asString, {
				arg i_in, i_out, ring, stretch = 1.0, pitch = 1.0, gate = 1, pos = 0, done;
				var in, partial;
				in = In.ar(i_in);
				partial = Ringz.ar(in,
					EnvGen.kr(Env.new(item.at(4), item.at(2)), gate, pitch, 0, stretch, 0),
					ring,
					// deallocate preceding node (source)
					EnvGen.kr(Env.new(item.at(3), item.at(2)), gate, 1, 0, stretch, done )
				);
				
				// Changed to Pan2 (see note above);
				Out.ar(i_out, Pan2.ar(partial, pos))
			}).load(server, nil, dir);
		});
		("Built" + list.size + "defs").inform;
	}
	
	// cause a server to load a directory of defs. Useful if you don't store them in synthdefs/
	loadDirectory { arg path;
		server.sendMsg("/d_loadDir", path);
	}
	
	play { arg out = 0, pitch = 1.0, ring = 0.5, stretch = 1.0, amp = 1.0, decay = 0.1, 
		off = 0, ioff = 0, attack = 0, releaseTime, limit = 1.0 ... sourceArgs;
		var sourceID, numPartials, outfunc;
		numPartials = times.size;
		if(numChannels == 2, {outfunc = 0}, {outfunc = {numChannels.rand}});
		
		playing.not.if({
			playing = true;
			CmdPeriod.add(this);
			// this allows overlapping plays
			group = Group.new(intarget, inaddAction); 
			groupID = group.nodeID;
			// allocate private bus
			bus = Bus.audio(server, numChannels);
			busIndex = bus.index;
			this.makeConnectBus;
			sourceIDs = Set.new;
		// This keeps track of the sourceIDs so that source nodes can be messaged while playing
			responder = OSCresponderNode(server.addr, "/n_end", 
			{arg msg, id; sourceIDs.remove(id)});
			responder.add;
			decayTime = decay;
			env = Synth.tail(group, envDefName, 
				["i_out", out, "decay", decay, "attack", attack, "amp", amp, 
				"i_in", bus.index, "limit", limit]);
			clock = TempoClock.new;
			singleSource.if({
				sourceID = server.nextNodeID;
				sourceIDs.add(sourceID);
				server.sendBundle(nil, 
						["/s_new", 
						myinputDef, sourceID, 0, groupID, 
						"i_out", connectBusIndex
						] ++ sourceArgs);
				times.do({arg item, i;
					// this stuff commented out as not necessary?
					//var thisSourceID;
					//thisSourceID = server.nextNodeID;
					//sourceIDs.add(thisSourceID);
					clock.schedAbs(item + off.value + (i*ioff.value),{server.sendBundle(latency, 
						["/s_new", 
						listName.asString ++ "-" ++ i.asString, 
						server.nextNodeID,
						3,
						sourceID,
						"i_in", connectBusIndex,
						"i_out", busIndex + outfunc.value,
						"pos", 1.0.rand2,
						"stretch", stretch.value,
						"pitch", pitch.value,
						"ring", ring.value,
						"done", 2 // release preceding node
						])
					});
				});},
				{
				times.do({arg item, i;
					var thisSourceID, interconnect;
					thisSourceID = server.nextNodeID;
					sourceIDs.add(thisSourceID);
					interconnect = connectBusIndex + i;
					clock.schedAbs(item + off.value + (i*ioff.value), {server.sendBundle(latency, 
						["/s_new", 
						myinputDef, thisSourceID, 0, groupID, 
						"i_out", interconnect
						] ++ sourceArgs,
						["/s_new", 
						listName.asString ++ "-" ++ i.asString, 
						server.nextNodeID,
						3,
						thisSourceID,
						"i_in", interconnect,
						"i_out", busIndex + outfunc.value,
						"pos", 1.0.rand2,
						"stretch", stretch.value,
						"pitch", pitch.value,
						"ring", ring.value,
						"done", 3 // release preceding node
						])
					});
				});
			});
			releaseTime.notNil.if({SystemClock.sched(releaseTime, {this.release}) });
		}, {"Already Playing".error});
	}
	
	// following added to keep lang app from choking on large numbers of partials
	playRout { arg out = 0, pitch = 1.0, ring = 0.5, stretch = 1.0, amp = 1.0, decay = 0.1, 
		off = 0, ioff = 0, attack = 0, releaseTime, limit = 1.0 ... sourceArgs;
		var sourceID, numPartials, outfunc;
		numPartials = times.size;
		if(numChannels == 2, {outfunc = 0}, {outfunc = {numChannels.rand}});
		// could have this return the clock so that multiple plays can happen
		playing.not.if({
			playing = true;
			CmdPeriod.add(this);
			// this allows overlapping plays
			group = Group.new(intarget, inaddAction); 
			groupID = group.nodeID;
			// allocate private bus
			bus = Bus.audio(server, numChannels);
			busIndex = bus.index;
			this.makeConnectBus;
			decayTime = decay;
			sourceIDs = Set.new;
		// This keeps track of the sourceIDs so that source nodes can be messaged while playing
			responder = OSCresponderNode(server.addr, "/n_end", 
				{arg msg, id; sourceIDs.remove(id)});
			responder.add;
			env = Synth.tail(group, envDefName, 
				["i_out", out, "decay", decay, "attack", attack, "amp", amp, "i_in", busIndex, 
				"limit", limit]);
			clock = TempoClock.new;
			singleSource.if({
				sourceID = server.nextNodeID;
				sourceIDs.add(sourceID);
				server.sendBundle(nil, 
						["/s_new", 
						myinputDef, sourceID, 0, groupID, 
						"i_out", connectBusIndex
						] ++ sourceArgs);
				Routine({
					// commented out as not necessary
					//var thisSourceID;
					//thisSourceID = server.nextNodeID;
					//sourceIDs.add(thisSourceID);
					numPartials.do({ arg i; 
						server.sendBundle(latency, 
							["/s_new", 
							listName.asString ++ "-" ++ i.asString, 
							server.nextNodeID,
							3,
							sourceID,
							"i_in", connectBusIndex,
							"i_out", busIndex + outfunc.value,
							"pos", 1.0.rand2,
							"stretch", stretch.value,
							"pitch", pitch.value,
							"ring", ring.value,
							"done", 2
							]);
						if(i < (numPartials - 1), 
							{(times.at(i + 1) - times.at(i) + off.value + 
								(i*ioff.value)).wait;});
					});
				}).play(clock);},
				{
				Routine({
					numPartials.do({ arg i;
						var thisSourceID, interconnect;
						thisSourceID = server.nextNodeID;
						sourceIDs.add(thisSourceID);
						interconnect = connectBusIndex + i;
						server.sendBundle(latency, 
							["/s_new", 
							myinputDef, thisSourceID, 0, groupID, 
							"i_out", interconnect
							] ++ sourceArgs,
							["/s_new", 
							listName.asString ++ "-" ++ i.asString, 
							server.nextNodeID,
							3,
							thisSourceID,
							"i_in", interconnect,
							"i_out", busIndex + outfunc.value,
							"pos", 1.0.rand2,
							"stretch", stretch.value,
							"pitch", pitch.value,
							"ring", ring.value,
							"done", 3 // release preceding node
						]);
						if(i < (numPartials - 1), 
							{(times.at(i + 1) - times.at(i) + off.value + 
								(i*ioff.value)).wait;});
					});
				}).play(clock);
			});
			releaseTime.notNil.if({SystemClock.sched(releaseTime, {this.release}) });
		}, {"Already Playing".error});
	}
	
	release { arg time;
		var oldbus, oldConnectBus, oldSourceIDs, releaseTime;
		playing.if({
			playing = false;
			CmdPeriod.remove(this);
			releaseTime = time ? decayTime;
			server.sendBundle((releaseTime) + 0.05, group.freeMsg);
			group = nil;
			env.release(time);
			env = nil;
			responder.remove;
			oldSourceIDs = sourceIDs;
			sourceIDs = nil;
			clock.stop;clock = nil;
			oldbus = bus; oldConnectBus = connectBus;
			SystemClock.sched((releaseTime) + 0.05, 
				{oldbus.free; oldConnectBus.free; oldSourceIDs.clear;});
			bus = nil; connectBus = nil;
			}, {"Not Playing".error}
		)
	}
	
	// call this before object is garbage collected
	// this probably needs to be cleaned up as some have already been freed.
	free {
		freed.not.if({
			freed = true;
			playing.if({
				this.release; 
				SystemClock.sched(decayTime + 0.05, { bus.free; group.free; }) 
				}, { bus.free; connectBus.free; group.free; responder.remove; responder.postln; }
			);
		}, { "Already Freed".error });
	}
	
	cmdPeriod { this.release(0); }
}