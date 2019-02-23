// interim version

// Overlap add style listplayers for SC3 Created Toronto Dec 19/03
// adapted from SC2 code

// intended for lists of the following format:
/**

#[
[[amp, freq], [amp, freq], [amp, freq]],	//one frame with three bands
[[amp, freq], ...]
...
]
*/

// CHANGE SERVER ARG TO TARGET!!!!!

// Could make it possible to swap in different lists, but would need to reallocate connectBus in Res version

// Could add in phase arg and still make it compatible with phaseless lists

// Should "frametime" be frametime.poll * overlap.poll

// could have a source group and use free

// free messages unecessary? or should be changed to handle CmdPeriod elegantly?

OverlapAddPlayer {
	var server, frameList, routine, playing = false, envDefName, numChan, <>latency = 0.05;
	var group, groupID, bus, <busIndex, <env, <clock, freed = false, maxFrameSize, bankSize;
	var bankDefName, intarget, <>intarget, <>inaddAction, decayTime;
	
	*new { arg list, numChannels = 2, target = nil, addAction = \addToHead;
		^super.new.init(list, numChannels, target, addAction);
	}
	
	init { arg list, numChannels, target, addAction;
		frameList = list;
		numChan = numChannels;
		server = target.asTarget.server;
		intarget = target;
		inaddAction = addAction;
		envDefName = "system-OverlapAddPlayerEnv" ++ numChan;
		SynthDef(envDefName, {
			arg i_out=0, decay=0.1, amp = 1.0, gate = 1, i_in, attack;
			var input, output;
			input = In.ar(i_in, numChannels);
			output = input * EnvGen.kr(Env.asr(attack, 1.0, decay), gate, amp, 0, 1.0, 7);
			// free the nodes in the group when released
			Out.ar(i_out, output);
		}).send(server);
		maxFrameSize = frameList.maxItem({arg item; item.size}).size;
		
		("maxFrameSize:" + maxFrameSize).postln;
		bankSize = maxFrameSize / numChannels;
		if(bankSize.frac >= 0.5, { bankSize = bankSize.ceil; }, {bankSize = bankSize.floor});
		postln("bankSize:" + bankSize);
		bankDefName = "system-OverlapAddBankPartial" ++ bankSize;
		SynthDef(bankDefName, { arg i_out, freq, frameTime;
			var bank, freqs, amps, phases;
//			osc = FSinOsc.ar(freq, 0, amp) * EnvGen.kr(Env.sine(1,1), 1.0, amp, 0, frameTime, 2);
			freqs = Control.names([\freqs]).ir(Array.fill(bankSize, 100.0));
			amps = Control.names([\amps]).ir(Array.fill(bankSize, 0.0));
			phases = Array.fill(bankSize, 0.0);
			bank = Klang.ar(`[freqs, amps, phases], freq)
				* EnvGen.kr(Env.sine(1,1), 1.0, 1.0, 0, frameTime, 2); 
			//Out.ar(i_out, Pan2.ar(bank, pos));
			Out.ar(i_out, bank);
		}).send(server);
	}
	
	*initClass {
		SynthDef.writeOnce("system-OverlapAddPartial", { arg i_out, freq, amp, frameTime, pos;
			var osc;
			osc = FSinOsc.ar(freq, 0, amp) * EnvGen.kr(Env.sine(1,1), 1.0, 1.0, 0, frameTime, 2);
			Out.ar(i_out, Pan2.ar(osc, pos));
		});
	}
	
	play { arg freqScal = 1, frameTime = 0.05, mul = 1, overlap = 3, out = 0, endFunc, 
			envSynth, lim, attack = 0.0, decay = 0.1;
		var outfunc;
		if(numChan == 2, {outfunc = 0}, {outfunc = {numChan.rand}});
		playing.not.if({
			playing = true;
			CmdPeriod.add(this);
			decayTime = decay;
			group = Group.new(intarget, inaddAction);
			groupID = group.nodeID;
			bus = Bus.audio(server, numChan);
			busIndex = bus.index;
			clock = TempoClock.new;
			env = envSynth ? Synth.tail(group, envDefName, 
				["i_out", out, "decay", decay, "attack", attack, "amp", mul, "i_in", busIndex]);
			routine = Routine({
				var size;
				size = lim ? frameList.size;
				size.do({ arg i;
					var frame, bundle;
					frame = frameList[i];
					bundle = Array.new(frame.size);
					frame.do({ arg item;
						var amp;
						amp = item.at(0);
						bundle.add(
							["/s_new", 
							"system-OverlapAddPartial", 
							server.nextNodeID,
							0,
							groupID,
							"i_out", busIndex + outfunc.value,
							"freq", item.at(1) * freqScal.poll,
							"amp", amp,
							"frameTime", frameTime.poll * overlap.poll,
							"pos", 1.0.rand2
							]);
					});
					server.listSendBundle(latency, bundle);
					frameTime.poll.wait;
				});
				(overlap.poll * frameTime.poll).wait;
				env.release;
				endFunc.value;
				playing = false;
			}).play(clock);
		}, {"Already Playing".warn; });
	}
	
	playBank { arg freqScal = 1, frameTime = 0.05, mul = 1, overlap = 3, out = 0, endFunc, 
			envSynth, lim, attack = 0.0, decay = 0.1;
		var outfunc;
		if(numChan == 2, {outfunc = 0}, {outfunc = {numChan.rand}});
		playing.not.if({
			playing = true;
			CmdPeriod.add(this);
			decayTime = decay;
			group = Group.new(intarget, inaddAction);
			groupID = group.nodeID;
			bus = Bus.audio(server, numChan);
			busIndex = bus.index;
			clock = TempoClock.new;
			env = envSynth ? Synth.tail(group, envDefName, 
				["i_out", out, "decay", decay, "attack", attack, "amp", mul, "i_in", busIndex]);
			routine = Routine({
				var size;
				size = lim ? frameList.size;
				size.do({ arg i;
					var frame, split, bundle;
					frame = frameList[i].scramble;
					split = Array.new(numChan);
					numChan.do({split.add(Array.new(bankSize))});
					bundle = Array.new(numChan * 3);
					frame.do({arg item, j; split.wrapAt(j).add(item)});
					split = split.scramble;
					//split.asCompileString.postln;
					split.do({ arg item, j;
						var flop, freqs, amps, thisSize, thisID;
						flop = item.flop;
						amps = flop.at(0);
						freqs = flop.at(1);
						thisSize = freqs.size;
						thisID = server.nextNodeID;
						bundle.add(
							["/s_new", 
							bankDefName, 
							thisID,
							0,
							groupID,
							"i_out", (busIndex + j),
							"freq", freqScal.poll,
							"frameTime", frameTime.poll * overlap.poll
							//"pos", 1.0.rand2
						]);
						// set Controls
//						bundle.add(
//							["/n_setn",  
//							thisID,
//							"freqs"] ++ thisSize ++ freqs ++
//							["amps"] ++ thisSize ++ amps
//						);
						bundle.add(
							["/n_setn",  
							thisID,
							"amps", thisSize] ++ amps
						);
						bundle.add(
							["/n_setn",  
							thisID,
							"freqs", thisSize] ++ freqs
						);
					});
					//bundle.asCompileString.postln;
					server.listSendBundle(latency, bundle);
					frameTime.poll.wait;
				});
				(overlap.poll * frameTime.poll).wait;
				env.release;
				endFunc.value;
				playing = false;
			}).play(clock);
		}, {"Already Playing".warn; });
	}

		
	// this currently does not allow a new play until release is finished.
	release { arg time;
		var oldbus, oldclock, releaseTime;
		playing.if({ 
			playing = false;
			CmdPeriod.remove(this);
			releaseTime = time ? decayTime;
			server.sendBundle((releaseTime) + 0.05, group.freeMsg);
			group = nil;
			env.release(releaseTime);
			env = nil;
			oldbus = bus; bus = nil;
			oldclock = clock; 
			clock = nil; 
			SystemClock.sched(releaseTime - latency - 0.05, {oldclock.stop;});
			SystemClock.sched(releaseTime + 0.05, {oldbus.free;});
		}, {"Not Playing".warn;});
	}
	
		// call this before object is garbage collected
	free {
		freed.not.if({
			freed = true;
			playing.if({
				this.release(0.1); 
				}, { bus.free; group.free;}
			);
		}, { "Already Freed".error });
	}
	
	cmdPeriod { this.release(0); }
}

// a better way to handle the OSCresponderNode?
OverlapAddRes {
	var server, frameList, routine, playing = false, envDefName, numChan, <>latency = 0.05;
	var group, groupID, bus, <busIndex, <env, <clock, freed = false, maxFrameSize;
	var myinputDef, <sourceIDs, connectBus, connectBusIndex, responder, <singleSource;
	var bankSize, bankDefName, intarget, <>intarget, <>inaddAction, decayTime;

	
	*new { arg list, inputDef = "PPR-Dust2-Decay2", singleFlag = true, numChannels = 2,
			target = nil, addAction = \addToHead, limiter = false;
		^super.new.init(list, inputDef, singleFlag, numChannels, target, addAction, limiter);
	}
	
	init { arg list, inputDef, singleFlag, numChannels, target, addAction, limiter;
		frameList = list;
		numChan = numChannels;
		server = target.asTarget.server;
		intarget = target;
		inaddAction = addAction;
		myinputDef = inputDef;
		singleSource = singleFlag;
		// Envelope Def	
		envDefName = "OverlapAddPlayerEnv" ++ numChan ++ limiter;
		SynthDef(envDefName, {
			arg i_out=0, decay=0.1, amp = 1.0, gate = 1, i_in, limit = 1, attack;
			var input, output;
			input = In.ar(i_in, numChannels);
			output = input * EnvGen.kr(Env.asr(attack, 1.0, decay), gate, amp, 0, 1.0, 7);
			limiter.if({output = Limiter.ar(output, limit)});
			// free the nodes in the group when released
			Out.ar(i_out, output);
		}).send(server);
		
		maxFrameSize = frameList.maxItem({arg item; item.size}).size;
		
		("maxFrameSize:" + maxFrameSize).postln;
		bankSize = maxFrameSize / numChannels;
		if(bankSize.frac >= 0.5, { bankSize = bankSize.ceil; }, {bankSize = bankSize.floor});
		postln("bankSize:" + bankSize);
		bankDefName = "system-OverlapAddResBank" ++ bankSize;
		SynthDef(bankDefName, { arg i_out, i_in, freq, frameTime;
			var bank, freqs, amps, rings;
//			osc = FSinOsc.ar(freq, 0, amp) * EnvGen.kr(Env.sine(1,1), 1.0, amp, 0, frameTime, 2);
			freqs = Control.names([\freqs]).ir(Array.fill(bankSize, 100.0));
			amps = Control.names([\amps]).ir(Array.fill(bankSize, 0.0));
			rings = Control.names([\rings]).ir(Array.fill(bankSize, 0.0));
			bank = Klank.ar(`[freqs, amps, rings], In.ar(i_in, 1),freq)
				* EnvGen.kr(Env.sine(1,1), 1.0, 1.0, 0, frameTime, 2); 
			//Out.ar(i_out, Pan2.ar(bank, pos));
			Out.ar(i_out, bank);
		}).send(server);
	}
	
	*initClass {
		SynthDef.writeOnce("system-OverlapAddResPartial", { arg i_out, i_in, ring, 
				freq, amp, frameTime, pos;
			var osc;
			osc = Ringz.ar(In.ar(i_in, 1), freq, ring, amp) 
				* EnvGen.kr(Env.sine(1,1), 1.0, 1.0, 0, frameTime, 2);
			Out.ar(i_out, Pan2.ar(osc, pos));
		});
		
		// A few prebuilt input Defs
		// These are copied from PartialPlayerRes and use the same names, 
		// so need to be kept in sync
		
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
		
		SynthDef.writeOnce("PPR-ClipNoise", {
			arg i_out, mul = 0.25;
			Out.ar(i_out, ClipNoise.ar(mul));
		});
	}
	
	makeConnectBus { arg bank = false;
		// this only allows for single channel singleSources; better way?
		singleSource.if({connectBus = Bus.audio(server, 1);},
			{bank.if({connectBus = Bus.audio(server, numChan);}, 
				{connectBus = Bus.audio(server, maxFrameSize);})});
		connectBusIndex = connectBus.index;
		//connectBusIndex.postln;
	}
	
	play { arg freqScal = 1, frameTime = 0.05, ring = 0.5, mul = 1, overlap = 3, out = 0, endFunc, 
			envSynth, lim, attack = 0.0, decay = 0.1, limit = 1 ... sourceArgs;
		var outfunc, sourceID, envID;
		var sourceBundle;
		if(numChan == 2, {outfunc = 0}, {outfunc = {(numChan - 1).rand}});
		playing.not.if({
			playing = true;
			CmdPeriod.add(this);
			decayTime = decay;
			group = Group.new(intarget, inaddAction);
			groupID = group.nodeID;
			// allocate private bus
			bus = Bus.audio(server, numChan);
			busIndex = bus.index;
			this.makeConnectBus(false);
			sourceIDs = Set.new;
			responder = OSCresponderNode(server.addr, "/n_end", 
				{arg msg, id; sourceIDs.remove(id)});
			responder.add;
			clock = TempoClock.new;
			env = envSynth ? Synth.tail(group, envDefName, 
				["i_out", out, "decay", decay, "attack", attack, "amp", 1, "i_in", busIndex, 
				"limit", limit]);
			envID = env.nodeID;
			singleSource.if({
				sourceID = server.nextNodeID;
				sourceIDs.add(sourceID);
				server.sendBundle(nil, 
						["/s_new", 
						myinputDef, sourceID, 0, groupID, 
						"i_out", connectBusIndex
						] ++ sourceArgs);
				routine = Routine({
					var size;
					size = lim ? frameList.size;
					size.do({ arg i;
						var frame, bundle;
						frame = frameList[i];
						bundle = Array.new(frame.size);
						frame.do({ arg item;
							var amp;
							amp = item.at(0);
							bundle.add(
								["/s_new", 
								"system-OverlapAddResPartial", 
								server.nextNodeID,
								3,
								sourceID,
								"i_in", connectBusIndex,
								"i_out", busIndex + outfunc.value,
								"freq", item.at(1) * freqScal.poll,
								"amp", amp * mul.value,
								"ring", ring.poll,
								"frameTime", frameTime.poll * overlap.poll,
								"pos", 1.0.rand2
								]);
						});
						server.listSendBundle(latency, bundle);
						frameTime.poll.wait;
					});
					(overlap.poll * frameTime.poll).wait;
					env.release;
					endFunc.value;
					playing = false;
				}).play(clock);
				}, {
				sourceBundle = Array.new(maxFrameSize);
				maxFrameSize.do({ arg i; 
					var thisSourceID;
					thisSourceID = server.nextNodeID;
					sourceIDs.add(thisSourceID);
					sourceBundle.add(["/s_new", 
						myinputDef, thisSourceID, 0, groupID, 
						"i_out", connectBusIndex + i
						] ++ sourceArgs);
				});
				server.listSendBundle(nil, sourceBundle);
				routine = Routine({
					var size;
					size = lim ? frameList.size;
					size.do({ arg i;
						var frame, bundle;
						frame = frameList[i];
						bundle = Array.new(frame.size);
						frame.do({ arg item, j;
							var freq, amp;
							freq = item.at(1) * freqScal.poll;
							amp = item.at(0);
							bundle.add(
								["/s_new", 
								"system-OverlapAddResPartial", 
								server.nextNodeID,
								//3,
//								sourceIDs.array.at(i),
								2,
								envID,
								"i_in", connectBusIndex + j,
								"i_out", busIndex + outfunc.value,
								"freq", freq,
								"amp", amp,
								"ring", ring.poll,
								"frameTime", frameTime.poll * overlap.poll,
								"pos", 1.0.rand2
								]);
						});
						server.listSendBundle(latency, bundle);
						frameTime.poll.wait;
					});
					(overlap.poll * frameTime.poll).wait;
					env.release;
					endFunc.value;
					playing = false;
				}).play(clock);
			});
		}, {"Already Playing".warn; });
	}

	playBank { arg freqScal = 1, frameTime = 0.05, ring = 0.5, mul = 1, overlap = 3, out = 0, 
		endFunc, envSynth, lim, attack = 0.0, decay = 0.1, limit = 1 ... sourceArgs;
		var outfunc, sourceID;
		var sourceBundle, envID;
		if(numChan == 2, {outfunc = 0}, {outfunc = {(numChan - 1).rand}});
		playing.not.if({
			playing = true;
			CmdPeriod.add(this);
			decayTime = decay;
			group = Group.new(intarget, inaddAction);
			groupID = group.nodeID;
			// allocate private bus
			bus = Bus.audio(server, numChan);
			busIndex = bus.index;
			this.makeConnectBus(true);
			sourceIDs = Set.new;
			responder = OSCresponderNode(server.addr, "/n_end", 
				{arg msg, id; sourceIDs.remove(id)});
			responder.add;
			clock = TempoClock.new;
			env = envSynth ? Synth.tail(group, envDefName, 
				["i_out", out, "decay", decay, "attack", attack, "amp", mul, "i_in", bus.index, 
				"limit", limit]);
			envID = env.nodeID;
			singleSource.if({
				sourceID = server.nextNodeID;
				sourceIDs.add(sourceID);
				server.sendBundle(nil, 
						["/s_new", 
						myinputDef, sourceID, 0, groupID, 
						"i_out", connectBusIndex
						] ++ sourceArgs);
				routine = Routine({
					var size;
					size = lim ? frameList.size;
					size.do({ arg i;
						var frame, split, bundle;
						frame = frameList[i].scramble;
						split = Array.new(numChan);
						numChan.do({split.add(Array.new(bankSize))});
						bundle = Array.new(numChan * 4);
						frame.do({arg item, j; split.wrapAt(j).add(item)});
						split = split.scramble;
						split.do({ arg item, j;
							var flop, freqs, amps, thisSize, thisID;
							flop = item.flop;
							amps = flop.at(0);
							freqs = flop.at(1);
							thisSize = freqs.size;
							thisID = server.nextNodeID;
							bundle.add(
								["/s_new", 
								bankDefName, 
								thisID,
								3,
								sourceID,
								"i_in", connectBusIndex,
								"i_out", busIndex + j,
								"freq", freqScal.poll,
								"frameTime", frameTime.poll * overlap.poll
								//"pos", 1.0.rand2
								]);
																			bundle.add(
								["/n_setn",  
								thisID,
								"amps", thisSize] ++ amps
							);
							bundle.add(
								["/n_setn",  
								thisID,
								"freqs", thisSize] ++ freqs
							);
							bundle.add(
								["/n_setn",  
								thisID,
								"rings", thisSize] ++ Array.fill(thisSize, ring.value);
							);
						});
						server.listSendBundle(latency, bundle);
						frameTime.poll.wait;
					});
					(overlap.poll * frameTime.poll).wait;
					env.release;
					endFunc.value;
					playing = false;
					// should busses be freed here?
				}).play(clock);
				}, {
				sourceBundle = Array.new(numChan);
				numChan.do({ arg i; 
					var thisSourceID;
					thisSourceID = server.nextNodeID;
					sourceIDs.add(thisSourceID);
					sourceBundle.add(["/s_new", 
						myinputDef, thisSourceID, 0, groupID, 
						"i_out", connectBusIndex + i
						] ++ sourceArgs);
				});
				server.listSendBundle(nil, sourceBundle);
				routine = Routine({
					var size;
					size = lim ? frameList.size;
					size.do({ arg i;
						var frame, split, bundle;
						frame = frameList[i].scramble;
						split = Array.new(numChan);
						numChan.do({split.add(Array.new(bankSize))});
						bundle = Array.new(numChan * 4);
						frame.do({arg item, j; split.wrapAt(j).add(item)});
						split = split.scramble;
						split.do({ arg item, j;
							var flop, freqs, amps, thisSize, thisID;
							flop = item.flop;
							amps = flop.at(0);
							freqs = flop.at(1);
							thisSize = freqs.size;
							thisID = server.nextNodeID;
							bundle.add(
								["/s_new", 
								bankDefName, 
								thisID,
								//3,
//								sourceIDs.array.at(i),
								2,
								envID,
								"i_in", connectBusIndex + j,
								"i_out", busIndex + j,
								"freq", freqScal.poll,
								"frameTime", frameTime.poll * overlap.poll
								//"pos", 1.0.rand2
								]);
						
							bundle.add(
								["/n_setn",  
								thisID,
								"amps", thisSize] ++ amps
							);
							bundle.add(
								["/n_setn",  
								thisID,
								"freqs", thisSize] ++ freqs
							);
							bundle.add(
								["/n_setn",  
								thisID,
								"rings", thisSize] ++ Array.fill(thisSize, ring.value);
							);
						});
						server.listSendBundle(latency, bundle);
						frameTime.poll.wait;
					});
					(overlap.poll * frameTime.poll).wait;
					env.release;
					endFunc.value;
					playing = false;
					// should busses be freed here?
				}).play(clock);
			});
		}, {"Already Playing".warn; });
	}
		
	// this currently does not allow a new play until release is finished.
	release { arg time;
		var oldbus, oldConnectBus, oldclock, oldSourceIDs, releaseTime;
		playing.if({ 
			playing = false;
			CmdPeriod.remove(this);
			releaseTime = time ? decayTime;
			server.sendBundle((releaseTime) + 0.05, group.freeMsg);
			group = nil;
			env.release(releaseTime);
			env = nil;
			oldbus = bus; bus = nil;
			oldConnectBus = connectBus; oldConnectBus = nil;
			oldclock = clock; 
			clock = nil; 
			responder.remove;
			oldSourceIDs = sourceIDs;
			sourceIDs = nil;
			SystemClock.sched(releaseTime - latency - 0.05, {oldclock.stop; oldSourceIDs.clear;});
			SystemClock.sched(releaseTime + 0.05, {oldbus.free; oldConnectBus.free});
		}, {"Not Playing".warn;});
	}
	
		// call this before object is garbage collected
	free {
		freed.not.if({
			freed = true;
			playing.if({this.release(0.0); });
		}, { "Already Freed".error });
	}
	
	cmdPeriod { this.release(0.0); }
}

