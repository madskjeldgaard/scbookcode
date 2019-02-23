CtkObj {
	classvar <>latency = 0.1;
	var <objargs;
	
	addTo {arg aCtkScore;
		aCtkScore.add(this);
		^this;
		}
			
	addGetter {arg key, defaultVal;
		objargs.put(key.asSymbol, defaultVal);
		this.addUniqueMethod(key.asSymbol, {arg object; object.objargs[key]});
		}

	addSetter {arg key;
		this.addUniqueMethod((key.asString++"_").asSymbol, 
			{arg object, newval; object.objargs[key] = newval; object;
			});
		}
		
	addMethod {arg key, func;
		objargs.put(key.asSymbol, func);
		this.addUniqueMethod(key.asSymbol, {arg object ... args; 
			objargs[key].value(object, args);
			});
		}
		
	addParameter {arg key, defaultVal;
		defaultVal.isKindOf(Function).if({
			this.addMethod(key, defaultVal);
			}, {
			this.addGetter(key, defaultVal);
			this.addSetter(key);
			})
		^this;
		}
	}

// a wrapper for Score... takes CtkEvents and calcs a pad time, sorts, etc.
CtkScore : CtkObj {
	
	var <endtime = 0, score, <buffers, <ctkevents, <ctkscores, <controls, notes, <others, 
		<buffermsg, <buffersScored = false, <groups, oscready = false, <messages;
	var <masterScore, <allScores, <masterNotes, <masterControls, <masterBuffers, 
		<masterGroups, <masterMessages;
	
	
	*new {arg ... events;
		^super.new.init(events);
		}

	init {arg events;
		masterScore = [];
		objargs = Dictionary.new;
		score = Score.new;
		ctkscores = Array.new;
		buffers = Array.new;
		messages = Array.new;
		groups = Array.new;
		notes = Array.new;
		ctkevents = Array.new;
		controls = Array.new;
		others = Array.new;
		events.notNil.if({
			this.add(events);
			});
		}
				
	add {arg ... events;
		events.flat.do({arg event;
			case { // if the event is a note ...
				event.isKindOf(CtkNote)
				} {
				notes = notes.add(event);
				this.checkEndTime(event);
				} {
				event.isKindOf(CtkGroup);
				} {
				groups = groups.add(event);
				this.checkEndTime(event);
				} { // if the event is a buffer
				event.isKindOf(CtkBuffer);
				} {
				buffersScored.if({buffersScored = false});
				buffers = buffers.add(event);
				} {
				event.isKindOf(CtkEvent);
				} {
				ctkevents = ctkevents.add(event);
				this.checkEndTime(event);
				} {
				event.isKindOf(CtkControl);
				} {
				event.isScored.not.if({
					controls = controls.add(event);
					event.isScored = true;
					event.ctkNote.notNil.if({
						this.add(event.ctkNote);
						});
					this.checkEndTime(event);
					})
				} {
				event.isKindOf(CtkScore);
				} {
				ctkscores = ctkscores.add(event);
				} {
				event.isKindOf(CtkMsg);
				} {
				messages = messages.add(event);
				} {
				event.respondsTo(\messages);
				} {
				others = others.add(event);
				event.respondsTo(\endtime).if({
					this.checkEndTime(event)
					})
				} {
				true
				} {
				"It appears that you are trying to add a non-Ctk object to this score".warn;
				}
			});
			oscready.if({this.clearOSC});
		}
	
	clearOSC {
		oscready = false; score = Score.new;	
		}
		
	checkEndTime {arg event;
		(event.endtime).notNil.if({
			(endtime < event.endtime).if({
				endtime = event.endtime
				})
			});	
	}
	
	notes {arg sort = true;
		oscready.if({this.clearOSC});
		^notes.sort({arg a, b; a.starttime <= b.starttime});
		}
		
	notesAt {arg time, thresh = 0.0001;
		var notelist;
		notelist = this.notes;
		^notelist.select({arg me; me.starttime.fuzzyEqual(time, thresh) > 0})
		}
		
	score {
		this.saveToFile;
		^score.score;
		}
		
	saveToFile {arg path;
		score = Score.new;
		this.prepareObjects(false);
		this.groupTogether;
		this.objectsToOSC;
		score.add([endtime, 0]);		
		path.notNil.if({score.saveToFile(path)});
		}
	
	addBuffers {
		buffersScored.not.if({
			buffers.do({arg me;
				this.add(CtkMsg(me.server, 0.0, me.bundle).bufflag_(true));
				this.add(CtkMsg(me.server, endtime, me.freeBundle));
				(me.closeBundle.notNil).if({
					this.add(CtkMsg(me.server, endtime, me.closeBundle));
					});
				});
			})
		}

	// builds everything except the buffers since they act
	// different in NRT and RT
	
	prepareObjects {var rt = false;
		var eventArray, allReleases, theseReleases;
		var time, argname, argval;
		masterNotes = Array.new;
		masterControls = Array.new;
		masterBuffers = Array.new;
		masterGroups = Array.new;
		masterMessages = Array.new;
		allReleases = Array.new;
		allScores = [];
		this.concatScores(this);
		ctkevents.do({arg thisctkev;
			allScores = allScores.add(thisctkev.score)
			});
		allScores.do({arg thisscore;
			this.grabEvents(thisscore.groups, thisscore.notes, 				thisscore.controls, thisscore.buffers);
			});
		rt.not.if({
			this.addBuffers;
			});
		/* here */
		masterMessages = messages;
		masterGroups.do({arg thisgroup;
			(thisgroup.messages.size > 0).if({
				thisgroup.messages.do({arg me;
					masterMessages = masterMessages.add(me);
					})
				});
			});
		masterControls.do({arg thiscontrol;
			(thiscontrol.messages.size > 0).if({
				thiscontrol.messages.do({arg me;
					masterMessages = masterMessages.add(me);
					})
				});
			});
		masterNotes.do({arg thisnote;
			var bundle, endmsg, oldval, refsSort;
			endmsg = thisnote.getFreeMsg;
			endmsg.notNil.if({
				masterMessages = masterMessages.add(endmsg);
				});
			thisnote.refsDict.do({arg key, val;
				var tmpdur;
				refsSort = key.value.sort({arg a, b; a[0] < b[0]});
				refsSort.do({arg me;
					#time, argname, argval = me;
					case
						{argval.isKindOf(SimpleNumber)}
						{
							thisnote.set(time, argname, argval)
						}
						{argval.isKindOf(CtkControl) and: 
							{argval.isScored.not or: {
								argval.isARelease}}}
						{
							argval.starttime_(time + thisnote.starttime);
							"Endtime".postln;
							tmpdur = thisnote.endtime - time;// - thisnote.starttime;
							[thisnote.endtime, tmpdur].postln;
							(argval.duration.isNil or: {argval.duration > tmpdur}).if({
								argval.duration_(tmpdur);
								});
							thisnote.releases = thisnote.releases.add(argval);
							argval.isARelease = true;
							thisnote.noMaps.indexOf(argname).notNil.if({
								thisnote.set(time, argname, argval)
								}, {
								thisnote.map(time, argname, argval)								})
							}
						{argval.isKindOf(CtkControl)}
						{
							thisnote.noMaps.indexOf(argname).notNil.if({
								thisnote.set(time, argname, argval.asUGenInput)
								}, {
								thisnote.map(time, argname, argval.asUGenInput)
								})
						}
						{true}
						{thisnote.set(time, argname, argval)}
					});
				});
			(thisnote.messages.size > 0).if({
				thisnote.messages.do({arg me;
					masterMessages = masterMessages.add(me);
					})
				});
			});
		theseReleases = this.collectReleases(masterNotes);
		while({
			allReleases = allReleases ++ theseReleases;
			theseReleases = this.collectReleases(theseReleases);
			theseReleases.size > 0;
			});
		allReleases.do({arg thisnote;
			var endmsg;
			endmsg = thisnote.getFreeMsg;
			endmsg.notNil.if({
				masterMessages = masterMessages.add(endmsg);
				});
			(thisnote.messages.size > 0).if({
				thisnote.messages.do({arg me;
					masterMessages = masterMessages.add(me);
					})
				});	
			});
		masterNotes = masterNotes ++ allReleases;
		masterBuffers.do({arg thisbuffer;
			(thisbuffer.messages.size > 0).if({
				thisbuffer.messages.do({arg me;
					masterMessages = masterMessages.add(me);
					})
				})
			});
		}
	
	collectReleases {arg noteCollect;
		var raw, rels, ctkns;
		raw = noteCollect.collect({arg me;
			me.releases;
			});
		rels = raw.select({arg me;
			me.size > 0;
			});
		ctkns = rels.flat.collect({arg me;
			me.ctkNote});
		^ctkns = ctkns.select({arg me; me.notNil});
		}
	
	grabEvents {arg thesegroups, thesenotes, thesecontrols, thesebuffers;
		masterGroups = masterGroups ++ thesegroups.collect({arg me; me});
		masterNotes = masterNotes ++ thesenotes.collect({arg me; me});
		masterBuffers = masterBuffers ++ thesebuffers.collect({arg me; me});
		masterControls = masterControls ++ thesecontrols.collect({arg me; me});
		}
		
	groupTogether {
		masterScore = masterGroups ++ masterNotes ++ masterMessages;
		masterScore.sort({arg a, b;
			a.starttime < b.starttime;
			});
		masterScore = masterScore.separate({arg a, b;
			a.starttime.fuzzyEqual(b.starttime, 1.0e-07) == 0;
			});
		masterScore.do({arg thisTimesEvents;
			(thisTimesEvents.size > 1).if({
				thisTimesEvents.sort({arg a, b;
					((b.target == a) or: {a.isKindOf(CtkMsg) and: {a.bufflag}});
					})
				})
			});
		}

	concatScores {arg aScore;
		(aScore.ctkscores.size > 0).if({	
			aScore.ctkscores.do({arg me;
				this.concatScores(me);
				});
			});
		this.checkEndTime(aScore);
		allScores = allScores.add(aScore);
		}
	
	getNotes {arg aCtkControl;
		aCtkControl.ctkNotes.notNil.if({
			this.add(aCtkControl.ctkNotes);
			})
		}
		
	// create the OSCscore, load buffers, play score
	play {arg server, clock, quant = 0.0;
		server = server ?? {Server.default};
		server.boot;
		server.waitForBoot({
			var cond;
			cond = Condition.new;
			score = Score.new;
			Routine.run({
				this.loadBuffers(server, clock, quant, cond);
				this.prepareObjects(true);
				this.groupTogether;
				this.objectsToOSC;
				score.play;
				})
			})
		}
	
	objectsToOSC {
		masterScore.do({arg thisTimeEvent;
			var offset, block, thisBundle, thisTime = thisTimeEvent[0].starttime;
			var tmpBundle, tmp;
			block = 0;
			offset = 1.0e-07;
			thisBundle = [];
			thisTimeEvent.do({arg me;
				thisBundle = thisBundle ++ me.msgBundle;
				});
			(thisBundle.bundleSize < 1000).if({
				thisBundle = thisBundle.addFirst(thisTime);
				score.add(thisBundle);
				}, {
				tmpBundle = [];
				while({
					thisBundle.size > 0
					}, {
					// remove a message
					tmp = thisBundle.removeAt(0);
					// check if tmpBundle is above our desired size first
					((tmpBundle ++ tmp).bundleSize > 1000).if({
						tmpBundle = tmpBundle.addFirst(thisTime + (block * offset));
						score.add(tmpBundle);
						tmpBundle = [];
						tmpBundle = tmpBundle.add(tmp);
						block = block + 1;
						}, {
						tmpBundle = tmpBundle.add(tmp);
						});
					});
				tmpBundle = tmpBundle.addFirst(thisTime + (block * offset));
				score.add(tmpBundle);
				});
			});
		oscready = true;	
		}
		
	loadBuffers {arg server, clock, quant, cond;
		(buffers.size > 0).if({
			server.sync(cond, 
				Array.fill(buffers.size, {arg i;
					buffers[i].bundle;
					})
				);
			buffers.do({arg me;
				this.add(CtkMsg(server, endtime, me.freeBundle));
				(me.closeBundle.notNil).if({
					this.add(CtkMsg(server, endtime, me.closeBundle));
					})
				});
			"Buffer loaded!".postln;
			});
		}
		
	// SC2 it! create OSCscore, add buffers to the score, write it
	write {arg path, duration, sampleRate = 44100, headerFormat = "AIFF", 
			sampleFormat = "int16", options;
		this.saveToFile;
		score.recordNRT("/tmp/trashme", path, sampleRate: sampleRate, 
			headerFormat: headerFormat,
		 	sampleFormat: sampleFormat, options: options, duration: duration);
		}
		
	// add a time to all times in a CtkScore
	/* will probably have to add events and controls here soon */
	/* returns a NEW score with the events offset */
	
	offset {arg duration;
		var items;
		// all but buffers
		items = notes ++ groups ++ messages ++ controls ++ ctkevents ++ others;		items.do({arg me;
			me.starttime_(me.starttime + duration);
			});
		endtime = endtime + duration;
		}
		
	// copying can be problematic - dependencies can be lost
	copy {
		var newScore, newNote;
		newScore = CtkScore.new;
		this.items.do({arg me;
			me.isKindOf(CtkNote).if({
				newScore.add(me.copy(me.starttime));
				}, {
				newNote = me.deepCopy;
				newNote.server = me.server; // deepCopy changes the server! This can be bad.
				newScore.add(newNote);
				})
			});
		^newScore;
		}
		
	items {^notes ++ groups ++ messages ++ controls ++ ctkevents ++ buffers ++ others }

	merge {arg newScore, newScoreOffset = 0;
		var addScore;
		addScore = newScore.offset(newScoreOffset);
		this.add(addScore.items ++ addScore.ctkscores);
		}
}
// creates a dictionary of Synthdefs, and CtkNoteObjects
CtkProtoNotes {
	var <synthdefs, <dict;
	*new {arg ... synthdefs;
		^super.newCopyArgs(synthdefs).init;
		}
		
	init {
		dict = Dictionary.new;
		this.addToDict(synthdefs);
		}
	
	// load and add to the dictionary
	addToDict {arg sds;
		sds.do({arg me;
			case
				{me.isKindOf(SynthDef)}
				{dict.add(me.name -> CtkNoteObject.new(me))}
				{me.isKindOf(SynthDescLib)}
				{me.read;
				me.synthDescs.do({arg thissd;
					dict.add(thissd.name -> CtkNoteObject.new(thissd.name.asSymbol))
					});
				}
			})	
		}
	
	at {arg id;
		^dict[id.asString]
		}
		
	add {arg ... newsynthdefs;
		synthdefs = synthdefs ++ newsynthdefs;
		this.addToDict(newsynthdefs);
		}
}
	
		
CtkNoteObject {
	var <synthdef, <server, <synthdefname, args, <noMaps;
	*new {arg synthdef, server;
		^super.newCopyArgs(synthdef, server).init;
		}
		
	init {
		var sargs, sargsdefs, sd, count, tmpar, namesAndPos, sdcontrols, tmpsize, kouts;
		case
			{
			synthdef.isKindOf(SynthDef)
			}{
			this.buildControls;
			}{
			// if a string or symbol is passed in, check to see if SynthDescLib.global 
			// has the SynthDef
			(synthdef.isKindOf(String) || synthdef.isKindOf(Symbol) || 
				synthdef.isKindOf(SynthDesc)) 
			}{
			synthdef.isKindOf(SynthDesc).if({
				sd = synthdef;
				}, {	
				sd = SynthDescLib.global.at(synthdef);
				});
			sd.notNil.if({
				// check if this is a SynthDef being read from disk... if it is, it
				// has to be handled differently
				sd.def.allControlNames.notNil.if({
					synthdef = sd.def;
					this.buildControls;
					}, {
					synthdef = sd.def;
					args = Dictionary.new;
					synthdefname = synthdef.name;
					count = 0;
					namesAndPos = [];
					sd.controls.do({arg me, i;
						(me.name != '?').if({
							namesAndPos = namesAndPos.add([me.name, i]);
							}); 
						});
					sdcontrols = namesAndPos.collect({arg me, i;
						(i < (namesAndPos.size - 1)).if({
							tmpsize = namesAndPos[i + 1][1] - me[1];
							[me[0].asSymbol, (tmpsize > 1).if({
								(me[1]..(namesAndPos[i+1][1] - 1)).collect({arg j;
									sd.controls[j].defaultValue;
									})
								}, {
								sd.controls[me[1]].defaultValue;
								})]
							}, {
							tmpsize = sd.controls.size - 1 - me[1];
							[me[0].asSymbol, (tmpsize > 1).if({
								(me[1] .. (sd.controls.size) - 1).collect({arg j;
									sd.controls[j].defaultValue;
									}, {
									sd.controls[me[1]].defaultValue;
									})
								})]
							})
						});
					sdcontrols.do({arg me;
						var name, def;
						#name, def = me;
						args.add(name -> def);
						})
					});
					// no maps keeps Out.kr output vars from being mapped to
					kouts = sd.outputs.collect({arg me; me.startingChannel.source});
					kouts.removeAllSuchThat({arg item; item.isKindOf(String).not});
					noMaps = kouts.collect({arg item; item.asSymbol});
				},{
				"The SynthDef id you requested doesn't appear to be in your global SynthDescLib. Please .memStore your SynthDef, OR run SynthDescLib.global.read to read the SynthDesscs into memory".warn
				})
			}
		}
		
	buildControls {
		var kouts;
		synthdef.load(server ?? {Server.default});
		args = Dictionary.new;
		synthdefname = synthdef.name;
		synthdef.allControlNames.do({arg ctl, i;
			var def, name = ctl.name;
			def = ctl.defaultValue ?? {
				(i == (synthdef.allControlNames.size - 1)).if({
					synthdef.controls[ctl.index..synthdef.controls.size-1];
					}, {
					synthdef.controls[ctl.index..synthdef.allControlNames[i+1].index-1];
					})
				};
			args.add(name -> def);
			});
		kouts = synthdef.children.collect({arg me;
			((me.rate == \control) 
				and: {(me.class == Out) or: 
					{(me.class == ReplaceOut) or: 
						{me.class == XOut}
						}
					}).if({
					me.inputs[0]})});
		kouts.removeAllSuchThat({arg item; item.isKindOf(OutputProxy).not});
		noMaps = kouts.collect({arg item;
			var me, start, end;
			me = item.dumpName;
			start = me.indexOf($[);
			end = me.indexOf($]);
			synthdef.allControlNames[me[start+1..end-1].interpret].name.asSymbol;
			})
		}
		
	// create an CtkNote instance
	new {arg starttime = 0.0, duration, addAction = 0, target = 1, server;
		^CtkNote.new(starttime, duration, addAction, target, server, synthdefname, noMaps)
			.args_(args.deepCopy);
		}
		
	args {
		("Arguments and defaults for SynthDef "++synthdefname.asString++":").postln;
		args.keysValuesDo({arg key, val;
			("\t"++key++" defaults to "++val).postln;
			});
		^args;
		}
	
}

