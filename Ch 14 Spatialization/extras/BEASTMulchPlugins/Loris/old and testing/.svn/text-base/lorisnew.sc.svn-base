// Partial Players for loris partials. New Version July 05 Birmingham


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


//////////////////////////////

/*

Need one class which makes a single set of defs which output freq and amp info

Store list and defs and time lists in a folder in the defs folder

Allow any target defs

For Single Source just use an In UGen

Since Synths ignore args which they don't understand, you can set on the Group

target synth requires i_freq, i_amp, i_phase, i_out, and pos

To Do: Fix defs in initClass

	Consider auto stopping. 
		Should play automatically release?
		Should the env synth free itself and enclosing group after awhile?
	
	Should multiple plays be possible?	
	
	Allow source args to be functions
	
	Fix release times
	
	Should noise sources have individual muls? Should they have a common name

*/

/////

// This should be a dependent of its Server. When the server is rebooted it should reload.
// perhaps with environment var? or Partials instance store which servers are loaded.

// needs to have release and free updated

LorisPlayer {
	classvar <>latency = 0.05;
	var partials, <numChannels, <>targetDef, >target, >addAction;
	var <server, <bus, <busIndex, <group, <groupID, <clock, <size;
	var playing = false, freed = false, <env, envDefName, decayTime;
	var <targetIDs, freqBus, <freqBusIndex, ampBus, <ampBusIndex, responder;
	var times, phases, name;
	var bwBus, <bwBusIndex;
	
	// does require list, if target is nil then Server.default
	*new { arg partials, targetDef = "Ls-Sine", numChannels = 2, 
		target = nil, addAction = \addToHead, limiter = false;
		^super.newCopyArgs(partials, numChannels.asInteger, targetDef, target, addAction)
			.init(limiter);
	}
	
	*initClass {
		
		// A few prebuilt target Defs
		
		SynthDef.writeOnce("Ls-Sine", {
			arg i_freq, i_amp, i_phase, i_out, pos = 0;
			var partial;
			partial = SinOsc.ar(In.ar(i_freq), i_phase, In.ar(i_amp));
			OffsetOut.ar(i_out, Pan2.ar(partial, pos));
		});
		
		SynthDef.writeOnce("Ls-Dust2-Decay2", {
			arg i_freq, i_amp, i_out, density = 25, decay = 0.5, pos = 0;
			var impulses, partial;
			impulses = Decay2.ar(Dust2.ar(density), 0.001, 0.002, WhiteNoise.ar(1));
			partial = Ringz.ar(impulses, In.ar(i_freq), decay, In.ar(i_amp));
			OffsetOut.ar(i_out, Pan2.ar(partial, pos));
		});
		
		SynthDef.writeOnce("Ls-Ringz-Input", {
			arg i_freq, i_amp, i_out, bus, decay = 0.5, pos = 0;
			var in, partial;
			in = In.ar(bus, 1);
			partial = Ringz.ar(in, In.ar(i_freq), decay, In.ar(i_amp));
			OffsetOut.ar(i_out, Pan2.ar(partial, pos));
		});
		
		SynthDef.writeOnce("Ls-Ringz-WhiteNoise", {
			arg i_freq, i_amp, i_out, decay = 0.5, pos = 0, mul = 0.2;
			var in, partial;
			in = WhiteNoise.ar(mul);
			partial = Ringz.ar(in, In.ar(i_freq), decay, In.ar(i_amp));
			OffsetOut.ar(i_out, Pan2.ar(partial, pos));
		});

		SynthDef.writeOnce("Ls-Ringz-Crackle", {
			arg i_freq, i_amp, i_out, decay = 0.5, pos = 0, param = 1.95, mul = 0.5;
			var in, partial;
			in = Crackle.ar(param, mul);
			partial = Ringz.ar(in, In.ar(i_freq), decay, In.ar(i_amp));
			OffsetOut.ar(i_out, Pan2.ar(partial, pos));
		});
		
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
		
		SynthDef.writeOnce("PPR-Crackle", {
			arg i_out, param = 1.95, mul = 0.5;
			Out.ar(i_out, Crackle.ar(param, mul));
		});
	}

	init { arg limiter;
		var defs;
		// if you pass in a group, must use its server
		server = target.asTarget.server;
		if(server.serverRunning.not, {"Server not booted".warn; ^this});
		name = partials.name;
		times = partials.times;
		phases = partials.phases;
		size = partials.size;
		
		// def routes private bus to output and envelopes it
		// have to build here as it's not clear at startup how many channels
		envDefName = "LorisPartialsEnv" ++ numChannels ++ limiter;
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
		
		partials.loadDefs(server);

	}
	
	makeBusses {
		freqBus = Bus.audio(server, size);
		freqBusIndex = freqBus.index;
		ampBus = Bus.audio(server, size);
		ampBusIndex = ampBus.index;
		bwBus = Bus.audio(server, size);
		bwBusIndex = bwBus.index;
	}
		
	play { arg out = 0, pitch = 1.0, ring = 0.5, stretch = 1.0, bw = 1.0, mul = 1.0, decay = 0.1, 
		off = 0, ioff = 0, attack = 0, releaseTime, limit = 1.0, doneAction = 4 ... targetArgs;
		var sourceID, outfunc, bund;

		// if we're done, you can play again
		if((targetIDs.size == 0) && playing, {this.release(0) });
		
		// this needs to be numChannels - 1 as there's always an extra channel with pan
		if(numChannels == 2, {outfunc = 0}, {outfunc = {(numChannels - 1).rand}});
		// could have this return the clock so that multiple plays can happen
		playing.not.if({
			playing = true;
			CmdPeriod.add(this);
			bund = [];
			// this allows overlapping plays
			bund = server.makeBundle(false, {
				group = Group.new(target, addAction); 
				groupID = group.nodeID;
			}, bund);
			// allocate private bus
			bus = Bus.audio(server, numChannels);
			busIndex = bus.index;
			this.makeBusses;
			decayTime = decay;
			targetIDs = Set.new;
		// This keeps track of the targetIDs so that target nodes can be messaged while playing
			responder = OSCresponderNode(server.addr, "/n_end", {arg time, responder, msg;
				targetIDs.remove(msg[1]);});
			responder.add;
			bund = server.makeBundle(false, {
				env = Synth.tail(group, envDefName, 
					["i_out", out, "decay", decay, "attack", attack, "amp", mul, "i_in", 
					busIndex, "limit", limit]);
			}, bund);
			clock = TempoClock.new;
			Routine({
				size.do({ arg i;
					var thisControllerID, thisTargetID, freqConnect, ampConnect, bwConnect;
					thisControllerID = server.nextNodeID;
					thisTargetID = server.nextNodeID;
					targetIDs.add(thisTargetID);
					freqConnect = freqBusIndex + i;
					ampConnect = ampBusIndex + i;
					bwConnect = bwBusIndex + i;
					server.listSendBundle(latency, 
						// controlling synth
						bund ++ [["/s_new", 
						name ++ "-" ++ i.asString, 
						thisControllerID,
						0,
						groupID,
						"i_lsFreq", freqConnect,
						"i_lsAmp", ampConnect,
						"i_lsBw", bwConnect,
						"lsStretch", stretch.value,
						"lsPitch", pitch.value,
						"lsBandScal", bw.value,
						"lsDoneAction", doneAction // default 4, release following node
						],
						// target synth
						["/s_new", 
						targetDef.value, 
						thisTargetID, 
						3, 				// add after controller
						thisControllerID, 
						"i_freq", freqConnect,
						"i_amp", ampConnect,
						"i_bw", bwConnect,
						"i_out", busIndex + outfunc.value,
						"i_phase", phases[i],
						"pos", 1.0.rand2
						] ++ targetArgs.value]);
						bund = [];
					(times[i] + off.value + (i*ioff.value)).wait;
				});
			}).play(clock);
			releaseTime.notNil.if({SystemClock.sched(releaseTime, {this.release}) });
		}, {"Already Playing".error});
	}
	
	release { arg time;
		var oldbus, oldFreqBus, oldAmpBus, oldBWBus, oldtargetIDs, releaseTime;
		playing.if({
			clock.stop;clock = nil;
			playing = false;
			CmdPeriod.remove(this);
			releaseTime = time ? decayTime;
			server.sendBundle((releaseTime) + 0.05, group.freeMsg);
			group = nil;
			env.release(time);
			env = nil;
			responder.remove;
			oldtargetIDs = targetIDs;
			targetIDs = nil;
			oldbus = bus; oldFreqBus = freqBus; oldAmpBus = ampBus; oldBWBus = bwBus;
			SystemClock.sched((releaseTime) + 0.05, 
				{oldbus.free; oldFreqBus.free; oldAmpBus.free; oldBWBus.free; 
					oldtargetIDs.clear;});
			bus = nil; freqBus = nil; ampBus = nil; bwBus = nil;
			}, {"Not Playing".error}
		)
	}
	
	// call this before object is garbage collected
	// this probably needs to be cleaned up as some have already been freed.
	free {
		freed.not.if({
			freed = true;
			playing.if({
				this.release(0); 
				}, { bus.free; freqBus.free; ampBus.free; bwBus.free; group.free; 
					responder.remove; }
			);
		}, { "Already Freed".error });
	}
	
	cmdPeriod { this.release(0); }
}

