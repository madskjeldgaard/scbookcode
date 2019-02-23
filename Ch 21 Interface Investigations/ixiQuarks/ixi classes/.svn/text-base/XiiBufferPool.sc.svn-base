
XiiBufferPool {	
		var <>xiigui;
		var <>win, params;
		
		var bufferList, bufferListNames, bufferListSelections;
		var s, <name, point;
		var recordingName, recButton, r, filename, timeText, secTask, inbus, numChannels;
		var stereoButt, monoButt, preRecButt;
		var <bufferPoolNum;
		var soundFileWindowsList, ram, ramview, fileramview;
		var cmdPeriodFunc;
		var txtv, ram, loadBufTask;
		var ampslider, ampAnalyserSynth, responder, vuview, amp;
		
	*new { arg server, channels, setting=nil, poolname;
		^super.new.initXiiBufferPool(server, channels, setting, poolname);
		}
		
	initXiiBufferPool {arg server, channels, setting, poolname=nil;
		var nameView, folderButt, fileButt, viewButt, saveButt;

		var bgColor, foreColor, spec, outbus;
		var refreshButton, playButton, r, filename, timeText, secTask;
		var soundfile, player, buffer, task, volSlider, openButton, volume;
		var folderSounds, addedSoundNames, sounds, soundNames;
		var soundFileWindowsList, stereomonoview;
		
		s = server;
		
		soundFileWindowsList = List.new;
		
		XQ.bufferPoolNum = XQ.bufferPoolNum + 1; // the number of the pool
		bufferPoolNum = XQ.bufferPoolNum;  // make it a local var (it might have increased)
		
		bufferList = List.new;
		bufferListSelections = List.new; // this is for the selections of each buffer
		bufferListNames = []; // Cocoa dialog will fill this.

		name = if(poolname==nil, {("bufferPool"+(bufferPoolNum+1).asString)}, {poolname});
		filename = "";
		inbus = 0;
		numChannels = 2;
		ram = 0;
		amp = 1.0;

		point = if(setting.isNil, {XiiWindowLocation.new(name)}, {setting[1]});
		xiigui = nil; // not using window server class here
		params = if(setting.notNil, {setting[2]});
		// the params are loaded from XiiQuarks at the bottom of this function

		bgColor = Color.new255(155, 205, 155);
		foreColor = Color.new255(103, 148, 103);
		outbus = 0;
		
		win = SCWindow.new(name, Rect(point.x, point.y, 220, 195), resizable:false);

		SCStaticText(win, Rect(10, 0, 40, 12))
			.font_(Font("Helvetica", 9))
			.string_("ram use:");

		ramview = SCStaticText(win, Rect(50, 0, 28, 12))
			.font_(Font("Helvetica", 9))
			.string_("0");

		SCStaticText(win, Rect(90, 0, 40, 12))
			.font_(Font("Helvetica", 9))
			.string_("file size:");

		fileramview = SCStaticText(win, Rect(130, 0, 28, 12))
			.font_(Font("Helvetica", 9))
			.string_("0");

		stereomonoview = SCStaticText(win, Rect(180, 0, 40, 12))
			.font_(Font("Helvetica", 9))
			.string_("");

		txtv = SCListView(win, Rect(10,15, 200, 145))
			.items_(bufferListNames)
			.background_(Color.new255(155, 205, 155, 60))
			.hiliteColor_(Color.new255(103, 148, 103)) //Color.new255(155, 205, 155)
			.selectedStringColor_(Color.black)
			.enterKeyAction_({|sbs|
				if(txtv.items.size > 0, {
					soundFileWindowsList.add(
						XiiSoundFileView.new(
							bufferList[sbs.value].path, 
							bufferList[sbs.value].bufnum,
							sbs.value, name, 
							bufferListSelections[sbs.value]);
					);
				});
			})
			.action_({ arg sbs; var f, filesize;
				if(bufferListNames.size>0, {
					f = SoundFile.new;
					f.openRead(bufferList[sbs.value].path);
					filesize = f.numFrames * f.numChannels * 2 * 2;
					stereomonoview.string_(if(f.numChannels == 1, {"mono"},{"stereo"}));
					fileramview.string_((filesize/1000/1000).round(0.01));
					f.close;
				});
			});

		saveButt = SCButton(win, Rect(31, 167, 40, 18))
			.states_([["save",Color.black, Color.clear]])
			.font_(Font("Helvetica", 9))			
			.canFocus_(false)
			.action_({ 
				XQ.globalWidgetList.add(
					XiiPoolManager.new(Server.default, nil, nil,
					Rect(win.bounds.left+win.bounds.width+10, win.bounds.top, 160, 80), 
					this);
				);
			});	

		fileButt = SCButton(win, Rect(74, 167, 40, 18))
			.states_([["free",Color.black, Color.clear]])
			.font_(Font("Helvetica", 9))			
			.canFocus_(false)
			.action_({ arg butt; var a, chNum, dur, filepath, pFunc;
				soundFileWindowsList.do(_.close);
				bufferList.do(_.free);
				bufferList = List.new;
				ram = 0;
				bufferListSelections = List.new;
				bufferListNames = [];
				txtv.items_(bufferListNames);
				XQ.globalBufferDict.add(name.asSymbol -> 0);
			});	
				
		folderButt = SCButton(win, Rect(117, 167, 55, 18))
			.states_([["add file(s)",Color.black, Color.clear]])
			.font_(Font("Helvetica", 9))
			.focus(true)			
			.action_({ arg butt;
				CocoaDialog.getPaths({arg paths;
					this.loadBuffers(paths);
				});
			});	
			
		viewButt = SCButton(win, Rect(175, 167, 34, 18))
			.states_([["view",Color.black, Color.clear]])
			.font_(Font("Helvetica", 9))		
			.canFocus_(false)
			.action_({ arg butt;
				if(txtv.items.size > 0, {
					soundFileWindowsList.add(
						XiiSoundFileView.new(
								bufferList[txtv.value].path, 
								bufferList[txtv.value].bufnum,
								txtv.value,  // the number of the buffer in the list
								name,
								bufferListSelections[txtv.value]);
						);
				});
			});	

		preRecButt = SCButton(win, Rect(10, 167, 18, 18))
			.states_([["R",Color.black, Color.clear], ["r",Color.black, Color.clear]])
			.font_(Font("Helvetica", 9))
			.canFocus_(false)
			.action_({ arg butt;
				if(butt.value == 1, {
					"vuu meter on".postln;
					win.bounds_(Rect(win.bounds.left, win.bounds.top-90, 222, 285));
					ampAnalyserSynth = Synth(\xiiVuMeter, 
									[\inbus, inbus, \amp, amp], addAction:\addToTail);
					responder = OSCresponderNode(s.addr,'/tr',{ arg time, responder, msg;
						if (msg[1] == ampAnalyserSynth.nodeID, {
							{ 
								win.isClosed.not.if({ // if window is not closed, update...
									vuview.value = \amp.asSpec.unmap(msg[3]);
								});
							}.defer;
						});
					}).add;

				}, {
					win.bounds_(Rect(win.bounds.left, win.bounds.top+90, 222, 195));
					ampAnalyserSynth.free; // kill the analyser
					responder.remove;
				});	
			});	

		stereoButt = OSCIIRadioButton(win, Rect(10,205,14,14), "stereo")
						.value_(1)
						.font_(Font("Helvetica", 9))
						.action_({ arg butt;
								if(butt.value == 1, {
								numChannels = 2;
								monoButt.value_(0);
								});
						});

		monoButt = OSCIIRadioButton(win, Rect(100,205,14,14), "mono ")
						.value_(0)
						.font_(Font("Helvetica", 9))
						.action_({ arg butt;
								if(butt.value == 1, {
								numChannels = 1;
								stereoButt.value_(0);
								});
						});

		recordingName = SCTextView(win, Rect(10, 225, 140, 16))
				.hasVerticalScroller_(false)
				.autohidesScrollers_(true)
				.string_(filename);

		recButton = SCButton(win, Rect(104, 250, 46, 16))
			.states_([["Record",Color.black, Color.clear], ["Stop",Color.red,Color.red(alpha:0.2)]])
			.font_(Font("Helvetica", 9))
			.canFocus_(false)
			.action_({ arg butt; var file, f, filesize, buffer;
				if(s.serverRunning == true, { // if the server is running
					if(butt.value == 1, {
						filename = recordingName.string;
						if(filename == "", {
							filename = Date.getDate.stamp.asString;
							recordingName.string_(filename);
						});
		
						r = XiiRecord(s, inbus, numChannels);
						r.start("sounds/ixiquarks/"++filename++".aif");
						r.setAmp_(amp);
						secTask.start;
					}, {
						r.stop;
						secTask.stop;

						file = "sounds/ixiquarks/"++filename++".aif";
						buffer = Buffer.read(s, file);
						bufferList.add(buffer);
						bufferListNames = bufferListNames.add(file.basename);
						txtv.items_(bufferListNames);
						recordingName.string_(filename = PathName(filename).nextName);
						XQ.globalBufferDict.add(name.asSymbol -> [bufferList, bufferListSelections]);
						txtv.focus(true);
						txtv.value_(txtv.items.size-1);
						if(s.serverRunning, {
						loadBufTask = Task({
							inf.do({ arg i;
							if(bufferList[bufferList.size-1].numChannels != nil, {
								// get soundfile frames when loaded into buffer
								f = SoundFile.new;
								f.openRead(file);									filesize = f.numFrames * f.numChannels * 2 * 2;
								ram = ram + (filesize/1000/1000).round(0.01);
								{ramview.string_(ram.asString)}.defer;
								bufferListSelections.add([0, f.numFrames]);
								f.close;
								{this.sendBufferPoolToWidgets}.defer;
								loadBufTask.stop;
							});
							0.1.wait; 
							});
						}).start;
						});
					});
				}, {
					XiiAlert("ixi alert: you need to start the server in order to record");
					recButton.value_(0);
				});
			});	
		
		timeText = SCStaticText(win, Rect(64, 250, 40, 16))
					.string_("00:00");

		// the vuuuuu meter
		vuview = XiiVuView(win, Rect(162, 205, 46, 37))
				.canFocus_(false);

		ampslider = OSCIISlider.new(win, Rect(162, 250, 46, 10), "vol", 0, 1, 1, 0.001, \amp)
			.font_(Font("Helvetica", 9))
			.action_({arg sl; 
				amp = sl.value;
				ampAnalyserSynth.set(\amp, amp);
				if(recButton.value == 1, { r.setAmp_(amp); });
			});

		// record busses
		SCPopUpMenu(win, Rect(10, 250, 44, 16))
			.items_(XiiACDropDownChannels.getStereoChnList)
			.value_(0)
			.font_(Font("Helvetica", 9))
			.background_(Color.white)
			.canFocus_(false)
			.action_({ arg ch;
				inbus = ch.value * 2;
				ampAnalyserSynth.set(\inbus, inbus);
				if(recButton.value == 1, { r.inbus_(inbus);});
			});
			
		// updating the seconds text		
		secTask = Task({var sec, min, secstring, minstring;
			sec = 0;
			min = 0;
			inf.do({arg i; 
				sec = sec + 1;
				if(sec > 59, {min = min+1; sec = 0;});
				if(min < 10, {minstring = "0"++min.asString}, {minstring = min.asString});
				if(sec < 10, {secstring = "0"++sec.asString}, {secstring = sec.asString});
				{timeText.string_(minstring++":"++secstring)}.defer;
				1.wait;
			});
		});

		cmdPeriodFunc = { recButton.valueAction_(0);};
		CmdPeriod.add(cmdPeriodFunc);

		win.front;
		win.onClose_({
			var t;
			recButton.valueAction_(0); // stop recording
			CmdPeriod.remove(cmdPeriodFunc);
			soundFileWindowsList.do(_.close);
			bufferList.do(_.free);
			loadBufTask.stop;
			
			try{XQ.globalBufferDict.removeAt(name.asSymbol)};
			this.sendBufferPoolToWidgets;
			
			XQ.globalWidgetList.do({arg widget, i; if(widget === this, { t = i})});
			try{XQ.globalWidgetList.removeAt(t)};
			// write window position to archive.sctxar
			point = Point(win.bounds.left, win.bounds.top);
			XiiWindowLocation.storeLoc(name, point);
		});
		
		// This is taken care of here because GUI items have to be instantiated first
		if(setting.notNil, {
			this.setName_(params[0]); // name
			this.loadBuffers(params[1], params[2]); // bufferpaths, selections
		});
	}
	
	loadBuffers {arg paths, selections; var f, filesize, buffer;
		paths.do({ arg file;
			file.postln;
			f = SoundFile.new;
			f.openRead(file);
			buffer = Buffer.read(s, file);
			bufferList.add(buffer);
			// if loading from Cocoa Dialog, then all file is selected:
			if(selections.isNil, {bufferListSelections.add([0, f.numFrames])});
			bufferListNames = bufferListNames.add(file.basename);
			// size = frames * channels * bytes * 16 to 32 bit converson
			filesize = f.numFrames * f.numChannels * 2 * 2;
			ram = ram + (filesize/1000/1000).round(0.01);
			f.close;
		});
		txtv.items_(bufferListNames);
		txtv.focus(true);
		
		// if loading from PoolManager, then supply selection list:
		if(selections.notNil, {bufferListSelections = selections});

		XQ.globalBufferDict.add(name.asSymbol -> [bufferList, bufferListSelections]);

		ramview.string_(ram.asString);
				
		if(s.serverRunning, {
			loadBufTask = Task({
				inf.do({arg i;
				if(bufferList[bufferList.size-1].numChannels != nil, {
					this.sendBufferPoolToWidgets;
					loadBufTask.stop;
				});
				"loading buffers ->  ".post; (i*100).post; " milliseconds".postln;
				0.1.wait; 
				});
			}).start;
		});
	}
	
	sendBufferPoolToWidgets {
		" - > sending pool buffers to all active quark instruments < -".postln;
		XQ.globalWidgetList.do({arg widget;
			{ // the various widgets that receive and use bufferpools
			if(widget.isKindOf(XiiBufferPlayer), {widget.updatePoolMenu;});
			if(widget.isKindOf(XiiGrainBox), {widget.updatePoolMenu;});
			if(widget.isKindOf(XiiPredators), {widget.updatePoolMenu;});
			if(widget.isKindOf(XiiPolyMachine), {widget.updatePoolMenu;});
			if(widget.isKindOf(XiiGridder), {widget.updatePoolMenu;});
			if(widget.isKindOf(XiiSoundScratcher), {widget.updatePoolMenu;});
			if(widget.isKindOf(XiiMushrooms), {widget.updatePoolMenu;});
			if(widget.isKindOf(XiiSounddrops), {widget.updatePoolMenu;});
			}.defer;
		});
	}

	getFilePaths {
		var pathList;
		pathList = bufferList.collect({arg buffer; buffer.path });
		^pathList;
	}
	
	// a method necessary because .asCompileString does not work like I thought
	getFilePathsString {
		var pathList;
		pathList = bufferList.collect({arg buffer; '"'++buffer.path++'"'});
		^pathList;
	}
	
	setName_ {arg argname;
		name = argname;
		win.name_(name);
		this.sendBufferPoolToWidgets;
	}

	getState { // for saving settings/presets - called from the XiiQuarks GUI
		var point;		
		point = Point(win.bounds.left, win.bounds.top);
		
//		"as compile string".postln;
//		Post << this.getFilePaths.asCompileString;
//		"get file path string".postln;
//		Post << this.getFilePathsString;
		
		//params = [name.asCompileString, this.getFilePaths.asCompileString, bufferListSelections];
		params = [name.asString, this.getFilePaths, bufferListSelections];
		^[2, point, params]; // channels, point, params
	}
}