CtkSynthDef : CtkNoteObject {
	*new {arg name, ugenGraphFunc, rates, prependArgs, variants;
		var synthdef;
		synthdef = SynthDef(name, ugenGraphFunc, rates, prependArgs, variants);
		^super.new(synthdef);
		}
	}
	
CtkNode : CtkObj {
	classvar addActions, <nodes, <servers, <resps, cmd, <groups;

	var <addAction, <target, <>server;
	var >node, <>messages, <>starttime, <>willFree = false;
	var <isPaused = false, <>releases;

	node {
		^node ?? {node = server.nextNodeID};
		}
		
	watch {arg group;
		var thisidx;
		this.addServer(group);
		thisidx = servers.indexOf(server);
		nodes[thisidx] = nodes[thisidx].add(node);
		group.notNil.if({group.children = group.children.add(node)});
		}
		
	addServer {arg group;
		var idx;
		groups.indexOf(group).isNil.if({
			groups = groups.add(group)
			});
		servers.includes(server).not.if({
			idx = servers.size;
			servers = servers.add(server); // add the server
			nodes = nodes.add([]); // add an array for these nodes to live in
			resps = resps.add(OSCresponderNode(server.addr, '/n_end', {arg time, resp, msg;
				nodes[idx].remove(msg[1]);
				(groups.size > 0).if({
					groups.do({arg me;
						me.notNil.if({
							me.children.remove(msg[1]);
							})
						});
					})
				}).add); // add a responder to remove nodes
			cmd.if({cmd = false; CmdPeriod.doOnce({this.cmdPeriod})});
			});
	
		}
	
	isPlaying {
		var idx;
		this.addServer;
		(servers.size > 0).if({
			idx = servers.indexOf(server);
			(node.notNil && nodes[idx].includes(node)).if({^true}, {^false});
			}, {
			^false
			})
		}
		
	cmdPeriod {
		resps.do({arg me; me.remove});
		resps = [];
		servers = [];
		nodes = [];
		cmd = true;
		}
		
	set {arg time, key, value; 
		var bund;
		bund = [\n_set, this.node, key, value];
		this.handleMsg(time, bund);
		}
	
	setn {arg time, key ... values;
		var bund;
		values = values.flat;
		bund = [\n_setn, this.node, key, values.size] ++ values;
		this.handleMsg(time, bund);	
		}
	
	map {arg time, key, value;
		var bund;
		bund = [\n_map, this.node, key, value.asUGenInput];
		this.handleMsg(time, bund);
		}

	mapn {arg time, key ... values;
		var bund;
		values = values.flat;
		bund = [\n_map, this.node, key, values.size] ++ values.collect({arg me; me.bus});
		this.handleMsg(time, bund);
		}
	
	handleMsg {arg time, bund;
		this.isPlaying.if({ // if playing... send the set message now!
			time.notNil.if({
				SystemClock.sched(time, {
					server.sendBundle(latency, bund);
					});
				}, {
				server.sendBundle(latency, bund);
				})
			}, {
			starttime = starttime ?? {0.0};
			time = time ?? {0.0};
			messages = messages.add(CtkMsg(server, starttime + time, bund));
			})
	}	
	
	release {arg time, key = \gate;
		this.set(time, key, 0);
		willFree = true;
		((releases.size > 0) && this.isPlaying).if({
			Routine.run({
				while({
					0.1.wait;
					this.isPlaying.not.if({
						(releases.size > 0).if({
							releases.do({arg me; me.free})
							});
						});
					this.isPlaying;
					})
				});
			});
		^this;
		}
	
	// immeditaely kill the node
	free {arg time = 0.0, addMsg = true; 
		var bund;
		bund = [\n_free, this.node];
		willFree = true;
		this.isPlaying.if({
			SystemClock.sched(time, {
				server.sendBundle(latency, bund);
				(releases.size > 0).if({	
					releases.do({arg me;
						me.free;
						})
					});
				});
			}, {
			addMsg.if({
				messages = messages.add(CtkMsg(server, time+starttime, bund));
				})
			})
		}
	
	pause {
		this.isPlaying.if({
			isPaused.not.if({
				server.sendMsg(\n_run, node, 0);
				isPaused = true;
				})
			})
		}

	run {
		this.isPlaying.if({
			isPaused.if({
				server.sendMsg(\n_run, node, 1);
				isPaused = false;
				})
			})
		}
					
	asUGenInput {^node ?? {this.node}}
		
	*initClass {
		addActions = IdentityDictionary[
			\head -> 0,
			\tail -> 1,
			\before -> 2,
			\after -> 3,
			\replace -> 4,
			0 -> 0,
			1 -> 1,
			2 -> 2,
			3 -> 3,
			4 -> 4
			];
		nodes = [];
		servers = [];
		resps = [];
		cmd = true;
		groups = [];
		}
	}	

