// should this hold the list (i.e. for buffer playback), or load it lazily either from an archive array or the original SDIF?

// better solution for fade times: Make fadeins fadein * stretch.reciprocal. This will make the time constant so that onset phase is correct. Should be able to optimise it so it only happens once!

LorisPartials {
	classvar <dir = "/Users/scottw/lorisdefs/";
	var <name, parentPath, defs;
	var <thisDir, <size, <>times, <phases, <partialList, <dur;
	var <servers, dataDict;

	*new { arg name, parentPath, loadData = true;	
		^super.newCopyArgs(name, parentPath).init(loadData);
	}
	
	*dir_ {arg path; if(path.last == $/, { dir = path; }, { dir = path ++ $/ });}
	
	init { arg loadData;
		var dataPath, arch;
		servers = Dictionary.new; // stores servers
		if(parentPath.notNil, {
			if(parentPath.last != $/, {parentPath = parentPath ++ "/"});
			thisDir = parentPath ++ name ++ "/";}, 
			{
			thisDir = dir ++ name ++ "/";
			});
		defs = (thisDir ++ name ++ "-" ++ "*.scsyndef").pathMatch;
		//if( defs.size == 0, { "No defs found. Build?".warn; ^this });
		//size = defs.size;
		//(size.asString ++ " defs found for " ++ name).inform;
		times = Object.readTextArchive(thisDir ++ name ++ "-times");
		phases = Object.readTextArchive(thisDir ++ name ++ "-phases");
		size = phases.size;
		// archive issues
		loadData.if({
			dataPath = thisDir ++ name ++ "-zdata";
			File.exists(dataPath).if({
				arch = ZArchive.read(thisDir ++ name ++ "-zdata");
				dataDict = arch.readItem(IdentityDictionary);
				arch.close;
			}, { dataDict = Object.readTextArchive(thisDir ++ name ++ "-data");}); //legacy
			
			dataDict.notNil.if({
				partialList = dataDict[\partiallist];
				dur = dataDict[\dur];
			});
		});
	}
	
	// only load once
	loadDefs { |server|
		if(servers[server.addr].isNil, { 
			server.loadDirectory(thisDir); 
			servers[server.addr] = server;
			server.addDependant(this);
		});
	}
	
	// load defs whenever a server boots
	update { arg server, what;
		if(what == \serverRunning, {
			if(server.serverRunning == true, {server.loadDirectory(thisDir);});
		});
	}
	
	clearServers { 
		servers.values.do({|server| server.removeDependant(this) });
		servers = Dictionary.new; 
	}
	
	*build { arg sdifFile, name, defDir, fade = true;
		var list, times, phases, dataDict, dur = 0, arch;
		name = name.asString;		
//		if(pathOrList.isString, {list = thisProcess.interpreter.executeFile(pathOrList)},
//			{list = pathOrList});
		list = sdifFile.readFramesToPartials;		
		// name, fade, faded dur, unfaded partiallist
		dataDict = IdentityDictionary[\name->name, \fade->fade, \partiallist->list.deepCopy];
		fade.if({this.fadeInOut(list)});
		// calculate dur
		list.do({|item| 
			var end;
			end = item[2].sum + item[0]; // duration
			dur = dur.max(end);
		});
		dataDict[\dur] = dur;
		times = Array.new(list.size);
		phases = Array.new(list.size);
		("Building Defs for " ++ name).inform;
		if(defDir.notNil, { if(defDir.last != $/, {defDir = defDir ++ "/"}) });
		defDir = (defDir ?? dir) ++ name.asString ++ "/";
		// use systemCmd for no blocking
		("mkdir -p " ++ $" ++ defDir.standardizePath ++ $").systemCmd; 
		list.do({ arg item, i;
			phases.add(item[1]);
			SynthDef(name.asString ++ "-" ++ i.asString, {
				arg i_lsFreq, i_lsAmp, i_lsBw, lsStretch = 1.0, lsPitch = 1.0, lsBandScal = 1.0, 
					lsDoneAction = 0;
				var freq, amp, bw;
				freq = EnvGen.ar(Env.new(item[4], item[2]), 1, lsPitch, 0, lsStretch, 0);
				bw = EnvGen.ar(Env.new(item[5], item[2]), 1, lsBandScal, 0, lsStretch, 0);
				amp = EnvGen.ar(Env.new(item[3], item[2]), 1, 1, 0, lsStretch, lsDoneAction);
				OffsetOut.ar(i_lsFreq, freq);
				OffsetOut.ar(i_lsAmp, amp);
				OffsetOut.ar(i_lsBw, bw);
			}).writeDefFile(defDir);
		});
		("Built" + list.size + "defs").inform;
		list.doAdjacentPairs({|item, next| times.add(next[0] - item[0]) });
		times.add(0); // for last wait
		times.writeTextArchive(defDir ++ name ++ "-times");
		phases.writeTextArchive(defDir ++ name ++ "-phases");
		//dataDict.writeTextArchive(defDir ++ name ++ "-data");
		arch = ZArchive.write(defDir ++ name ++ "-zdata");
		arch.writeItem(dataDict);
		arch.writeClose;
		//if(pathOrList.isString, {("cp" + pathOrList + defDir).systemCmd}); // copy original list
		
	}

	oaFrames { arg hop = 0.05, trimSilentStart = false;
		var oaList, numFrames, oaFrames, maxDur = 0;
		// amp, freq, bw, start, end
		oaList = partialList.collect({|item| 
			var end;
			end = item[2].sum + item[0]; // duration
			maxDur = maxDur.max(end);
			[Env(item[3], item[2]),Env(item[4], item[2]), Env(item[5], item[2])]
				.collect({|env| env.delay(item[0])})
				.addAll([item[0], end]); // start, end 
		});
		numFrames = floor(maxDur / hop);
		oaFrames = Array.new(numFrames);
		numFrames.do({|i|
			var now, frame;
			now = hop * i;
			frame = List.new;
			oaList.do({|item| 
				if((now >= item[3]) && (now <= item[4]) && (item[0].at(now) > 0.0), 
					{frame.add([item[0].at(now), item[1].at(now), item[2].at(now)]);});
			});
			oaFrames.add(frame.asArray);
		});
		trimSilentStart.if({
			while({oaFrames.first.size == 0}, {oaFrames.remove(oaFrames.first)});
		});
		^oaFrames;
	}
	
	// fades in or out partials with non-zero start and/or end amps
	*fadeInOut { arg list;
		var fadein = 0.001, fadeout = 0.01;
		list.do({ arg partial;
			// fadein
			if(partial[3].first > 0,{
				partial[0] = partial[0] - fadein; // roll back start slightly
				partial[1] = partial[1] - (2pi * partial[4].first * fadein); // roll back phase
				partial[2] = partial[2].insert(0, fadein); // short fadein time segment
				partial[3] = partial[3].insert(0, 0); // amp zero
				partial[4] = partial[4].insert(0, partial[4].first); // extra freq
				partial[5] = partial[5].insert(0, partial[5].first); // extra bw
			});
			
			// fadeout
			if(partial[3].last > 0,{
				partial[2] = partial[2].add(fadeout); // short fadeout segment
				partial[3] = partial[3].add(0); // amp zero
				partial[4] = partial[4].add(partial[4].last); // extra freq
				partial[5] = partial[5].add(partial[5].last); // extra bw
			});
		});
	
	}
	
	// correct old versions
	*buildFromList { arg list, name, defDir, fade = true;
		var times, phases, dataDict, dur = 0;
		name = name.asString;		
		if(list.isString, {list = thisProcess.interpreter.executeFile(list)});
		//list = sdifFile.readFramesToPartials;		
		// name, fade, faded dur, unfaded partiallist
		dataDict = IdentityDictionary[\name->name, \fade->fade, \partiallist->list.deepCopy];
		fade.if({this.fadeInOut(list)});
		// calculate dur
		list.do({|item| 
			var end;
			end = item[2].sum + item[0]; // duration
			dur = dur.max(end);
		});
		dataDict[\dur] = dur;
		times = Array.new(list.size);
		phases = Array.new(list.size);
		("Building Defs for " ++ name).inform;
		if(defDir.notNil, { if(defDir.last != $/, {defDir = defDir ++ "/"}) });
		defDir = (defDir ?? dir) ++ name.asString ++ "/";
		// use systemCmd for no blocking
		("mkdir -p " ++ $" ++ defDir.standardizePath ++ $").systemCmd; 
		list.do({ arg item, i;
			phases.add(item[1]);
			SynthDef(name.asString ++ "-" ++ i.asString, {
				arg i_lsFreq, i_lsAmp, i_lsBw, lsStretch = 1.0, lsPitch = 1.0, lsBandScal = 1.0, 
					lsDoneAction = 0;
				var freq, amp, bw;
				freq = EnvGen.ar(Env.new(item[4], item[2]), 1, lsPitch, 0, lsStretch, 0);
				bw = EnvGen.ar(Env.new(item[5], item[2]), 1, lsBandScal, 0, lsStretch, 0);
				amp = EnvGen.ar(Env.new(item[3], item[2]), 1, 1, 0, lsStretch, lsDoneAction);
				OffsetOut.ar(i_lsFreq, freq);
				OffsetOut.ar(i_lsAmp, amp);
				OffsetOut.ar(i_lsBw, bw);
			}).writeDefFile(defDir);
		});
		("Built" + list.size + "defs").inform;
		list.doAdjacentPairs({|item, next| times.add(next[0] - item[0]) });
		times.add(0); // for last wait
		times.writeTextArchive(defDir ++ name ++ "-times");
		phases.writeTextArchive(defDir ++ name ++ "-phases");
		dataDict.writeTextArchive(defDir ++ name ++ "-data");
		//if(pathOrList.isString, {("cp" + pathOrList + defDir).systemCmd}); // copy original list
		
	}
	
}