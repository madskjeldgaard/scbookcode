// h. james harkins - jamshark70@dewdrop-world.net
// not ready for prime time!

TLSequenceIterator {
	classvar	<>defaultAutoSync = true;
	var	<array, <sequencer, <activeCmds, <>autoSyncAtEnd,
		<>onStop,
		<status = \idle, routine, <condition, <>index, <clock, <>shouldSync = true;
	*new { |array, sequencer, autoSync|
		^super.newCopyArgs(array, sequencer, IdentitySet.new, autoSync ? defaultAutoSync)
	}
	
	play { |time = 0, argClock, runningCmds|
		var	temp, item, now, cmd, lastCmd;
		clock = argClock ?? { TempoClock.default };
		if(status == \idle) {
			condition = Condition.new;
			status = \running;
//"TLSeq: creating new routine".debug;
			routine = Routine({ |inval|
				this.addNotifications(runningCmds);
				now = inval;
//"TLSeq: set index to 0".debug;
				index = 0;
				this.changed(\play);
				while { 
//[index, array.size].debug("checking to continue");
				index < array.size } {
					item = array[index].asTLItem(now);
//[index, item].debug("got item");
					case
						{ item.isNumber } {
//item.debug("number");
							index = index + 1;
							now = max(item, 0).yield;
						}
						{ item.respondsTo(\isTLCommand) } {
//"command".debug;
							cmd = item;
							index = index + 1;
							if(array[index].respondsTo(\keysValuesDo)) {
								this.playCmd(cmd, array[index]);
								index = index + 1;
							} {
								this.playCmd(cmd);
							}
						}
						{ item == \sync } {
							now = this.fullSync;
							index = index + 1;
						}
						{ item == \cmdSync } {
							now = this.cmdSync(lastCmd);
							index = index + 1;
						}
						{ item.isArray } {
//"spawn".debug;
							index = index + 1;
							cmd = this.class.new(item, sequencer).play;
							this.addActive(cmd);
						}
						{		// default, ignore unrecognized item
							"Unrecognized sequence item at index %: %".format(index, item).warn;
							index = index + 1
						};
					lastCmd = cmd;
				};
//[index, array.size].debug("exit while");
//thisThread.clock.beats.debug("time at exit");
//lastCmd.env.postcs;
//status.debug("status at exit");
				if(autoSyncAtEnd) { this.fullSync(warn: false) };
//thisThread.clock.beats.debug("time after final sync");
				status = \idle;
					// there may be non-syncable commands still running
					// pass them back so that the next iterator can track them
					// the cmds should not retain the notification for this iterator
					// the next iterator should create them for itself
				this.removeNotifications(activeCmds);
					// in case this is a spawned iterator
				NotificationCenter.notify(this, \done, (activeCmds: activeCmds));
				this.changed(\done, activeCmds); // .debug("done sent");
					// allow old cmds to be GC'ed
					// not sure if this will break something
				activeCmds = IdentitySet.new;
			});
			(clock ?? { thisThread.clock }).sched(time, routine);
		} {
			"TLSequenceIterator is already active, cannot replay without stopping first".warn;
		};
	}
	
	stop { |parms|
//parms.debug(">> TLSequenceIterator:stop");
		parms ?? { parms = () };
		parms[\manualStop] ?? { parms.put(\manualStop, true) };
//parms.debug("parms after update");
		activeCmds.copy.do({ |cmd|
				// stopping non-syncable commands here messes up their status
				//  for the next iterator
			if(parms[\manualStop] or: { cmd.shouldSync }) {
				cmd.stop(parms)
			};
		});
		onStop.value(parms);
		routine.stop;
		if(status != \idle) {
			this.changed(\done).debug("done upon stop");
			status = \idle;
		};
//debug("<< TLSequenceIterator:stop");
	}
	
	isRunning { ^status == \running }

		// bookkeeping
	playCmd { |cmd, parms|
		if(cmd.isRunning) {
			cmd = cmd.copy;
		};
		this.addActive(cmd);
		parms ?? { parms = () };
		parms.putAll((sequencer: sequencer, iterator: this));
		cmd.play(parms);
		^cmd
	}
	
	addActive { |cmd|
//		var	updater;
//"\n\n>> TLSequenceIterator:addActive".debug;
//(try { cmd.env } { cmd }).debug("added command");
//cmd.dump;
//this.dumpBackTrace;
		activeCmds.add(cmd);
		NotificationCenter.registerOneShot(cmd, \done, ("stopped" ++ this.hash).asSymbol,
		{	|parms, resumeTime|
			this.cmdStopped(cmd, parms, resumeTime);
		});
//		updater = Updater(cmd, { |obj, what, parms, resumeTime|
////[obj, what].debug("activecmd updater");
//			if(what == \done) {
//				updater.remove;
//				this.cmdStopped(cmd, parms, resumeTime);
//			};
//		})
//"<< TLSequenceIterator:addActive".debug;
	}
	
	findActive { |id, thisCmd|
		^activeCmds.detect({ |item| (item !== thisCmd) and: { item.id == id } })
	}
	
	cmdStopped { |cmd, parms, resumeTime|
		var	oldCmds;
//var temp;
//
//status.debug(">> cmdStopped");
		activeCmds.remove(cmd);
		
			// I hope this works - the idea is that any non-syncable commands
			// from a spawned iterator should be registered in the parent,
			// so they can be passed up the chain if needed until they finally stop for real
		if((oldCmds = parms.tryPerform(\at, \activeCmds)).notNil) {
			oldCmds.do({ |cmd| this.addActive(cmd) });
		};
		
//if(cmd.class == Proto) {
//	cmd.env.debug("removed command");
//	cmd.env.proto.debug;
//} {
//	cmd.debug("removed command");
//};
//temp = activeCmds.select({ |cmd| cmd.shouldSync }).size.debug("number of remaining syncable commands");
//(activeCmds.size - temp).debug("number of non-syncable commands");
		if(status == \fullSync and: { activeCmds.any(_.shouldSync).not }) {
			if(resumeTime.notNil) {
//"scheduling resume".debug;
				clock.schedAbs(resumeTime, { this.prUnhang })
			} { /*"resuming".debug;*/ this.prUnhang };
		};
//status.debug("<< cmdStopped");
	}
	
	fullSync { |warn = true|
//"setting fullSync status".debug;
		if(status == \running and: { activeCmds.any(_.shouldSync) }) {
			status = \fullSync;
			condition.hang;
		} {
			if(warn) { "TLSequenceIterator: fullSync ignored, not running or no active commands.".warn };
		}
	}
	
	cmdSync { |lastCmd|
		var	updater;
//"\n>> cmdSync".debug;
//if(lastCmd.class == Proto) {
//	lastCmd.env.debug("removed command");
//	lastCmd.env.proto.debug;
//} {
//	lastCmd.debug("removed command");
//};
		if(lastCmd.shouldSync and: { lastCmd.tryPerform(\isRunning) ? false }) {
//"set cmdSync status".debug;
			status = \cmdSync;
			NotificationCenter.registerOneShot(lastCmd, \done, ("cmdSync" ++ this.hash).asSymbol,
			{	|parms, resumeTime|
//"\n\n\ngot done notification from lastCmd, resuming".debug;
				if(resumeTime.notNil) {
					clock.schedAbs(resumeTime, { this.prUnhang })
				} { this.prUnhang };
			});
//			updater = Updater(lastCmd, { |obj, what, parms, resumeTime|
////"sync updater".debug;
//				if(what == \done) {
//					updater.remove;
//					if(resumeTime.notNil) {
//						clock.schedAbs(resumeTime, { this.prUnhang })
//					} { this.prUnhang };
//				};
//			});
//"about to hang".debug;
			^condition.hang;
		} {
			"TLSequenceIterator: Cannot cmdSync to %. It is either not running or an invalid command."
				.format(lastCmd).warn;
		}
	}
	
	prUnhang {
		status = \running;
//"\n\n\nprUnhang: unhanging".debug;
		condition.unhang;
	}

	clock_ { |argClock|
		if(status == \idle) {
			clock = argClock ?? { TempoClock.default };
		} {
			"Cannot change sequencer's clock while it is playing.".warn;
		}
	}

	addNotifications { |cmds|
		cmds.do({ |cmd| this.addActive(cmd) });
	}
	
	removeNotifications { |cmds|
		var	dict, myid = this.hash.asString;
		cmds.do({ |cmd|
			(dict = NotificationCenter.registrationsFor(cmd, \done)) !? {
				 dict.keys.do({ |key|
				 		// does the notification belong to me?
					if(key.asString.contains(myid)) {
						NotificationCenter.unregister(cmd, \done, key);
					};
				});
			};
		});
	}

	*initClass {
		StartUp.add {
			(PathName(this.filenameSymbol.asString).pathOnly ++ "proto-cmds.scd").loadPath;
		}
	}
}