// these objects are similar to the Node, Synth and Buffer objects, except they are used to 
// create Scores and don't directly send messages to the Server

CtkNote : CtkNode {

	var <duration, <synthdefname,
		<endtime, <args, <setnDict, <mapDict, <noMaps, automations, <refsDict;
			
	*new {arg starttime = 0.0, duration, addAction = 0, target = 1, server, synthdefname, noMaps;
		server = server ?? {Server.default};
		^super.newCopyArgs(Dictionary.new, addAction, target, server)
			.initCN(starttime, duration, synthdefname, noMaps);
		}
				
	copy {arg newStarttime;
		var newNote;
		newStarttime = newStarttime ?? {starttime};
		newNote = this.deepCopy;
		newNote.server_(server);
		newNote.starttime_(newStarttime);
		newNote.messages = Array.new;
		newNote.node_(nil); 
		newNote.args_(args.deepCopy);
		^newNote;
		}

	initCN {arg argstarttime, argduration, argsynthdefname, argnoMaps;
		starttime = argstarttime;
		duration = argduration;
		synthdefname = argsynthdefname;
		node = nil; 
		noMaps = argnoMaps;
		messages = Array.new;
		(duration.notNil && (duration != inf)).if({
			endtime = starttime + duration;
			});
		setnDict = Dictionary.new;
		mapDict = Dictionary.new;
		refsDict = Dictionary.new;
		releases = [];
		automations = [];
		}
	
	starttime_ {arg newstart;
		starttime = newstart;
		releases.do({arg me; me.starttime_(newstart)});
		(duration.notNil && (duration != inf)).if({
			endtime = starttime + duration;
			})
		}
	
	duration_ {arg newdur;
		duration = newdur;
		releases.do({arg me; me.duration_(newdur)});
		starttime.notNil.if({
			endtime = starttime + duration;
			})
		}

	getValueAtTime {arg argname, time = 0.0;
		var times, argkey, values, pos, myRef;
		refsDict[argname].isNil.if({
			^args.at(argname)
			}, {
			myRef = refsDict[argname].value ++ [[0.0, argname, args.at(argname)]];
			#times, argkey, values = myRef.value.sort({arg a, b;
				a[0] < b[0]
				}).flop;
			pos = times.indexOfGreaterThan(time);
			pos = pos.isNil.if({
				times.size - 1;
				}, {
				pos - 1;
				});
			^values[pos];	
			})
		}
		
	args_ {arg argdict;
		args = argdict;
		argdict.keysValuesDo({arg argname, val;
			this.addUniqueMethod(argname.asSymbol, {arg note, time;
				time.isNil.if({
					args.at(argname)
					}, {
					this.getValueAtTime(argname, time)
					});
				});
			this.addUniqueMethod((argname.asString++"_").asSymbol, {
				arg note, newValue, timeOffset, curval;
				var oldval, thisarg;
				(this.isPlaying).if({
						oldval = args[argname];
						args.put(argname.asSymbol, newValue);
						this.handleRealTimeUpdate(argname, newValue, oldval, timeOffset);
						}, {	
						timeOffset.isNil.if({
							args.put(argname, newValue);
							this.checkIfRelease(newValue);
							}, {
							curval = this.perform(argname, timeOffset);
							refsDict[argname].isNil.if({
								refsDict.put(argname, 
									Ref([[timeOffset, argname, newValue]]))
								}, {
								refsDict[argname] = refsDict[argname].value.add( 
									[timeOffset, argname, newValue])
								});
							(curval.isKindOf(CtkControl) and: {curval.isARelease}).if({
								curval.duration_(timeOffset - curval.starttime)
								})
							});
						});

				note;
				});
			});		
		}
	
	checkIfRelease {arg aValue;
		// if it is a CtkControl     AND
		(aValue.isKindOf(CtkControl) and: {
			// it is is NOT playing or already a release
			(aValue.isARelease or: {aValue.isPlaying or: {aValue.isScored}}).not}).if({
				// then make it a release;
				releases = releases.add(aValue);
				aValue.isARelease = true;
				aValue.starttime_(starttime);
				(aValue.duration.notNil and: 
					{duration.notNil and: {aValue.duration > duration}}).if({
						aValue.duration_(duration);
					})
				})
		}
	
	checkNewValue {arg argname, newValue, oldval;
		case {
			(newValue.isArray || newValue.isKindOf(Env) || newValue.isKindOf(InterplEnv))
			}{
			newValue = newValue.asArray;
			this.setn(nil, argname, newValue);
			}{
			newValue.isKindOf(CtkControl)
			}{
			newValue.isPlaying.not.if({
				this.checkIfRelease(newValue);
				newValue.play(node, argname);
				});
			noMaps.indexOf(argname).notNil.if({
				this.set(latency, argname, newValue);
				}, {
				this.map(latency, argname, newValue);
				})
			}{
			true
			}{
			this.set(nil, argname, newValue.asUGenInput);
			};
		// real-time support for CtkControls
		(oldval.isKindOf(CtkControl)).if({
			(releases.indexOf(oldval)).notNil.if({
				oldval.free;
				releases.remove(oldval);
				})
			});
	}
				
	handleRealTimeUpdate {arg argname, newValue, oldval, timeOffset;
		timeOffset.notNil.if({
			SystemClock.sched(timeOffset, {
				this.isPlaying.if({
					this.checkNewValue(argname, newValue, oldval)
					})
				})
			}, {
			this.checkNewValue(argname, newValue, oldval);
			});
		}

	// every one of these has a tag and body... leaves room for addAction and 
	// target in CtkEvent
	
	newBundle {
		var bundlearray, initbundle;
		bundlearray =	this.buildBundle;
		initbundle = [starttime, bundlearray];
		setnDict.keysValuesDo({arg key, val;
			initbundle = initbundle.add([\n_setn, node, key, val.size] ++ val);
			});
		mapDict.keysValuesDo({arg key, val;
			initbundle = initbundle.add([\n_map, node, key, val.asUGenInput])			});
		^initbundle;	
		}
	
	// no time stamp;
	msgBundle {
		var bundlearray, initbundle;
		bundlearray =	this.buildBundle;
		initbundle = [bundlearray];
		setnDict.keysValuesDo({arg key, val;
			initbundle = initbundle.add([\n_setn, node, key, val.size] ++ val);
			});
		mapDict.keysValuesDo({arg key, val;
			initbundle = initbundle.add([\n_map, node, key, val.asUGenInput])			});
		^initbundle;	
		}
		
	buildBundle {
		var bundlearray, tmp;
		(target.isKindOf(CtkNote) || target.isKindOf(CtkGroup)).if({
			target = target.node});
		bundlearray =	[\s_new, synthdefname, this.node, addActions[addAction], target];
		args.keysValuesDo({arg key, val;
			var refsize;
			// check if val is a Ref - if so, we just need the initial value
			// store and automate its data
			refsDict[key].notNil.if({
				refsDict[key].value.do({arg me;
					automations = automations.add(me)
					});
				});
			tmp = this.parseKeys(key, val);
			tmp.notNil.if({
				bundlearray = bundlearray ++ tmp;
				})
			});
		^bundlearray;		
		}

	parseKeys {arg key, val;
		case {
			(val.isArray || val.isKindOf(Env) || val.isKindOf(InterplEnv))
			}{
			setnDict.add(key -> val.asArray); ^nil;
			}{
			val.isKindOf(CtkControl)
			}{
			// if this key is a noMap (so, probably the bus arg of Out.kr),
			// send in the CtkControl's bus number
			noMaps.indexOf(key).notNil.if({
				^[key, val.asUGenInput];
				}, {
				// oherwise, map the arg to the argument
				mapDict.add(key -> val); 
				^nil
				});
			}{
			true
			}{
			^[key, val.asUGenInput];
			}	
	}


	bundle {
		^this.newBundle;
		}

	getFreeMsg {
		(duration.notNil && willFree.not).if({
			^CtkMsg(server, (starttime + duration).asFloat, [\n_free, this.node]);
			}, {
			^nil
			});
		}
		
	// support playing and releasing notes ... not for use with scores
	play {arg group;
		var bund, start;
		this.isPlaying.not.if({
			SystemClock.sched(starttime ?? {0.0}, {
				bund = OSCBundle.new;
				bund.add(this.buildBundle);
				setnDict.keysValuesDo({arg key, val;
					bund.add([\n_setn, node, key, val.size] ++ val);
					});
				mapDict.keysValuesDo({arg key, val;
					(val.isPlaying.not).if({
						this.checkIfRelease(val);
						val.play;
						});
					bund.add([\n_map, node, key, val.asUGenInput]);
					});
				bund.send(server, latency);
				// for CtkControl mapping... make sure things are running!
				this.watch(group);
				// if a duration is given... kill it
				duration.notNil.if({
					SystemClock.sched(duration, {this.free(0.1, false)})
					});
				(automations.size > 0).if({
					this.playAutomations;
					})
				});
			^this;
			}, {
			"This instance of CtkNote is already playing".warn;
			})
		}

	playAutomations {
		var events, curtime = 0.0, firstev, idx = 0;
		// first, save the automations to a local var, and clear them out.
		events = automations;
		automations = [];
		events.sort({arg a, b; a[0] < b[0]});
		firstev = events[0][0];
		SystemClock.sched(firstev, {
			(this.isPlaying).if({
				this.perform((events[idx][1]++"_").asSymbol, events[idx][2]);
				curtime = events[idx][0];
				idx = idx + 1;
				(idx < events.size).if({
					events[idx][0] - curtime
					}, {
					nil
					});
				})
			})
		}
		
	prBundle {
		^this.bundle;
		}		
	}