//// testing version still needs release and free
//LorisPartialsBW : LorisPartials {
//	var bwBus, <bwBusIndex;
//
//	*build { arg pathOrList, name, defDir;
//		var list, times, phases;
//		name = name.asString;		
//		if(pathOrList.isString, {list = thisProcess.interpreter.executeFile(pathOrList)},
//			{list = pathOrList});
//		times = Array.new(list.size);
//		phases = Array.new(list.size);
//		("Building Defs for " ++ name).inform;
//		if(defDir.notNil, { if(defDir.last != $/, {defDir = defDir ++ "/"}) });
//		defDir = (defDir ?? dir) ++ name.asString ++ "/";
//		// use systemCmd for no blocking
//		("mkdir -p " ++ $" ++ defDir.standardizePath ++ $").systemCmd; 
//		list.do({ arg item, i;
//			phases.add(item[1]);
//			SynthDef(name.asString ++ "-" ++ i.asString, {
//				arg i_lsFreq, i_lsAmp, i_lsBw, lsStretch = 1.0, lsPitch = 1.0, lsBandScal = 1.0, 
//					lsDoneAction = 0;
//				var freq, amp, bw;
//				freq = EnvGen.ar(Env.new(item[4], item[2]), 1, lsPitch, 0, lsStretch, 0);
//				bw = EnvGen.ar(Env.new(item[5], item[2]), 1, lsBandScal, 0, lsStretch, 0);
//				amp = EnvGen.ar(Env.new(item[3], item[2]), 1, 1, 0, lsStretch, lsDoneAction);
//				OffsetOut.ar(i_lsFreq, freq);
//				OffsetOut.ar(i_lsAmp, amp);
//				OffsetOut.ar(i_lsBw, bw);
//			}).writeDefFile(defDir);
//		});
//		("Built" + list.size + "defs").inform;
//		list.doAdjacentPairs({|item, next| times.add(next[0] - item[0]) });
//		times.add(0); // for last wait
//		times.writeTextArchive(defDir ++ name ++ "-times");
//		phases.writeTextArchive(defDir ++ name ++ "-phases");
//		if(pathOrList.isString, {("cp" + pathOrList + defDir).systemCmd}); // copy original list
//		
//	}
//	
//	// warp tscale
////	*buildTWarp { arg path, name, tenv, defDir;
////		var list, times, phases;
////		name = name.asString;		
////		list = thisProcess.interpreter.executeFile(path);
////		times = Array.new(list.size);
////		phases = Array.new(list.size);
////		("Building Defs for " ++ name).inform;
////		if(defDir.notNil, { if(defDir.last != $/, {defDir = path ++ "/"}) });
////		defDir = (defDir ?? dir) ++ name.asString ++ "/";
////		// use systemCmd for no blocking
////		("mkdir -p " ++ $" ++ defDir.standardizePath ++ $").systemCmd; 
////		list.do({ arg item, i;
////			phases.add(item[1]);
////			SynthDef(name.asString ++ "-" ++ i.asString, {
////				arg i_lsFreq, i_lsAmp, i_lsBw, lsStretch = 1.0, lsPitch = 1.0, lsBandScal = 1.0, 
////					lsDoneAction = 0;
////				var freq, amp, bw, timeEnvGen;
////				timeEnvGen = EnvGen.ar(tenv, timeScale: lsStretch);
////				freq = EnvGen.ar(Env.new(item[4], item[2]), 1, lsPitch, 0, timeEnvGen, 0);
////				bw = EnvGen.ar(Env.new(item[5], item[2]), 1, lsBandScal, 0, timeEnvGen, 0);
////				amp = EnvGen.ar(Env.new(item[3], item[2]), 1, 1, 0, timeEnvGen, lsDoneAction);
////				OffsetOut.ar(i_lsFreq, freq);
////				OffsetOut.ar(i_lsAmp, amp);
////				OffsetOut.ar(i_lsBw, bw);
////			}).writeDefFile(defDir);
////		});
////		("Built" + list.size + "defs").inform;
////		list.doAdjacentPairs({|item, next| times.add(next[0] - item[0]) });
////		times.add(0); // for last wait
////		times.writeTextArchive(defDir ++ name ++ "-times");
////		phases.writeTextArchive(defDir ++ name ++ "-phases");
////		("cp" + path + defDir).systemCmd; // copy original list
////		
////	}
//
//	makeBusses {
//		super.makeBusses;
//		bwBus = Bus.audio(server, size);
//		bwBusIndex = bwBus.index;
//	}
//	
//	play { arg out = 0, pitch = 1.0, ring = 0.5, stretch = 1.0, bw = 1.0, mul = 1.0, decay = 0.1, 
//		off = 0, ioff = 0, attack = 0, releaseTime, limit = 1.0, doneAction = 4 ... targetArgs;
//		var sourceID, outfunc;
//
//		// if we're done, you can play again
//		if((targetIDs.size == 0) && playing, {this.release(0) });
//		
//		if(numChannels == 2, {outfunc = 0}, {outfunc = {numChannels.rand}});
//		// could have this return the clock so that multiple plays can happen
//		playing.not.if({
//			playing = true;
//			CmdPeriod.add(this);
//			// this allows overlapping plays
//			group = Group.new(target, addAction); 
//			groupID = group.nodeID;
//			// allocate private bus
//			bus = Bus.audio(server, numChannels);
//			busIndex = bus.index;
//			this.makeBusses;
//			decayTime = decay;
//			targetIDs = Set.new;
//		// This keeps track of the targetIDs so that target nodes can be messaged while playing
//			responder = OSCresponderNode(server.addr, "/n_end", {arg time, responder, msg;
//				targetIDs.remove(msg[1]);});
//			responder.add;
//			env = Synth.tail(group, envDefName, 
//				["i_out", out, "decay", decay, "attack", attack, "amp", mul, "i_in", busIndex, 
//				"limit", limit]);
//			clock = TempoClock.new;
//			Routine({
//				size.do({ arg i;
//					var thisControllerID, thisTargetID, freqConnect, ampConnect, bwConnect;
//					thisControllerID = server.nextNodeID;
//					thisTargetID = server.nextNodeID;
//					targetIDs.add(thisTargetID);
//					freqConnect = freqBusIndex + i;
//					ampConnect = ampBusIndex + i;
//					bwConnect = bwBusIndex + i;
//					server.sendBundle(latency, 
//						// controlling synth
//						["/s_new", 
//						name ++ "-" ++ i.asString, 
//						thisControllerID,
//						0,
//						groupID,
//						"i_lsFreq", freqConnect,
//						"i_lsAmp", ampConnect,
//						"i_lsBw", bwConnect,
//						"lsStretch", stretch.value,
//						"lsPitch", pitch.value,
//						"lsBandScal", bw.value,
//						"lsDoneAction", doneAction // default 4, release following node
//						],
//						// target synth
//						["/s_new", 
//						targetDef.value, 
//						thisTargetID, 
//						3, 				// add after controller
//						thisControllerID, 
//						"i_freq", freqConnect,
//						"i_amp", ampConnect,
//						"i_bw", bwConnect,
//						"i_out", busIndex + outfunc.value,
//						"i_phase", phases[i],
//						"pos", 1.0.rand2
//						] ++ targetArgs.value);
//					(times[i] + off.value + (i*ioff.value)).wait;
//				});
//			}).play(clock);
//			releaseTime.notNil.if({SystemClock.sched(releaseTime, {this.release}) });
//		}, {"Already Playing".error});
//	}
//
//}

