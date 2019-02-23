// need a way for iterator to tell bpcmd whether it stopped automatically or is manually killed

// setcmd doesn't handle synthdesc right

// h. james harkins - jamshark70@dewdrop-world.net
// not ready for prime time!

// important safety tip: setDoneSignal should not populate the doneSignal variable in a deferred function

var	saveSubType = AbstractChuckArray.defaultSubType;

AbstractChuckArray.defaultSubType = \tlCmd;

protect {

Proto({
	// subclasses should have methods
	// ~prep
	// ~fire
	// ~done
	// ~setDoneSignal (optional)
	// ~clearDoneSignal (optional)
	// ~free (optional)
	
	~isRunning = false;
	
		// parameters that one might want to override must be included in parentKeys below
	~isTLCommand = true;
		// if true, this command will be included in \sync or \cmdSync instructions
	~shouldSync = true;
	
	~play = { |parms|
//thisThread.clock.beats.debug(">> %: play".format(~collIndex));
		if(~isRunning.not) {
			~setParms.(parms);
			~isRunning = true;
//~setDoneSignal.postcs;
//"calling setDoneSignal".debug;
			~setDoneSignal.(parms);
//"calling fire".debug;
			~fire.(parms);
//			currentEnvironment.changed(\play, parms);
			NotificationCenter.notify(currentEnvironment, \play, [parms]);
			~schedDone.(parms);
		};
//thisThread.clock.beats.debug("<< abstractcmd: play");
		currentEnvironment
	};
	~schedDone = { |parms|
		if(~doneSignal.isNil) {
			thisThread.clock.sched((~dur.value(parms) ? 0), e { ~stop.value });
		};
	};
	~stop = { |parms|
		var	notifyTime;

//currentEnvironment.env.debug("abstractcmd: stop");

		if(~isRunning) {
			~done.(parms);
			~clearDoneSignal.(parms);
//"setting isRunning false".debug;
			~isRunning = false;
//"\nsending done notification".debug;
//currentEnvironment.listVars;

			if((notifyTime = parms.tryPerform(\at, \notifyTime)).notNil) {
				thisThread.clock.schedAbs(notifyTime, e {
					NotificationCenter.notify(currentEnvironment, \done, [parms]);
				});
			} {
				NotificationCenter.notify(currentEnvironment, \done, [parms]);
			};
//			currentEnvironment.changed(\done, parms);
		};
		currentEnvironment
	};
	~setParms = { |parms|
		var	e = currentEnvironment.env;
		if(e.proto.isNil) {
			e.proto = ();
		};
		parms.respondsTo(\keysValuesDo).if({
			e.proto.putAll(parms);
		});
	};
}, parentKeys: #[isTLCommand, shouldSync]) => PR(\abstractTLCommand);

PR(\abstractTLCommand).clone({
	~id = 0;
	~fire = { |parms|
		thisThread.clock.beats.debug("firing %".format(~id));
		currentEnvironment
	};
	~done = { |parms|
		thisThread.clock.beats.debug("done %".format(~id));
		currentEnvironment
	};
}, parentKeys: #[id]) => PR(\testTLCmd);

// run an arbitrary function
PR(\abstractTLCommand).clone({
	~fire = { |parms|
//		~setParms.(parms);
		currentEnvironment.env.use({ ~func.valueEnvir });
	};
}) => PR(\funcCmd);

PR(\abstractTLCommand).clone({
	~fire = { |parms|
//		~setParms.(parms);
		var	schedTime;
		~server ?? { ~server = Server.default };
		~node = Synth.basicNew(~name, ~server, ~id);
		~id = ~node.nodeID;		// nil is replaced in basicNew
		~latency ?? { ~latency = ~server.latency };
		~args = ~getArgs.value(parms);
		~quant ?? { ~quant = 0 };
		~schedTime = ~quant.nextTimeOnGrid(thisThread.clock);
		thisThread.clock.schedAbs(~schedTime, e {
			if(~latency == 0) {
				s.sendBundle(nil, ~node.newMsg(~target, ~args, ~addAction ? \addToHead))
			} {
				s.sendBundle(~latency, ~node.newMsg(~target, ~args, ~addAction ? \addToHead))
			};
		});
		NodeWatcher.register(~node, true);  // true == assumePlaying
		~node.addDependant(currentEnvironment);
	};
	~getArgs = { |parms|
		var	result;
		~synthDesc ?? {
			~synthDesc = (~descLib ?? SynthDescLib.global).at(~name.asSymbol);
		};
		~synthDesc.notNil.if({
			currentEnvironment.env.use({ ~synthDesc.msgFunc.valueEnvir });
		}, {
			parms.tryPerform(\keysValuesDo, { |key, value|
				if(value.isValidUGenInput) {
					result = result.add(key).add(value);
				};
			});
			result
		});
	};
	~schedDone = { |parms|
		if(~dur.notNil and: { ~synthDesc.tryPerform(\hasGate) ? false }) {
//"synthCmd scheduling release".debug;
			thisThread.clock.schedAbs(~schedTime + ~dur, e { ~stop.(parms) });
		};	// else -- node had better end on its own and trigger ~update
	};
	~done = { |parms|
		var	msg;
//">> synthCmd:done".debug;
		if(~node.isPlaying) {
//"sending release message".debug;
			if(~synthDesc.notNil and: { ~synthDesc.hasGate }) {
				msg = ~node.setMsg(\gate, 0);
			} {
				msg = ~node.freeMsg;
			};
			if(~latency != 0) {
				s.sendBundle(~latency, msg);
			} {
				s.sendBundle(nil, msg);
			};
		};
		~node.removeDependant(currentEnvironment);
//"<< synthCmd:done".debug;
	};
	~update = { |obj, msg|
		if(obj == ~node and: { msg == \n_end }) {
			~stop.value;
		};
	};
	~isSynth = true;
}) => PR(\synthCmd);

PR(\abstractTLCommand).clone({
	~fire = { |parms|
		var	synthDesc;
//		~setParms.(parms);
		~nodes = ~iterator.activeCmds.select({ |cmd|
			cmd.tryPerform(\isSynth) ? false
		});
		if(~nodes.size > 0) {
			~nodes.do({ |node|
				synthDesc = node.synthDesc;
				~args = ~getArgs.(parms);
				if(node.latency == 0) {
					node.node.set(*~args);
				} {
					node.server.sendBundle(node.latency, node.node.setMsg(*~args))
				};
			});
		};
		~dur = 0;
	};
}).import((synthCmd: #[getArgs, setParms])) => PR(\setCmd);


// Pdef player

PR(\abstractTLCommand).clone({
	~quant = Quant(0);
	~fire = { |parms|
//		~setParms.(parms);
		~name = ~name.asArray;
		~pdefs = Array(~name.size);
		~players = Array(~name.size);
		~name.do({ |name|
			if(Pdef(name).isActive) {
				"Pdef(%) is already playing; ignored".format(name.asCompileString).warn;
			} {
				~pdefs.add(Pdef(name));
				if(Pdef(name).clock !== thisThread.clock) {
					Pdef(name).clock = thisThread.clock;
				};
					// user hook to manipulate Pdef before play
				~preparePdef.(Pdef(name));
//name.debug("Pdefcmd - playing");
//[thisThread.clock.beats, Pdef(name).eventSchedTime(~quant)].debug("now, go time");
//~quant.dump;
				Pdef(name).play(thisThread.clock, ~protoEvent, ~quant, ~doReset ? false);
				~players.add(Pdef(name).player);
			};
		});
		if(~waitForStop == nil) {
			~waitForStop = ~pdefs.collect(_.key);
		} {
				// could be immutable as input
			~waitForStop = ~waitForStop.copy;
		};
	};
	~schedDone = { |parms|
		var	stopTime;
			// nothing played
		if(~pdefs.size == 0) {
			~stop.value;
		} {
			~pdefs.do({ |pdef| pdef.player.addDependant(currentEnvironment) });
			if(~dur.isNumber and: { ~dur.isStrictlyPositive }) {
				stopTime = thisThread.clock.beats + ~dur;
				thisThread.clock.sched(~dur - 0.001, e {
					~stop.value((notifyTime: stopTime));
				});
			};
		};
	};
	~done = { |parms|
			// if iterator stops automatically and this command should NOT sync,
			// then we don't want the pdefs to stop
//[~name, parms].debug("Pdefcmd:done");
		if(~shouldSync or: { parms.tryPerform(\at, \manualStop) == true }) {
//"stopping pdefs".debug;
				// pdefs will be removed from this array on each stopNow
				// so copying the array is necessary
				// also at end, this process might be responsible for stopping everything
			if(~stopAll == true, { Pdef.all }, { ~pdefs.copy })
			.do({ |pdef| pdef.stop });
		}
	};
	~update = { |obj, what, how|
		var	index;
//[obj, what, how].debug("Pdefcmd got update");
		if(what == \stopped and: { (index = ~players.indexOf(obj)).notNil }) {
				// user hook to reset Pdef after completion
			~pdefs[index].stop;
			~resetPdef.(obj);
				// the Pdef has already forgotten about its player
				// but I haven't
			~players[index].removeDependant(currentEnvironment);
			~waitForStop.remove(~pdefs[index].key);
			~pdefs.removeAt(index);
			~players.removeAt(index);
			if(~waitForStop.size == 0) {
				~stop.value;
			};
		};
	};	
}) => PR(\pdefCmd);


// bp command

PR(\abstractTLCommand).clone({
	~quant = NilTimeSpec.new;
	~fire = { |parms|
//		~setParms.(parms);
		~name = ~name.asArray;
		~bps = Array(~name.size);
		~name.do({ |name|
			if(BP(name).isPlaying) {
				"BP(%) is already playing; ignored".format(name.asCompileString).warn;
			} {
				~bps.add(BP(name));
				if(BP(name).clock !== thisThread.clock) {
					thisThread.clock => BP(name);
				};
					// user hook to manipulate BP before play
				~prepareBP.(BP(name));
//name.debug("bpcmd - playing");
//[thisThread.clock.beats, thisThread.clock.baseBarBeat, BP(name).eventSchedTime(~quant)].debug("now, basebar, go time");
//~quant.dump;
				thisThread.clock.schedAbs(BP(name).eventSchedTime(~quant), e {
						// if not running, cmd was stopped before BP fired
						// so skip the .play
					if(~isRunning) {
//[~name, thisThread.clock.beats].debug("playing bps now");
						BP(name).play(NilTimeSpec.new, nil, doReset: ~doReset ? false);
					} {
						BP(name).removeDependant(currentEnvironment);
					};
				});
			};
		});
		if(~waitForStop == nil) {
			~waitForStop = ~bps.collect(_.collIndex);
		} {
				// could be immutable as input
			~waitForStop = ~waitForStop.copy;
		};
	};
	~schedDone = { |parms|
		var	stopTime;
			// nothing played
		if(~bps.size == 0) {
			~stop.value;
		} {
			~bps.do({ |bp| bp.addDependant(currentEnvironment) });
			if(~dur.isNumber and: { ~dur.isStrictlyPositive }) {
				stopTime = thisThread.clock.beats + ~dur;
				thisThread.clock.sched(~dur - 0.001, e {
					~bps.copy.do({ |bp| bp.stopNow(notifyTime: stopTime); });
				});
			};
		};
	};
	~done = { |parms|
			// if iterator stops automatically and this command should NOT sync,
			// then we don't want the bps to stop
		var	timeSpecForStop = if(~stopAll != true) { NilTimeSpec.new };
//[~name, parms].debug("\n\nbpcmd:done");
//thisThread.dumpBackTrace;
		if(~shouldSync or: { parms.tryPerform(\at, \manualStop) == true }) {
//"stopping bps".debug;
				// bps will be removed from this array on each stopNow
				// so copying the array is necessary
				// also at end, this process might be responsible for stopping everything
			if(~stopAll == true, { BP.all }, { ~bps.copy })
			.do({ |bp| bp.stopNow(quant: timeSpecForStop) });
		}
	};
	~update = { |obj, what, how|
//[obj.tryPerform(\collIndex), what, how].debug("bpcmd got update");
		if(what == \stop and: { how == \stopped and: { ~bps.includes(obj) } }) {
				// user hook to reset BP after completion
			~resetBP.(obj);
			~bps.remove(obj);
			~waitForStop.remove(obj.collIndex);
			obj.removeDependant(currentEnvironment);
			if(~waitForStop.size == 0) {
				~stop.value;
			};
		};
	};	
}, parentKeys: #[quant]) => PR(\bpCmd);

// this command triggers one event in the registered bp's
// it must be non-syncing and have a cleanup function saved in ~done
PR(\abstractTLCommand).clone({
	~shouldSync = false;
	~fire = { |parms|
//		~setParms.(parms);
		~name = ~name.asArray;
		~bps = Array(~name.size);
		~name.do({ |name|
			if(BP(name).isPlaying) {
				"BP(%) is playing; can't trigger one event".format(name.asCompileString).warn;
			} {
				~bps.add(BP(name));
				BP(name).addDependant(currentEnvironment);
				if(BP(name).clock !== thisThread.clock) {
					thisThread.clock => BP(name);
				};
					// user hook to manipulate BP before play
				~prepareBP.(BP(name));
				thisThread.clock.schedAbs(BP(name).eventSchedTime(~quant), {
					BP(name).triggerOneEvent(NilTimeSpec.new, nil, doReset: ~doReset ? false);
				});
			};
		});
	};
	~done = { |parms|
		~cleanup.(parms);
		if(parms.tryPerform(\at, \manualStop) ? false) {
			~bps.do(_.stopNow);
		};
		~bps.do({ |bp| bp.removeDependant(currentEnvironment) });
	};
	~update = { |obj, what, how|
//[obj.tryPerform(\collIndex), what, how].debug("bp1eventCmd got update");
		obj.removeDependant(currentEnvironment);
		switch(what)
				// triggerOneEvent failed
			{ \oneEventEmpty } { ~stop.value }
				// somebody else started playing the process - this guy is dead now
			{ \play } { ~stop.value };
	};	
}, #[shouldSync]) => PR(\bp1eventCmd);

PR(\abstractTLCommand).clone({
	~fire = { |parms|
//		~setParms.(parms);
		thisThread.clock.setMeterNextBar(~beatsPerBar);
		~stop.();
	};
}) => PR(\meterCmd);

// REALLY experimental: flow-of-control commands

// looping over a whole array
// how to stop: same as others, set a user signal or specify a duration

PR(\abstractTLCommand).clone({
	~fire = { |parms|
//		~setParms.(parms);
		~myIterator = TLSequenceIterator(~cmds, autoSync: true);
			// looping works by receiving "stop" signal, then restarting upon that signal
		~myIterator.addDependant(currentEnvironment);
		~myIterator.play;
	};
	~done = { |parms|
		~myIterator.stop;
	};
	~update = { |obj, what|
		if((~isRunning ? false) and: { what == \done }) {
			"got done notification; looping back".postln;
			~myIterator.play;
		};
	};
}) => PR(\loopCmd);

// send user-defined notification from the iterator
PR(\abstractTLCommand).clone({
	~fire = { |parms|
//[~iterator, parms.atBackup(\notification, currentEnvironment), parms].debug("notifyCmd");
		parms[\iterator].changed(parms.atBackup(\notification, currentEnvironment),
			parms ?? ~parms);
	};
}) => PR(\notifyCmd);


// SECTION object
// responsible for saving bp states and selecting sequences (if needed) by user input

Proto({
	~name = "";		// to display in GUI
	~autoRestoreStates = true;
	~bps = nil;	// fill in with an array of BP names (symbols)
	~status = \idle;
	~autoSync = true;	// passed into TLSequenceIterator

	~isLoaded = false;

		// parent will call this one
	~prInitProc = {
		if(~isLoaded.not) {
			~initProcesses.();
			~isLoaded = true;
		};
	};
	
	~prUnload = {
		if(~isLoaded) {
			~unloadProcesses.();
			~isLoaded = false;
		};
	};

	~initProcesses = nil;		// user-supplied function to load resources
	~unloadProcesses = nil;

	~play = { |savedCmds|
		if(~isRunning.().not) {
			~savedCmds = savedCmds;
//savedCmds.debug("section:play - savedcmds");
			~clock ?? { ~clock = TempoClock.default };
			~initStates.();
			~preparePlay.();
			~seqPlayer = ~makeSeqPlayer.();
			~clock.sched(0, ~seqPlayer);
			~status = \running;
			currentEnvironment.changed(\play);
			~schedDone.();
		};
	};
	~schedDone = {
		var	now = thisThread.clock.beats;
		if(~dur.notNil) {
			~clock.sched(~dur - 0.001, e { ~stop.(now + ~dur) });
		}	// else, there should be a user input trigger to move to the next section
	};
	~stop = { |notifyTime, manualStop = true|
		if(~isRunning.()) {
			~status = \idle;
			~currentIterator.stop((manualStop: manualStop ? true));
			~done.();
			~clearDoneSignal.();
			~clock.schedAbs((notifyTime ?? { ~clock.beats }), e {
//~clock.beats.debug("sending done notification at");
//~savedCmds.debug("with these saved commands");
				currentEnvironment.changed(\done, ~savedCmds);
			});
			if(thisThread === ~seqPlayer) {
				nil.yield
			} {
				~seqPlayer.stop;
			};
		};
		currentEnvironment
	};
	~done = {
	};
	
	~isRunning = { ~status != \idle };
	
//	~saveStates = {
//		var	bps;
//		bps = ~bps ?? { BP.keys };
//		~states ?? { ~states = IdentityDictionary.new };
//		bps.do({ |bp|
//			~states.put(bp, BP(bp).archiveState);
//		});
//	};
//	~restoreStates = {
//		if(~states.notNil) {
//			~states.keysValuesDo({ |bpname, archive|
//				BP(bpname).restoreArchive(archive);
//			})
//		};
//	};
//	
//	~initStates = {
//		if(~states.isNil) {
//			~saveStates.()
//		} {
//			if(~autoRestoreStates) {
//				~restoreStates.();
//			};
//		};
//	};

	~makeSeqPlayer = {
//		~seqStream ?? { ~seqStream = ~seqPattern.().asStream };
			// should always reset: start from beginning of section every time you play
		~seqStream = ~seqPattern.().asStream;
			// ~condition is used elsewhere, not here, but needs to exist
		~condition ?? { ~condition = Condition.new };
		Routine({ |inval|
			var	item, delta;
			loop {
				item = ~seqStream.next(~seqEventProto);
				if(item.notNil) {
					delta = ~playItem.(item);
					delta.yield;	// playItem returns time to wait
				} {
					~stop.(nil, false)
				};
			}
		})
	};
	
	~update = { |obj, what, args|
//[obj, what, args].debug("section % received update".format(~name));
		if(obj === ~currentIterator) {
			switch(what)
				{ \done } {
					obj.removeDependant(currentEnvironment);
						// iterator might remove commands after this happens
					~savedCmds = args.copy;
//args.debug("section:update - savedcmds");
//~savedCmds.debug("savedCmds variable");
					if(~status == \sync) { ~condition.unhang };
				}
		};
			// a command might send a notification that's really meant for the section sequencer
			// it can set the passthru flag
			// try returns nil if there's an error
		if(try { args[\passthru] } == true) {
//"passing notification thru".debug;
			currentEnvironment.changed(what, args)
		};
	};
	
	~playItem = { |item|
		var	seq = item[\sequence], dur = item[\dur],
			iterator, /*updater, */quant;
		if(seq.size > 0) {
			~currentIterator = iterator = TLSequenceIterator(seq, currentEnvironment,
				autoSync: ~autoSync).play(0, ~clock, ~savedCmds);
			~savedCmds = nil;
//"section playItem: set savedCmds to nil".debug;
			iterator.addDependant(currentEnvironment);
		};
		case
			{ dur.isNumber and: { dur >= 0.001 } } {
				~clock.sched(dur - 0.001, {
					iterator.removeDependant(currentEnvironment);
					iterator.stop;
				});
				dur	// return value
			}
				// cannot sync if there's nothing to play
			{ dur == \sync and: { iterator.notNil } } {
				~status = \sync;
				~condition.hang;
				~status = \running;
				0	// after waking up, go immediately
			}
			{ dur == \trigger } {
				~setTrigger.(item);
				~status = \waitForTrigger;
				~condition.hang;
				~status = \running;
				item[\quant].asQuant.nextTimeOnGrid(~clock) - ~clock.beats
//				0	// after waking up, go immediately
			}
			{ (quant = dur.tryPerform(\asQuant)).notNil } {
				quant = quant.nextTimeOnGrid(~clock) - ~clock.beats;
				~clock.sched(quant - 0.001, {
					iterator.removeDependant(currentEnvironment);
					iterator.stop;
				});
				quant
			}
			{ seq == \initNext } {
				currentEnvironment.changed(\initNext);
				dur
			}
				// default case: invalid object - stop rather than loop infinitely
			{
				MethodError("Invalid item in section", item).throw
			}
	};

	~setTrigger = { |item|
		if(~currentTriggerItem.isNil) {
			~currentTriggerItem = item;
			item[\init].(item);
		} {
			"Another trigger is already active. Ignored.".warn;
		};
	};
	~cleanupTrigger = { |triggerID, parms|
		if(~currentTriggerItem.notNil) {
			~currentIterator.stop;
			~currentTriggerItem[\clear].(~currentTriggerItem, triggerID, parms);
			~currentTriggerItem = nil;
		};
	};
	~doTrigger = { |triggerID, parms|
		if(~status == \waitForTrigger) {
			if(~receivedTrigger.(triggerID, parms)) {
				~status = \running;
				~cleanupTrigger.(triggerID, parms);
				~condition.unhang;
			} {
				"Received inactive trigger %; ignored.".format(triggerID).warn;
			};
		} {
			"Section is not waiting for user trigger. Cannot ~doTrigger now.".warn;
		};
	};
	~receivedTrigger = { |triggerID, parms|
		~currentTriggerItem[\id].matchItem(triggerID)
			and: { ~currentTriggerItem[\verify].(~currentTriggerItem, parms) ? true }
	};
		// SEQUENCE SELECTION
		// up to you to decide how to store sequences
		// pattern must return events (i.e., you can use Pbind) with
		// (sequence: array for TLSeqIt, dur: timing info)
		// sequence = nil means pause based on timing
		// dur = number, run sequence for exactly dur beats (Quant OK too)
		// dur = \sync, resume section when sequence stops
		// dur = \trigger (or nil), wait for some user signal - user's responsibility to maintain
	~seqPattern = {};	// you supply!
	~seqEventProto = ();
}) => PR(\tlSection);

Proto({
		// should send .changed(\section, sectionIndex) when moving to a new section

	~sections = [];	// supply array of sections
	~status = \idle;
	
	~currentSection = 0;
	
	~countOffBeats = 4;
	~clock = TempoClock.default;
	~quant = Quant(1);	// start (to count off) on next whole bar
	~sectionLeadTime = 0.9;	// how many beats before barline to kick off the section
	
	~autoReset = true;	// automatically reset all BPs?
	~doNotReset = nil;	// collection of bp names NOT to reset
	
	~condition = Condition.new;
	
	~isRunning = { ~status != \idle };
	
	~recvPlayButton = { |section|
		if(~isRunning.()) { ~stop.() }
			{ ~currentSection = section ?? { ~currentSection }; ~play.() };
	};
	
	~play = {
//">> sectionSequencer:play".debug;
		if(~autoReset) {
			~doReset.();
		};
		~startingSection = ~currentSection;
		~loadResources.(~currentSection);
		~countOffAndGo.();
//		~status = \playing;
		currentEnvironment.changed(\play);
//"<< sectionSequencer:play".debug;
	};
	
	~stop = { |lastSection = false|
//lastSection.debug(">> sectionSequencer:stop");
		if(~status == \countingOff) {
			~status = \idle;
			~countOffRoutine.stop;
			currentEnvironment.changed(\stop);
			currentEnvironment.changed(\countoff, nil);
		} {
			~sections[~currentSection].stop/*(nil, manualStop)*/;
				// must force manual stop for last section at end of piece
				// but this didn't work
			if(lastSection ? false) { ~sections.last.stop(nil, true) };
//			if(thisThread === ~sectionRoutine) { nil.yield }
//				{ ~sectionRoutine.stop };
			~status = \idle;
			~currentSection = ~startingSection;
			currentEnvironment.changed(\stop);
			currentEnvironment.changed(\section, ~currentSection);
			~savedCmds = nil;
			~sectionRoutine.stop;
			~sectionRoutine = nil;
		};
//"<< sectionSequencer:stop".debug;
	};
	
	~loadResources = { |section|
//section.debug(">> loadResources");
			// must add 1 because do runs from 0 to n-1
		(section+1).do({ |sect|
			if(~sections[sect].isLoaded.not.debug("section % needs to load".format(sect))) {
				~sections[sect].prInitProc;
			};
		});
//debug("<< loadResources");
	};
	
	~countOffAndGo = {
		var	goTime = ~quant.nextTimeOnGrid(~clock) + ~countOffBeats - ~sectionLeadTime,
			started = false;

//[~clock.baseBarBeat, ~clock.beats, ~quant.nextTimeOnGrid(~clock), goTime].debug("starting countoff - base beat, now, quant time, go time");

		~status = \countingOff;
		~clock.schedAbs(~quant.nextTimeOnGrid(~clock), ~countOffRoutine = Routine({
			currentEnvironment.changed(\countoff, \init);
				// count off first
			~countOffBeats.do({ |count|
				currentEnvironment.changed(\countoff, count + 1);
				if(started.not and: { goTime - thisThread.clock.beats < ~sectionLeadTime }) {
					started = true;

//[~clock.beats, goTime].debug("starting section player - now, go time");

					thisThread.clock.schedAbs(goTime, ~sectionRoutine = Routine({
						~status = \playing;
						while { ~isRunning.() and: { ~currentSection < ~sections.size } } {
//~savedCmds.debug("playing section % with saved cmds".format(~sections[~currentSection].name));
							~sections[~currentSection].play(~savedCmds);
//"section sequencer: set savedCmds to nil".debug;
							~savedCmds = nil;
								// ~update method below will catch notifications
							~sections[~currentSection].addDependant(currentEnvironment);
							~condition.hang;
						};
						~stop.(true);	// stop last section
					}));
				};
				1.0.wait;
			});
			~countOffRoutine = nil;
			currentEnvironment.changed(\countoff, nil);
				// removing initial meter
				// section should run its own conductor slightly in advance of the true barline
			
//				// in theory nothing should be playing now
//				// so it should be OK to set this even off the barline
//			thisThread.clock.setMeterAtBeat(~sections[~currentSection].startBeatsPerBar,
//				thisThread.clock.beats);
//[~clock.beatsPerBar, ~clock.baseBar, ~clock.baseBarBeat].debug("after setting initial meter");
		}));
	};
	
	~doReset = {
		(BP.keys - ~doNotReset.as(IdentitySet)).do({ |name|
			BP(name).reset;
		});
	};
	
	~gotoSection = { |section|
		if(~isRunning.()) {
			MethodError("Cannot change section while playing", currentEnvironment).throw;
		} {
			~currentSection = section;
			currentEnvironment.changed(\section, section);
		};
	};
	~currentSection_ = { |section| ~gotoSection.(section) };
	
	~countOffBeats_ = { |beats|
		~countOffBeats = beats;
		currentEnvironment.changed(\countOffBeats, beats);
	};
	
	~autoReset_ = { |autoReset|
		~autoReset = autoReset;
		currentEnvironment.changed(\autoReset, autoReset);
	};
	
	~update = { |obj, what, args|
//[obj, what, args].debug("section sequencer got update");
		var	section;
		if(obj === ~sections[~currentSection]) {
			switch(what)
				{ \done } {
//args.debug("section sequencer update - savedCmds");
					~savedCmds = args;
					obj.removeDependant(currentEnvironment);
					if(~isRunning.()) {
						~currentSection = ~currentSection + 1;
//~currentSection.debug("section done, moving to");
						currentEnvironment.changed(\section, ~currentSection);
						~condition.unhang;
					};
				}
				{ \initNext } {
					if(~sections[~currentSection + 1].notNil) {
						~sections[~currentSection + 1].prInitProc;
					}
				}
				{ \unload } {
//[obj, what, args].debug("got unload message");
					args.tryPerform(\at, \name).asArray.do { |sectName|
//sectName.debug("checking");
						if((section = ~sections.detect { |sct| sct.name == sectName }).notNil) {
//sectName.debug("unloading one");
							section.prUnload;
						}
					};
				}
		};
	};
	~sectionNames = { ~sections.collect({ |sect, i| "%: %".format(i+1, sect.name) }) };
}) => PR(\sectionSequencer);

PR.allOfType(\tlCmd).do({ |pr|
	pr.value.put(\collIndex, pr.collIndex)
});

} { AbstractChuckArray.defaultSubType = saveSubType; };