/* methods common to CtkGroup and CtkNote need to be put into their own class (CtkNode???) */
CtkGroup : CtkNode {
	var <endtime = nil, <duration, <isGroupPlaying = false, <>children;
	
	*new {arg starttime = 0.0, duration, node, addAction = 0, target = 1, server;
		^super.newCopyArgs(Dictionary.new, addAction, target, server, node)
			.init(starttime, duration);
		}
		
	*play {arg starttime = 0.0, duration, node, addAction = 0, target = 1, server;
		^this.new(starttime, duration, node, addAction, target, server).play;
		}
		
	init {arg argstarttime, argduration;
		starttime = argstarttime;
		duration = argduration;
		duration.notNil.if({
			endtime = starttime + duration
			});
		server = server ?? {Server.default};
		messages = Array.new;
		children = Array.new;
		}
		
	newBundle {
		var start, bundlearray;
		bundlearray =	this.buildBundle;
		start = starttime ?? {0.0}
		^[starttime, bundlearray];	
		}
	
	buildBundle {
		var bundlearray;
		bundlearray =	[\g_new, this.node, addActions[addAction], target.asUGenInput];
		^bundlearray;		
		}
	
	msgBundle {
		^[this.buildBundle];
		}
		
	prBundle {
		^this.bundle;
		}

	bundle {
		var thesemsgs;
		thesemsgs = [];
		thesemsgs = thesemsgs.add(this.newBundle);
		(duration.notNil && willFree.not).if({
			thesemsgs = thesemsgs.add([(starttime + duration).asFloat, [\n_free, node]]);
			});
		^thesemsgs;
		}
		
	// create the group for RT uses
	play {arg neg = 0.01; // neg helps insure that CtkGroups will be created first 
		var bundle = this.buildBundle;
		starttime.notNil.if({
			SystemClock.sched(starttime, {server.sendBundle(latency - neg, bundle)});
			}, {
			server.sendBundle(latency - neg, bundle);
			});
		duration.notNil.if({
			SystemClock.sched(duration, {this.freeAll})
			});
		this.watch;
		isGroupPlaying = true;
		^this;
		}
		
	freeAll {arg time = 0.0;
		var bund1, bund2;
		bund1 = [\g_freeAll, node];
		bund2 = [\n_free, node];
		isGroupPlaying.if({
			SystemClock.sched(time, {server.sendBundle(latency, bund1, bund2)});
			isGroupPlaying = false;
			}, {
			messages = messages.add(CtkMsg(server, starttime + time, bund1, bund2));
			})
		}
	
	deepFree {arg time = 0.0;
		this.freeAll(time);
		}
		
	}