OverlapAddBWPlayer : OverlapAddPlayer {
	
	init { arg list, numChannels, target, addAction;
		frameList = list;
		numChan = numChannels;
		server = target.asTarget.server;
		intarget = target;
		inaddAction = addAction;
		envDefName = "system-OverlapAddPlayerEnv" ++ numChan;
		SynthDef(envDefName, {
			arg i_out=0, decay=0.1, amp = 1.0, gate = 1, i_in, attack;
			var input, output;
			input = In.ar(i_in, numChannels);
			output = input * EnvGen.kr(Env.asr(attack, 1.0, decay), gate, amp, 0, 1.0, 7);
			// free the nodes in the group when released
			Out.ar(i_out, output);
		}).send(server);
		maxFrameSize = frameList.maxItem({arg item; item.size}).size;
		
//		("maxFrameSize:" + maxFrameSize).postln;
//		bankSize = maxFrameSize / numChannels;
//		if(bankSize.frac >= 0.5, { bankSize = bankSize.ceil; }, {bankSize = bankSize.floor});
//		postln("bankSize:" + bankSize);
//		bankDefName = "system-OverlapAddBankPartial" ++ bankSize;
//		SynthDef(bankDefName, { arg i_out, freq, frameTime;
//			var bank, freqs, amps, bws, phases;
////			osc = FSinOsc.ar(freq, 0, amp) * EnvGen.kr(Env.sine(1,1), 1.0, amp, 0, frameTime, 2);
//			freqs = Control.names([\freqs]).ir(Array.fill(bankSize, 100.0));
//			amps = Control.names([\amps]).ir(Array.fill(bankSize, 0.0));
//			phases = Array.fill(bankSize, 0.0);
//			bank = Klang.ar(`[freqs, amps, phases], freq)
//				* EnvGen.kr(Env.sine(1,1), 1.0, 1.0, 0, frameTime, 2); 
//			//Out.ar(i_out, Pan2.ar(bank, pos));
//			Out.ar(i_out, bank);
//		}).send(server);
	}
	
	*initClass {
		SynthDef.writeOnce("system-OverlapAddBWPartial", { arg i_out, freq, amp, bw, fT, 
			pos;
			var osc;
			osc = BEOsc.ar(freq, 0, bw, EnvGen.ar(Env.sine(1,1), 1.0, amp, 0, fT, 2));
			Out.ar(i_out, Pan2.ar(osc, pos));
		});
	}
	
	play { arg freq = 1, frameTime = 0.05, bw = 1, mul = 1, overlap = 3, out = 0, endFunc, 
			envSynth, lim, attack = 0.0, decay = 0.1;
		var outfunc;
		if(numChan == 2, {outfunc = 0}, {outfunc = {(numChan - 1).rand}});
		playing.not.if({
			playing = true;
			CmdPeriod.add(this);
			decayTime = decay;
			group = Group.new(intarget, inaddAction);
			groupID = group.nodeID;
			bus = Bus.audio(server, numChan);
			busIndex = bus.index;
			clock = TempoClock.new;
			env = envSynth ? Synth.tail(group, envDefName, 
				["i_out", out, "decay", decay, "attack", attack, "amp", 1, "i_in", busIndex]);
			routine = Routine({
				var size;
				size = lim ? frameList.size;
				size.do({ arg i;
					var frame, bundle;
					frame = frameList[i];
					bundle = Array.new(frame.size);
					frame.do({ arg item;
						var amp;
						amp = item.at(0);
						bundle.add(
							["/s_new", 
							"system-OverlapAddBWPartial", 
							server.nextNodeID,
							0,
							groupID,
							"i_out", busIndex + outfunc.value,
							"freq", item.at(1) * freq.poll,
							"bw", item.at(2) * bw.poll,
							"amp", amp * mul.value,
							"fT", frameTime.poll * overlap.poll,
							"pos", 1.0.rand2
							]);
					});
					server.listSendBundle(latency, bundle);
					frameTime.poll.wait;
				});
				(overlap.poll * frameTime.poll).wait;
				env.release;
				endFunc.value;
				playing = false;
			}).play(clock);
		}, {"Already Playing".warn; });
	}
}	
// This version clips the output at a fraction of amp
//OverlapAddDistorter : OverlapAddPlayer {
//
//	*initClass {
//		SynthDef.writeOnce("system-OverlapAddDistPartial", { arg i_out, freq, amp, frameTime, pos, 
//				clip = 1;
//			var osc;
//			osc = FSinOsc.ar(freq, 0, amp).clip2(amp * clip) * EnvGen.kr(Env.sine(1,1), 1.0, 1.0, 0, frameTime, 2);
//			Out.ar(i_out, Pan2.ar(osc, pos));
//		});
//	}
//
//	play { arg freqScal = 1, clip = 0.8, frameTime = 0.05, mul = 1, overlap = 3, out = 0, endFunc, 
//			envSynth, lim, decay = 0.1;
//		var outfunc;
//		if(numChannels == 2, {outfunc = 0}, {outfunc = {numChannels.rand}});
//		playing.not.if({
//			playing = true;
//			clock = TempoClock.new;
//			env = envSynth ? Synth.tail(group, envDefName, 
//				["i_out", out, "decay", decay, "amp", mul, "i_in", bus.index]);
//			routine = Routine({
//				var size;
//				size = lim ? frameList.size;
//				size.do({ arg i;
//					var frame, bundle;
//					frame = frameList[i];
//					bundle = Array.new(frame.size);
//					frame.do({ arg item;
//						var amp;
//						amp = item.at(0);
//						bundle.add(
//							["/s_new", 
//							"system-OverlapAddDistPartial", 
//							server.nextNodeID,
//							0,
//							groupID,
//							"i_out", busIndex + outfunc.value,
//							"freq", item.at(1) * freqScal.poll,
//							"amp", amp,
//							"frameTime", frameTime.poll * overlap.poll,
//							"pos", 1.0.rand2,
//							"clip", clip.poll
//							]);
//					});
//					server.listSendBundle(latency, bundle);
//					frameTime.poll.wait;
//				});
//				(overlap.poll * frameTime.poll).wait;
//				env.release;
//				endFunc.value;
//				playing = false;
//			}).play(clock);
//		}, {"Already Playing".warn; });
//	}
//
//}