// Overlap add style listplayers for SC3 Created Toronto Dec 19/03
// adapted from SC2 code

// intended for lists of the following format:
/**

#[
[[amp, freq, bw], [amp, freq, bw], [amp, freq, bw]],	//one frame with three bands
[[amp, freq, bw], ...]
...
]
*/


// Could make it possible to swap in different lists, but would need to reallocate connectBus in Res version

// Could add in phase arg and still make it compatible with phaseless lists

// could have a source group and use free

// free messages unecessary? or should be changed to handle CmdPeriod elegantly?

LorisOAPlayer {
	var server, frameList, routine, playing = false, envDefName, numChan, <>latency = 0.05;
	var group, groupID, bus, <busIndex, <env, <clock, freed = false, maxFrameSize, bankSize;
	var intarget, <>intarget, <>inaddAction, decayTime, <>freqThresh = 20;
	var <>def, >outfunc;
	var releasing = false;
	
	//var >test = false;
	
	*new { arg list, numChannels = 2, target = nil, addAction = \addToHead, 
			def = "system-OverlapAddBWPartial", outfunc;
		^super.new.init(list, numChannels, target, addAction, def, outfunc);
	}
	
	init { arg list, numChannels, target, addAction, argdef, argoutfunc;
		def = argdef;
		outfunc = argoutfunc ? if(numChan == 2, {0}, { {(numChan - 1).rand} });
		frameList = list;
		numChan = numChannels;
		server = target.asTarget.server;
		intarget = target;
		inaddAction = addAction;
		envDefName = "system-LorisOAPlayerEnv" ++ numChan;
		SynthDef(envDefName, {
			arg i_out=0, decay=0.1, amp = 1.0, gate = 1, i_in, attack;
			var input, output;
			input = In.ar(i_in, numChannels);
			output = input * EnvGen.kr(Env.asr(attack, 1.0, decay), gate, amp, 0, 1.0, 14);
			// free the nodes in the group when released
			Out.ar(i_out, output);
		}).send(server);
		maxFrameSize = frameList.maxItem({arg item; item.size}).size;
		
	}
		
	*initClass {
		SynthDef.writeOnce("system-OverlapAddBWPartial", { arg i_out, freq, amp, bw, fT, 
			pos;
			var osc;
			osc = BEOsc.ar(freq, 0, bw, EnvGen.ar(Env.sine(1,1), 1.0, amp, 0, fT, 2));
			Out.ar(i_out, Pan2.ar(osc, pos));
		});
	}
	
	play { arg pitch = 1, frameTime = 0.05, bw = 1, mul = 1, overlap = 3, out = 0, endFunc, 
			envSynth, lim, attack = 0.0, decay = 0.1, targetArgs; // named for consistency
		var startBund, thisDef;
		
		playing.not.if({
			playing = true;
			thisDef = def;
			CmdPeriod.add(this);
			decayTime = decay;
			startBund = server.makeBundle(false, {
				group = Group.new(intarget, inaddAction);
				groupID = group.nodeID;
				bus = Bus.audio(server, numChan);
				busIndex = bus.index;
				clock = TempoClock.new;
				env = envSynth ? Synth.tail(group, envDefName, 
					["i_out", out, "decay", decay, "attack", attack, "amp", 1, "i_in", 
						busIndex]);
			});
			routine = Routine({
				var size, thisFrameTime, thisOverlap;
				size = lim ? frameList.size;
				size.do({ arg i;
					var frame, bundle, testbw;
					frame = frameList[i];
					bundle = startBund ? Array.new(frame.size);
					thisFrameTime = frameTime.poll;
					thisOverlap = overlap.poll;
					frame.do({ arg item;
						var amp, freq;
						amp = item.at(0);
						freq = item.at(1) * pitch.poll;
						if(freq > freqThresh, {
							bundle = bundle.add(
								["/s_new", 
								thisDef.poll.asString, 
								server.nextNodeID,
								0,
								groupID,
								"i_out", busIndex + outfunc.value,
								"freq", freq,
								"bw", testbw = item.at(2) * bw.poll,
								"amp", amp * mul.value,
								"fT", thisFrameTime * thisOverlap,
								"pos", 1.0.rand2
								] ++ targetArgs.value);
							//testbw.isNaN.if({\ack.postln});
							//test.if({testbw.postln;});
						});
					});
					server.listSendBundle(latency, bundle);
					startBund = nil; // only needed first time
					thisFrameTime.wait;
				});
				
				(thisFrameTime * thisOverlap - thisFrameTime + latency).wait;
				releasing.not.if({this.release(0);});
				endFunc.value;
				
			}).play(clock);
		}, {"Already Playing".warn; });
	}

		
	// this currently does not allow a new play until release is finished.
	release { arg time;
		var oldbus, oldclock, releaseTime;
		
		(playing && releasing.not).if({ 
			releasing = true;
			playing = false;
			CmdPeriod.remove(this);
			releaseTime = time ? decayTime;
			//server.sendBundle((releaseTime) + 0.05, group.freeMsg);
			group = nil;
			env.release(releaseTime);
			env = nil;
			oldbus = bus; bus = nil;
			oldclock = clock; 
			clock = nil; 
			SystemClock.sched(releaseTime - latency - 0.05, {oldclock.stop;});
			SystemClock.sched(releaseTime + 0.05, {oldbus.free; releasing = false});
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