// if a CtkBuffer is loaded to a server, its 'isPlaying' instance var will be set to true, and 
// the CtkBuffer will be considered live.

CtkBuffer : CtkObj {
	var <bufnum, <path, <size, <startFrame, <numFrames, <numChannels, <server, <bundle, 
		<freeBundle, <closeBundle, <messages, <isPlaying = false, <isOpen = false;
	var duration, <sampleRate, <starttime = 0.0;
	
	*new {arg path, size, startFrame = 0, numFrames, numChannels, bufnum, server;
		^this.newCopyArgs(Dictionary.new, bufnum, path, size, startFrame, numFrames, 
			numChannels, server).init;
		}
	
	*diskin {arg path, size = 32768, startFrame = 0, server;
		^this.new(path, size, startFrame, server: server)
		}
		
	*playbuf {arg path, startFrame = 0, numFrames = 0, server;
		^this.new(path, startFrame: startFrame, numFrames: numFrames, server: server)
		}
		
	*buffer {arg size, numChannels, server;
		^this.new(size: size, numChannels: numChannels, server: server)
		}

	init {
		var sf, nFrames;
		server = server ?? {Server.default};
		bufnum = bufnum ?? {server.bufferAllocator.alloc(1)};
		messages = [];
		path.notNil.if({
			sf = SoundFile.new(path);
			sf.openRead;
			numChannels = sf.numChannels;
			duration = sf.duration;
			sampleRate = sf.sampleRate;
			sf.close;
			});
		case { // path, not size - load file with b_allocRead
			path.notNil && size.isNil
			} {
			nFrames = numFrames ?? {0};
			bundle = [\b_allocRead, bufnum, path, startFrame, nFrames];
			} {// path, size ( for DiskIn )
			path.notNil && size.notNil
			} {
			nFrames = numFrames ?? {size};
			bundle = [\b_alloc, bufnum, size, numChannels, 
				[\b_read, bufnum, path, startFrame, nFrames, 0, 1]];
			closeBundle = [\b_close, bufnum];
			} { /// just allocate memory (for delays, FFTs etc.)
			path.isNil && size.notNil
			} {
			numChannels = numChannels ?? {1};
			bundle = [\b_alloc, bufnum, size, numChannels];
			};
		freeBundle = [\b_free, bufnum];
		}
	
	load {arg time = 0.0, sync = true, cond, onComplete;
		SystemClock.sched(time, {
			Routine.run({
				var msg;
				cond = cond ?? {Condition.new};
				server.sendBundle(latency, bundle);
				// are there already messages to send? If yes... SYNC!, then send NOW
				(messages.size > 0).if({
					server.sync(cond);
					messages.do({arg me; 
						msg = me.messages;
						msg.do({arg thismsg;
							server.sendBundle(latency, thismsg);
							});
						server.sync(cond);
						});
					});
				sync.if({
					server.sync(cond);
					("CtkBuffer with bufnum id "++bufnum++" loaded").postln;
					onComplete.value;
					});
				isPlaying = true;
				})
			});
		} 
	
	free {arg time = 0.0;
		closeBundle.notNil.if({
			SystemClock.sched(time, {
				server.sendBundle(latency, closeBundle, freeBundle);
				server.bufferAllocator.free(bufnum);
				});
			}, {
			SystemClock.sched(time, {
				server.sendBundle(latency, freeBundle);
				server.bufferAllocator.free(bufnum);
				});
			});
		isPlaying = false;
		}

	set {arg time = 0.0, startPos, values;
		var bund;
		values = values.asArray;
		// check for some common problems
		((values.size + startPos) > size).if({
			"Number of values and startPos exceeds CtkBuffer size. No values were set".warn;
			^this;
			}, {
			bund = [\b_setn, bufnum, startPos, values.size] ++ values;
			([0.0, bund].bundleSize >= 8192).if({
				"Bundle size exceeds UDP limit. Use .loadCollection. No values were set".warn;
				^this;
				}, {
				this.bufferFunc(time, bund);
				^this;
				})
			})
		}
		
	zero {arg time = 0;
		var bund;
		bund = [\b_zero, bufnum];
		this.bufferFunc(time, bund);
		}
		
	fill {arg time = 0.0, newValue, start = 0, numSamples = 1;
		var bund;
		bund = [\b_fill, bufnum, start, numSamples, newValue];
		this.bufferFunc(time, bund);
		}
	
	// write a buffer out to a file. For DiskOut usage in real-time, use openWrite and closeWrite
	write {arg time = 0.0, path, headerFormat = 'aiff', sampleFormat='int16', 
			numberOfFrames = -1, startingFrame = 0;	
		var bund;
		bund = [\b_write, bufnum, path, headerFormat, sampleFormat, numberOfFrames, 
			startingFrame, 0];
		this.bufferFunc(time, bund);
		}
	
	// prepare a buffer for use with DiskOut
	openWrite {arg time = 0.0, path, headerFormat = 'aiff', sampleFormat='int16', 
			numberOfFrames = -1, startingFrame = 0;	
		var bund;
		isOpen = true;
		bund = [\b_write, bufnum, path, headerFormat, sampleFormat, numberOfFrames, 
			startingFrame, 1];
		this.bufferFunc(time, bund);
		}
		
	closeWrite {arg time = 0.0;
		var bund;
		isOpen = false;
		bund = [\b_close, bufnum];
		this.bufferFunc(time, bund);
		}
		
	gen {arg time = 0.0, cmd, normalize = 0, wavetable = 0, clear = 1 ... args;
		var bund, flag;
		flag = (normalize * 1) + (wavetable * 2) + (clear * 4);
		bund = ([\b_gen, bufnum, cmd, flag] ++ args).flat;
		this.bufferFunc(time, bund);
		}
		
	sine1 {arg time, normalize = 0, wavetable = 0, clear = 1 ... args;
		this.gen(time, \sine1, normalize, wavetable, clear, args);
		}

	sine2 {arg time, normalize = 0, wavetable = 0, clear = 1 ... args;
		this.gen(time, \sine2, normalize, wavetable, clear, args);
		}
		
	sine3 {arg time, normalize = 0, wavetable = 0, clear = 1 ... args;
		this.gen(time, \sine3, normalize, wavetable, clear, args);
		}
		
	cheby {arg time, normalize = 0, wavetable = 0, clear = 1 ... args;
		this.gen(time, \cheby, normalize, wavetable, clear, args);
		}
		
	fillWithEnv {arg time = 0.0, env, wavetable = 0.0;
		env = (wavetable > 0.0).if({
			env.asSignal(size * 0.5).asWavetable;
			}, {
			env.asSignal(size)
			});
		this.set(time = 0.0, 0, env);
		}

	// checks if this is a live, active buffer for real-time use, or being used to build a CtkScore
	bufferFunc {arg time, bund;
		isPlaying.if({
			SystemClock.sched(time, {server.sendBundle(latency, bund)})
			}, {
			messages = messages.add(CtkMsg(server, time ?? {0.0}, bund))
			});
		}
	
	duration {
		duration.isNil.if({
			^size / (server.sampleRate ?? {"The Server doesn't appear to be booted, therefore a duration can not be calculated. The SIZE of the buffer in frames will be returned instead".warn; 1});
			}, {
			^duration
			})
		}
		
	server_ {arg aServer;
		isPlaying.not.if({
			server = aServer;
			}, {
			"A CtkBuffer's server can not be changed while it is being used in real-time mode".warn;})
		}
	asUGenInput {^bufnum}
	}
		