// need to have env synth release everything

LorisBufPlayer : LorisPlayer {
//	classvar <dict, <dir = "/Users/scottw/lorisdefs/", <>latency = 0.05;
//	var <numChannels, <>targetDef, target, addAction;
//	var <server, <bus, <busIndex, <group, <groupID, <clock, <size;
//	var playing = false, freed = false, times, <env, envDefName, decayTime;
//	var <targetIDs, freqBus, <freqBusIndex, ampBus, <ampBusIndex, responder;
//	var <thisDir, times, phases;
	
	var <>buffer;
	
	// if target is nil then Server.default
	*new { arg partials, targetDef = "Ls-Sine", numChannels = 2, 
		target = nil, addAction = \addToHead, limiter = false, parentPath;
		^super.newCopyArgs(partials, numChannels.asInteger, targetDef, target, addAction)
			.init(limiter, parentPath).loadBuffers;
	} // parentPath
	
	*initClass {
		// write the control def
		SynthDef("LorisBufPartial", {
				arg i_lsFreq, i_lsAmp, i_lsBw, i_lsIndex, i_lsBufnum, lsPitch = 1.0, 
					lsBandScal = 1.0, i_bufChanOffset;
				var params;
				
				params = BufRdChanOffset.ar(3, i_lsBufnum, In.ar(i_lsIndex, 1), 0, 1,
					chanOffset: i_bufChanOffset);
				OffsetOut.ar(i_lsFreq, params[0] * lsPitch);
				OffsetOut.ar(i_lsAmp, params[1]);
				OffsetOut.ar(i_lsBw, params[2] * lsBandScal);
		}).writeDefFile("synthdefs/");// later make writeOnce
	
	}

//	loadBuffers {
//		
//		buffers = Array.new(size);
//		times = Array.new(size);
//		phases = Array.new(size);
//		//times.add(0); // for last wait
//		LorisPartials.fadeInOut(partials.partialList);
//		partials.partialList.do({ arg item, i;
//			var buffer, envs, time;
//			time = item[0];
//			times.add(time);
//			phases.add(item[1]);
//			// freq, amp, bw
//			envs = [Env(item[4], item[2]), Env(item[3], item[2]), Env(item[5], item[2])]
//				.collect({|env| env.delay(time).asSignal(item[2].sum + time * 44100).as(Array)})
//				.interlace;
//			
//			buffer = Buffer.loadCollection(server, envs, 3);
//			buffers.add(buffer);
//		});
//		
//	}
	
//	loadBuffers {
//		
//		buffers = Array.new(size);
//		times = Array.new(size);
//		phases = Array.new(size);
//		//times.add(0); // for last wait
//		LorisPartials.fadeInOut(partials.partialList);
//		Routine.run {
//		partials.partialList.do({ arg item, i;
//			var buffer, envs, time;
//			time = item[0];
//			times.add(time);
//			phases.add(item[1]);
//			// freq, amp, bw
//			envs = [Env(item[4], item[2]), Env(item[3], item[2]), Env(item[5], item[2])]
//				.collect({|env| env.delay(time).asSignal(item[2].sum + time * 44100).as(Array)})
//				.lace;
//			
//			buffer = Buffer.loadCollection(server, envs, 3);
//			server.sync;
//			buffers.add(buffer);
//		});
//		}
//	}

	loadBuffers {
		var envs, maxFrames;
		//buffers = Array.new(size);
		times = Array.new(size);
		phases = Array.new(size);
		//times.add(0); // for last wait
		LorisPartials.fadeInOut(partials.partialList);
		
		partials.partialList.do({ arg item, i;
			var time;
			time = item[0];
			times.add(time);
			phases.add(item[1]);
			// freq, amp, bw
			envs = envs.addAll(
				[Env(item[4], item[2]), Env(item[3], item[2]), Env(item[5], item[2])]
				.collect({|env| env.delay(time).asSignal(item[2].sum + time * 4096)
				.as(Array)})
			);
			
			
		});
		maxFrames = envs.collect(_.size).maxItem;
		//zero pad if needed
		envs = envs.collect({|item| 
			(item.size < maxFrames).if({item.extend(maxFrames, 0.0)}, {item})
		});
		buffer = Buffer.loadCollection(server, envs.lace, 3 * size);

	}
		
	init { arg limiter;

		// if you pass in a group, must use its server
		server = target.asTarget.server;
		if(server.serverRunning.not, {"Server not booted".warn; ^this});
		
		size = partials.size;
		
		// def routes private bus to output and envelopes it
		// have to build here as it's not clear at startup how many channels
		envDefName = "LorisPartialsEnv" ++ numChannels ++ limiter;
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
		
	}
	// ring?
	play { arg out = 0, pitch = 1.0, ring = 0.5, bw = 1.0, mul = 1.0, decay = 0.1, 
		attack = 0, releaseTime, limit = 1.0, index ... targetArgs;
		var sourceID, outfunc;

		// if we're done, you can play again
		if((targetIDs.size == 0) && playing, {this.release(0) });
		
		if(numChannels == 2, {outfunc = 0}, {outfunc = {(numChannels - 1).rand}});
		// could have this return the clock so that multiple plays can happen
		playing.not.if({
			playing = true;
			CmdPeriod.add(this);
			// this allows overlapping plays
			group = Group.new(target, addAction); 
			groupID = group.nodeID;
			// allocate private bus
			bus = Bus.audio(server, numChannels);
			busIndex = bus.index;
			this.makeBusses;
			decayTime = decay;
			targetIDs = Set.new;
		// This keeps track of the targetIDs so that target nodes can be messaged while playing
			responder = OSCresponderNode(server.addr, "/n_end", {arg time, responder, msg;
				targetIDs.remove(msg[1]);});
			responder.add;
			server.makeBundle(latency, {
				env = Synth.tail(group, envDefName.postln, 
					["i_out", out, "decay", decay, "attack", attack, "amp", mul, "i_in", 
					busIndex, "limit", limit]);
			});
			clock = TempoClock.new;
			server.makeBundle(latency, {
				size.do({ arg i;
					var thisControllerID, thisTargetID, freqConnect, ampConnect, bwConnect;
					thisControllerID = server.nextNodeID;
					thisTargetID = server.nextNodeID;
					targetIDs.add(thisTargetID);
					freqConnect = freqBusIndex + i;
					ampConnect = ampBusIndex + i;
					bwConnect = bwBusIndex + i;
					server.listSendMsg(
						// controlling synth
						["/s_new", 
						"LorisBufPartial", 
						thisControllerID,
						0,
						groupID,
						"i_lsFreq", freqConnect,
						"i_lsAmp", ampConnect,
						"i_lsBw", bwConnect,
						"i_lsIndex", index.value,
						"i_lsBufnum", buffer.bufnum,
						"lsPitch", pitch.value,
						"lsBandScal", bw.value,
						"i_bufChanOffset", i * 3
						]);
					server.listSendMsg(
						// target synth
						["/s_new", 
						targetDef.value, 
						thisTargetID, 
						3, 				// add after controller
						thisControllerID, 
						"i_freq", freqConnect,
						"i_amp", ampConnect,
						"i_bw", bwConnect,
						"i_out", busIndex + outfunc.value,
						"i_phase", phases[i],
						"pos", 1.0.bilinrand
						] ++ targetArgs.value);
					
				});
			});
			releaseTime.notNil.if({SystemClock.sched(releaseTime, {this.release}) });
		}, {"Already Playing".error});
	}
}