CtkControl : CtkObj {
	var <server, <numChans, <bus, <initValue, <starttime, <messages, <isPlaying = false, 
	<endtime = 0.0, <duration; //may want to get rid of setter later
	var <env, <ugen, <freq, <phase, <high, <low, <ctkNote, free, <>isScored = false, 
	<isLFO = false, <isEnv = false;
	var timeScale, <levelBias, <levelScale, <doneAction, <>isARelease = false;
		
	classvar ctkEnv, sddict; 
	*new {arg numChans = 1, initVal = 0.0, starttime = 0.0, bus, server;
		^this.newCopyArgs(Dictionary.new, server, numChans, bus, initVal, starttime).initThisClass;
		}
	
	/* calling .play on an object tells the object it is being used in real-time
	and therefore will send messages to server */
	*play {arg numChans = 1, initVal = 0.0, bus, server;
		^this.new(numChans, initVal, 0.0, bus, server).play;
		}

	initThisClass {
		var bund;
		server = server ?? {Server.default};
		bus = bus ?? {server.controlBusAllocator.alloc(numChans)};
		messages = []; // an array to store sceduled bundles for this object
		bund = [\c_setn, bus, numChans, initValue];
		messages = messages.add(CtkMsg(server, starttime.asFloat, bund));
		ctkNote = nil;
		}
			
	starttime_ {arg newStarttime;
		starttime = newStarttime;
		starttime.notNil.if({
			ctkNote.notNil.if({
				ctkNote.starttime_(newStarttime);
				});
			[freq, phase, high, low].do({arg me;
				me.isKindOf(CtkControl).if({
					me.starttime_(newStarttime);
				});
			});
		})
		}
	
	duration_ {arg newDuration;
		duration = newDuration;
		duration.notNil.if({
			ctkNote.notNil.if({
				ctkNote.duration_(duration)
				});
			[freq, phase, high, low].do({arg me;
				me.isKindOf(CtkControl).if({
					me.duration_(duration)
					})
				})
			})
		}
	*env {arg env, starttime = 0.0, addAction = 0, target = 1, bus, server,
			levelScale = 1, levelBias = 0, timeScale = 1, doneAction = 2;
		^this.new(1, env[0], starttime, bus, server).initEnv(env, levelScale, levelBias, timeScale, 
			addAction, target, doneAction);
		}
	
	initEnv {arg argenv, argLevelScale, argLevelBias, argTimeScale, argAddAction, argTarget, 
			argDoneAction;
		env = argenv;
		timeScale = argTimeScale;
		levelScale = argLevelScale;
		levelBias = argLevelBias;
		doneAction = argDoneAction;
		isEnv = true;
		duration = env.releaseNode.notNil.if({
			free = false;
			nil
			}, {
			free = true;
			env.times.sum * timeScale;
			});
		// the ctk note object for generating the env
		ctkNote = sddict[\ctkenv].new(starttime, duration, argAddAction, argTarget, 
			server).myenv_(env).outbus_(bus).levelScale_(levelScale).levelBias_(levelBias)
			.timeScale_(timeScale).doneAction_(doneAction);
		}
	
	levelScale_ {arg newLS = 1;
		isEnv.if({
			ctkNote.levelScale_(newLS);
			levelScale = newLS;
			})
		}
		
	levelBias_ {arg newLB = 0;
		isEnv.if({
			ctkNote.levelBias_(newLB);
			levelBias = newLB;
			})
		}
		
	*lfo {arg ugen, freq = 1, low = -1, high = 1, phase = 0, starttime = 0.0, duration,
			addAction = 0, target = 1, bus, server;
		^this.new(1, 0.0, starttime, bus, server).initLfo(ugen, freq, phase, low, high, addAction,
			target, duration);
		}
		
	initLfo {arg argugen, argfreq, argphase, arglow, arghigh, argAddAction, argTarget, argDuration;
		var thisctkno;
		ugen = argugen;
		freq = argfreq;
		phase = argphase;
		low = arglow;
		high = arghigh;
		duration = argDuration;
		free = false;
		messages = [];
		isLFO = true;
		thisctkno = sddict[("CTK"++ugen.class).asSymbol];
		case
			{
			[LFNoise0, LFNoise1, LFNoise2].indexOf(ugen).notNil;
			} {
			ctkNote = thisctkno.new(starttime, duration, argAddAction,
				argTarget, server).freq_(freq).low_(low).high_(high).bus_(bus);
			} {
			[SinOsc, Impulse, LFSaw, LFPar, LFTri, LFCub].indexOf(ugen).notNil;
			} {
			ctkNote = thisctkno.new(starttime, duration, argAddAction, 
				argTarget, server).freq_(freq).low_(low).high_(high)
				.phase_(phase).bus_(bus);
			}			
		}
	
	freq_ {arg newfreq;
		isLFO.if({
			ctkNote.freq_(newfreq);
			})
		}

	low_ {arg newlow;
		isLFO.if({
			ctkNote.low_(newlow);
			})
		}

	high_ {arg newhigh;
		isLFO.if({
			ctkNote.high_(newhigh);
			})
		}
							
	// free the id for further use
	free {
		isPlaying = false;
		ctkNote.notNil.if({
			ctkNote.free;
			});
		server.controlBusAllocator.free(bus);
		}

	release {
		ctkNote.notNil.if({
			ctkNote.release;
			})
		}
		
	play {arg node, argname;
		var time, bund, bundle;
		isPlaying = true;
		ctkNote.notNil.if({
			ctkNote.play;
			});
		messages.do({arg me;
			me.msgBundle.do({arg thisMsg;
				bund = bund.add(thisMsg);
				});
			bundle = OSCBundle.new;
			bund.do({arg me;
				bundle.add(me)
				});
			time = me.starttime;
			(time > 0).if({
				SystemClock.sched(time, {
					bundle.send(server, latency);
					})
				}, {
					bundle.send(server, latency);
				});	
			});
		}
	
	server_ {arg aServer;
		this.isPlaying.not.if({
			server = aServer;
			})
		}
			
	set {arg val, time = 0.0;
		var bund;
		bund = [\c_setn, bus, numChans, val];
		isPlaying.if({
			SystemClock.sched(time, {server.sendBundle(latency, bund)});
			}, {
			time = time ?? {0.0};
			messages = messages.add(CtkMsg(server, starttime + time, bund));
			});
		initValue = val;
		^this;
		}
	
	asUGenInput {^bus}
	
	*initClass {
		var thisctkno;
		sddict = CtkProtoNotes(
			SynthDef(\ctkenv, {arg gate = 1, outbus, levelScale = 1, levelBias = 0, 
					timeScale = 1, doneAction = 0;
				Out.kr(outbus,
					EnvGen.kr(
						Control.names([\myenv]).kr(Env.newClear(16)), 
						gate, timeScale: timeScale, doneAction: doneAction) * 
							levelScale + levelBias)
				})
			);
			[LFNoise0, LFNoise1, LFNoise2].do({arg ugen;
				thisctkno = SynthDef(("CTK" ++ ugen.class).asSymbol, {arg freq, low, high, bus;
					Out.kr(bus, ugen.kr(freq).range(low, high));
					});
				sddict.add(thisctkno);
				});
			[SinOsc, Impulse, LFSaw, LFPar, LFTri, LFCub].do({arg ugen;
				thisctkno = 
					SynthDef(("CTK" ++ ugen.class).asSymbol, {arg freq, low, high, phase, bus;
						Out.kr(bus, ugen.kr(freq, phase).range(low, high));
					});
				sddict.add(thisctkno);
				});
		
		}
	}

// not really needed... but it does most of the things that CtkControl does
CtkAudio : CtkObj {
	var <server, <bus, <numChans;
	*new {arg bus, numChans = 1, server;
		^this.newCopyArgs(Dictionary.new, server, bus, numChans).init;
		}

	// free the id for further use
	free {
		server.audioBusAllocator.free(bus);
		}
			
	init {
		server = server ?? {Server.default};
		bus = bus ?? {server.audioBusAllocator.alloc(numChans)};
		}
		
	asUGenInput {^bus}
	}
	
/* this will be similar to ProcMod ... global envelope magic 

CtkEvent can return and play a CtkScore - individual group, envbus?

with .play - needs to act like ProcMod
with .write, needs to act like CtkScore
with .addToScore - needs to act like .write, and return the CtkScore that is created, and append
	them

need to create a clock like object that will wait in tasks, advance time in .write situations
*/

/* CtkTimer needs to be a TempoClock when played, a timekeeper when used for NRT */

CtkTimer {
	var starttime, <curtime, <clock, ttempo, rtempo, isPlaying = false, <next = nil;
	
	*new {arg starttime = 0.0;
		^super.newCopyArgs(starttime, starttime);
		}
	
	play {arg tempo = 1;
		isPlaying.not.if({
			clock = TempoClock.new;
			clock.tempo_(ttempo = tempo);
			rtempo = ttempo.reciprocal;
			isPlaying = true;
			}, {
			"This CtkClock is already playing".warn
			});
		}
	
	beats {
		^this.curtime;
		}
		
	free {
		isPlaying.if({
			clock.stop;
			isPlaying = false;
			})
		}
	
	now {
		isPlaying.if({
			^clock.elapsedBeats;
			}, {
			^curtime - starttime;
			})
		}
		
	wait {arg inval;
		isPlaying.if({
			(inval*rtempo).yield;
			}, {
			curtime = curtime + inval
			});
		}
		
	next_ {arg inval;
		next = inval;
		isPlaying.not.if({
			curtime = curtime + inval;
			})
		}
	}
	
CtkEvent : CtkObj {
	classvar envsd, addActions;
	var starttime, <>condition, <function, amp, <server, addAction, target, isRecording = false;
	var isPlaying = false, isReleasing = false, releaseTime = 0.0, <timer, clock, 
		<envbus, inc, <group, <>for = 0, <>by = 1, envsynth, envbus, playinit, notes, 
		score, <endtime, endtimeud, noFunc = false;
	
	*new {arg starttime = 0.0, condition, amp = 1, function, addAction = 0, target = 1, server;
		^super.newCopyArgs(Dictionary.new, starttime, condition, function, amp, server,
			addActions[addAction]).initCE(target);
		}
		
	initCE {arg argTarget;
		argTarget.asUGenInput;
		target = argTarget ?? {1};
		server = server ?? {Server.default};
		timer = CtkTimer.new(starttime);
		(condition.isKindOf(Env) and: {condition.releaseNode.isNil}).if({
			endtime = condition.times.sum;
			endtimeud = false
			}, {
			endtime = starttime;
			endtimeud = true;
			});
		inc = 0;
		playinit = true;
		notes = [];
		function = function ?? {noFunc = true; {}};
		}
		
	function_ {arg newfunction;
		noFunc = false;
		function = newfunction;
		}
	
	record {
		score = CtkScore.new;
		isRecording = true;
		this.play;
		^score;
		}
		
	play {
		var loopif, initVal, initSched;
		server.serverRunning.if({
			isPlaying.not.if({
				isPlaying = true;
				timer.play;
				this.setup;
				clock.sched(starttime, {
					var now;
					now = timer.now;
					playinit.if({
						playinit = false;
						condition.isKindOf(Env).if({
							condition.releaseNode.isNil.if({
								clock.sched(condition.times.sum + 0.1, {this.clear});
								})
							});
						[group, envbus, envsynth].do({arg me; 
							me.notNil.if({
								isRecording.if({
									score.add(me)
									});
								me.play
								})
							});
						});
					function.value(this, group, envbus, inc, server);
//					this.run;
					this.checkCond.if({
						timer.next;
						}, {
						initSched = (endtime > timer.now).if({endtime - timer.now}, {0.1});
						timer.clock.sched(initSched, {
//							((group.children.size == 0) and: {noFunc}).if({
							((group.children.size == 0)).if({
								this.free;
								}, {
								0.1;
								})
							})
						});
					})
				})
			}, {
			"Please boot the Server before trying to play an instance of CtkEvent".warn;
			})
		}
	
	run {
		notes.asArray.do({arg me;
			((me.starttime == 0.0) or: {me.starttime.isNil}).if({
				isPlaying.if({
					isRecording.if({score.add(me.copy.starttime_(timer.now))});
					me.play(group);
					})
				}, {
				clock.sched(me.starttime, {
					isPlaying.if({
						isRecording.if({
							score.add(me.copy(timer.now))
							});
						me.play(group);
						});
					})
				});
			});
		notes = [];
		inc = inc + by;	
		}
		
	setup {
		var thisdur;
		group.notNil.if({group.free});
		envbus.notNil.if({envbus.free});
		clock = timer.clock;
		group = CtkGroup.new(addAction: addAction, target: target, server: server);
		condition.isKindOf(Env).if({
			envbus = CtkControl.new(initVal: condition.levels[0], starttime: starttime, 
				server: server);
			thisdur = condition.releaseNode.isNil.if({condition.times.sum}, {nil});
			envsynth = envsd.new(duration: thisdur, target: group, server: server)
				.outbus_(envbus.bus).evenv_(condition).amp_(amp);
			}, {
			envbus = CtkControl.new(1, amp, starttime, server: server);
			});
		(target.isKindOf(CtkNote) || target.isKindOf(CtkGroup)).if({
			target = target.node});
		}
	
	free {
		this.clear;
		}
	
	release {
		isPlaying.if({
			noFunc.if({noFunc = false});
			condition.isKindOf(Env).if({
				condition.releaseNode.notNil.if({
					envsynth.release(key: \evgate);
					this.releaseSetup(condition.releaseTime);
					}, {
					"The envelope for this CtkEvent doesn't have a releaseNode. Use .free instead".warn;})
				}, {
				"This CtkEvent doesn't use an Env as a condition control. Use .free instead".warn
				})
			}, {
			"This CtkEvent is not playing".warn
			});
		}
		
	releaseSetup {arg reltime;
		clock.sched(reltime, {this.clear});
		}
		
	clear {
		clock.clear;
		clock.stop;
		group.free;
		envbus.free;
		isPlaying = false;
		isRecording = false;
		this.initCE;
		}

	scoreClear {
		clock.clear;
		clock.stop;
		isPlaying = false;
		isRecording = false;
		this.initCE;
		}
			
	next_ {arg inval;
		timer.next_(inval);
		}
	
	curtime {
		^timer.curtime;
		}
	
	checkCond {
		case
			{
			(timer.next == nil)// and: {noFunc.not}
			} {
			^noFunc;//false
			} {
			condition.isKindOf(Boolean) || condition.isKindOf(Function)
			} {
			^condition.value(timer, inc)
			} {
			condition.isKindOf(SimpleNumber)
			} {
			^inc < condition
			} {
			condition.isKindOf(Env)
			} {
			^condition.releaseNode.isNil.if({
				timer.now < condition.times.sum;
				}, {
				(isReleasing || (releaseTime < condition.releaseTime))
				})
			} {
			true
			} {
			^false
			}
		}
	
	collect {arg ... ctkevents;
		var thisend;
		ctkevents = ctkevents.flat;
		endtimeud.if({
			ctkevents.do({arg ev;
				ev.endtime.notNil.if({
					thisend = ev.endtime + timer.now;
					(thisend > endtime).if({
						endtime = thisend
						})
					})
				})
			});
		notes = (notes ++ ctkevents).flat;
		isPlaying.if({this.run});
 		}
	
	//  may not need this... or, if may be able to be simplified (just store objects to 
	// the CtkScore ... or WOW! I THINK IT WILL JUST WORK!)
	
	score {arg sustime = 0;
		var curtime,idx;
		// check first to make sure the condition, if it is an Env, has a definite duration
		condition.isKindOf(Env).if({
			condition.releaseNode.notNil.if({
				// use sustime to convert it to a finite Env
				idx = condition.releaseNode;
				condition.times = condition.times.insert(idx, sustime);
				condition.levels = condition.levels.insert(idx, condition.levels[idx]);
				condition.curves.isArray.if({
					condition.curves = condition.curves.insert(idx, \lin)
					});
				condition.releaseNode_(nil);
				});
			});
		score = CtkScore.new;
		this.setup;
		group.node;
		[group, envbus, envsynth].do({arg me; 
			me.notNil.if({me.starttime_(starttime);
			score.add(me)
			})
		});
		while({
			curtime = timer.curtime;
			function.value(this, group, envbus, inc, server);
			notes.asArray.do({arg me;
				me.starttime.isNil.if({
					me.starttime_(curtime)
					}, {
					me.starttime_(me.starttime + curtime);
					});
				score.add(me);
				});
			notes = [];
			inc = inc + by;
			this.checkCond;
			});
		this.scoreClear;
		^score;
		}
	
	*initClass {
		addActions = IdentityDictionary[
			\head -> 0,
			\tail -> 1,
			\before -> 2,
			\after -> 3,
			\replace -> 4,
			0 -> 0,
			1 -> 1,
			2 -> 2,
			3 -> 3,
			4 -> 4
			];
		StartUp.add({
		envsd = CtkNoteObject(
			SynthDef(\ctkeventenv_2561, {arg evgate = 1, outbus, amp = 1, timeScale = 1, 
					lag = 0.01;
				var evenv;
				evenv = EnvGen.kr(
					Control.names(\evenv).kr(Env.newClear(30)), evgate, 
						1, 0, timeScale, doneAction: 13) * Lag2.kr(amp, lag);
				Out.kr(outbus, evenv);
				})
			);
			})	
		}
		
	}

// a simple object for catching any number of extra messages - mostly for storage and sorting
// messages are OSC messages. Will probably be only used internally

CtkMsg : CtkObj{
	var <>starttime, <duration, <endtime, <messages, <target = 0, <>bufflag = false;
	var <>server;
	
	*new {arg server, starttime ... messages;
		^super.new.initMsgClass(server, starttime, messages);
		}
		
	initMsgClass {arg argServer, argStarttime, argMessages;
		server = argServer ?? {Server.default};
		messages = argMessages;
		starttime = argStarttime;
		}
	
	addMessage {arg ... newMessages;
		messages = messages ++ newMessages;
		}
		
	bundle {
		arg bundle;
		bundle = [starttime];
		messages.do({arg me; bundle = bundle.add(me)});
		^bundle;
		}
		
	msgBundle {
		arg bundle;
		bundle = [];
		messages.do({arg me; bundle = bundle.add(me)});
		^bundle;
		}
	}