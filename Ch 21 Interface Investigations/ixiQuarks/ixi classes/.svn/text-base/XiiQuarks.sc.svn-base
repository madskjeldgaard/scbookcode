// XiiQuarks dependencies:
// - midiname (wslib)

// IMPORTANT IN THIS CLASS:
// XQ.globalWidgetList
// XQ.globalBufferDict - a.keys (.asArray)
// all the environmental stuff are stored in classvars in the XQ class
// the presets are handled in a class called XiiSettings

// NOTE: BufferPlayer uses TriggerIDs from 50 to (number of instances * 50)
// AudioIn uses TriggerID number 800
// Recorder uses TriggerID nr 820
// Mushrooms uses TriggerID nr 840

// NEW IN VERSION 2:
// Amp slider in Recorder and in BufferPool
// Open sounds folder in Player
// two new instruments: LiveBuffers and Mushrooms
// Fixing loading of bufferpools (instruments would automatically load new sound)

// NEW IN VERSION 3:
// store settings
// bug fixes

// NEW IN VERSION 4:
// Relative tempi in PolyMachine and an increase up to 48 steps per track
// User definable number of tracks in PolyMachine
// User definable number of tracks in BufferPlayer
// PolyMachine: Fixing sc-code such that one does not have to submit code for each box
// BufferPool soundfile view now displays selections in the soundfile
// Fixing Gridder (the params argument so the transpose is set to 1 again)
// Fixing loadup of synthdefs in PolyMachine (removing from server)
// Optimising the distribution code
// Record fixed
// Fixing the route ordering of channels - now no need to restart effects
// Fixing amplifier
// Settings store bufferpools and their contents
// Effects remember their on/off state
// Refining small functions in SoundScratcher
// Fixing settings in the Quarks interface
// BufferPool and Recorder now get a new logical filename in text field when recording stops
// some new spectral effects
// new time domain effect called cyberpunk (thanks dan stowell for ugen)
// Added views that display frames, selection start and selection end in BufferPool SndFileView
// optimization of code
// got rid of all environmental variables and store envir vars in the XQ class
// soundfilefolder created on default if it doesn't exist
// new filter: Moog VCF

// NEW IN VERSION 5:
// new instrument: Sounddrops
// new tool: Theory (scales and chords)



// TODO: make Spectral plugins
// TODO: Test the Warp1MC Ugens that take and output multichannel (see mail sept 10, 2007)
// TODO: Make interface to bufferPool so that one can put it into a var like b and live-code
// TODO: Make a mixer channel gui

/*
a = XQ.globalWidgetList[0].xiigui.getState
b = XiiDelay.new(s, 2, a);
	getState {
		^[channels, inbus, outbus, tgt, loc, param[3]];
	}
*/


XiiQuarks {	

	*new { 
		^super.new.initXiiQuarks;
	}
		
	initXiiQuarks {
	
		var win, txtv, quarks, serv, channels;
		var openButt, effectCodeString, monoButt, stereoButt, effect;
		var name, point;
		var midi, midiControllerNumbers, midiRotateWindowChannel, midiInPorts, midiOutPorts;
		var openSndFolder;
		var chosenWidget, effectnum, types, typesview, ixilogo;
		var settingRegister, settingNameView, storeSettingButt, comingFromFieldFlag, settingName;
		var storedSettingsPop, loadSettingButt, deleteSettingButt, clearScreenButt;
		var prefFile, preferences;
		
		settingRegister = XiiSettings.new; // activate the settings registry

		GUI.cocoa;
		//Server.default = Server.local; // EXPERIMENTAL !!!!
		
		XQ.new; // A class containing all the settings and environment maintenance
		
		XQ.preferences; // retrieve preferences from the "preferences.ixi" file
		midi = XQ.pref.midi; // if you want to use midi or not (true or false)
		midiControllerNumbers = XQ.pref.midiControllerNumbers; // evolution mk-449c
		midiRotateWindowChannel = XQ.pref.midiRotateWindowChannel;
		midiInPorts = XQ.pref.midiInPorts;
		midiOutPorts = XQ.pref.midiOutPorts;
		if(XQ.pref.emailSent == false, {
			"open preferences/email.html".unixCmd;
		});

		XiiACDropDownChannels.numChannels_( XQ.pref.numberOfChannels ); // NUMBER OF AUDIO BUSSES

		//////////////////////////////////////////////

		XiiLoadSynthDefs.new(Server.default);
	
		name = " ixi quarks";
		point = XiiWindowLocation.new(name);
		
		win = SCWindow(name, Rect(point.x, point.y, 275, 212), resizable:false).front;
		
		comingFromFieldFlag = false;
		settingName = "preset_0";
		
		quarks = [ 
			["AudioIn", "Recorder", "Player", "BufferPool", "PoolManager", 
			"FreqScope", "WaveScope", "EQMeter", "MixerNode", 
			"ChannelSplitter", "Amplifier", "TrigRecorder", "Theory"],
	
			["SoundScratcher", "StratoSampler", "Sounddrops", "Mushrooms", "Predators", 
			"Gridder", "PolyMachine", "GrainBox", "BufferPlayer", "ScaleSynth"], 
			
			["Delay", "Freeverb", "AdCVerb", "Distortion", "ixiReverb", "Chorus",
			"Octave", "CyberPunk", "Tremolo", "Equalizer", "CombVocoder", "RandomPanner", 
			"MRRoque", "MultiDelay"],
			
			["Bandpass", "Lowpass", "Highpass", "RLowpass", "RHighpass", 
			"Resonant", "Klanks", "MoogVCF", "MoogVCFFF"],
			
			["SpectralEQ", "MagClip", "MagSmear", "MagShift", "MagFreeze", 
			"RectComb", "BinScramble", "BinShift"],
			
			["Noise", "Oscillators"]
		];
		
		types = ["utilities", "instruments", "effects", "filters", "spectral", "other"];
		
		ixilogo = [ // the ixi logo
			Point(1,7), Point(8, 1), Point(15,1), Point(15,33),Point(24, 23), Point(15,14), 
			Point(15,1), Point(23,1),Point(34,13), Point(45,1), Point(61,1), Point(66,6), 
			Point(66,37), Point(59,43), Point(53,43), Point(53,12), Point(44,22), Point(53,33), 
			Point(53,43), Point(42,43), Point(34,32),Point(24,43), Point(7,43), Point(1,36), Point(1,8)
			];

		channels = 2;
		effect = "AudioIn";

		typesview = SCListView(win,Rect(10,10, 120, 96))
			.items_(types)
			.hiliteColor_(XiiColors.darkgreen) //Color.new255(155, 205, 155)
			.background_(XiiColors.listbackground)
			.selectedStringColor_(Color.black)
			.action_({ arg sbs;
				txtv.items_(quarks[sbs.value]);
				txtv.value_(0);
				effect = quarks[sbs.value][txtv.value];
			})
			.enterKeyAction_({|view|
				txtv.value_(0);
				txtv.focus(true);
			});
			
		storedSettingsPop = SCPopUpMenu(win, Rect(10, 116, 78, 16)) // 550
			.font_(Font("Helvetica", 9))
			.canFocus_(false)
			.items_(settingRegister.getSettingsList.sort)
			.background_(Color.white);

		loadSettingButt = SCButton(win, Rect(95, 116, 35, 17))
			.states_([["load", Color.black, Color.clear]])
			.font_(Font("Helvetica", 9))
			.canFocus_(false)
			.action_({arg butt; 
				settingRegister.loadSetting(storedSettingsPop.items[storedSettingsPop.value]);
			});

		settingNameView = SCTextView.new(win, Rect(10, 139, 78, 14))
			.font_(Font("Helvetica", 9))
			.string_(settingName = PathName(settingName).nextName)
			.keyDownAction_({arg view, key, mod, unicode; 
				if(unicode ==13, {
					comingFromFieldFlag = true;
					storeSettingButt.focus(true);
				});
			});
		
		storeSettingButt = SCButton(win, Rect(95, 138, 35, 17))
			.states_([["store", Color.black, Color.clear]])
			.font_(Font("Helvetica", 9))
			.canFocus_(false)
			.action_({arg butt; 
				settingName = PathName(settingNameView.string).nextName;
				settingRegister.storeSetting(settingNameView.string);
				storedSettingsPop.items_(settingRegister.getSettingsList);
				settingNameView.string_(settingName);
			})
			.keyDownAction_({arg view, key, mod, unicode; // if RETURN on bufNameView
				if(unicode == 13, {
					if(comingFromFieldFlag, {
						"not storing setting".postln;
						comingFromFieldFlag = false;
					},{
						settingRegister.storeSetting(settingNameView.string);
						storedSettingsPop.items_(settingRegister.getSettingsList);
					})
				});
				settingName = PathName(settingNameView.string).nextName;
				settingNameView.string_(settingName);
			});

		deleteSettingButt = SCButton(win, Rect(95, 160, 35, 17))
			.states_([["delete", Color.black, Color.clear]])
			.font_(Font("Helvetica", 9))
			.canFocus_(false)
			.action_({arg butt; 
				settingRegister.removeSetting(storedSettingsPop.items[storedSettingsPop.value]);
				storedSettingsPop.items_(settingRegister.getSettingsList);
			});

		clearScreenButt = SCButton(win, Rect(95, 182, 35, 17))
			.states_([["clear", Color.black, Color.clear]])
			.font_(Font("Helvetica", 9))
			.canFocus_(false)
			.action_({arg butt; 
				settingRegister.clearixiQuarks;
			});

		txtv = SCListView(win,Rect(140,10, 120, 152))
			.items_(quarks[0])
			.hiliteColor_(XiiColors.darkgreen) //Color.new255(155, 205, 155)
			.background_(XiiColors.listbackground)
			.selectedStringColor_(Color.black)
			.action_({ arg sbs;
				("Xii"++quarks[typesview.value][sbs.value]).postln;
				effect = quarks[typesview.value][sbs.value];
			})
			.enterKeyAction_({|view|
				effect = quarks[typesview.value][view.value];
				effectCodeString = "Xii"++effect++".new(Server.default,"++channels++")";
				XQ.globalWidgetList.add(effectCodeString.interpret);
			})
			.keyDownAction_({arg view, char, modifiers, unicode;
				if(unicode == 13, {
					effect = quarks[typesview.value][view.value];
					effectCodeString = "Xii"++effect++".new(Server.default,"++channels++")";
					XQ.globalWidgetList.add(effectCodeString.interpret);
				});
				if (unicode == 16rF700, { txtv.valueAction = txtv.value - 1;  });
				if (unicode == 16rF703, { txtv.valueAction = txtv.value + 1;  });
				if (unicode == 16rF701, { txtv.valueAction = txtv.value + 1;  });
				if (unicode == 16rF702, { typesview.focus(true);  });
			});

		stereoButt = OSCIIRadioButton(win, Rect(140, 172, 12, 12), "stereo")
					.value_(1)
					.font_(Font("Helvetica", 9))
					.action_({ arg butt;
							if(butt.value == 1, {
							channels = 2;
							monoButt.value_(0);
							});
					});

		monoButt = OSCIIRadioButton(win, Rect(140, 190, 12, 12), "mono ")
					.value_(0)
					.font_(Font("Helvetica", 9))
					.action_({ arg butt;
							if(butt.value == 1, {
								channels = 1;
								stereoButt.value_(0);
							});	
					});

		openSndFolder = SCButton(win, Rect(195, 178, 13, 18))
				.states_([["f",Color.black,Color.clear]])
				.font_(Font("Helvetica", 9))
				.canFocus_(false)
				.action_({ arg butt;
					"open sounds/ixiquarks/".unixCmd
				});
								
		openButt = SCButton(win, Rect(210, 178, 50, 18))
				.states_([["Open",Color.black,Color.clear]])
				.font_(Font("Helvetica", 9))
				.canFocus_(false)
				.action_({ arg butt;
					effectCodeString = "Xii"++effect++".new(Server.default,"++channels++")";
					XQ.globalWidgetList.add(effectCodeString.interpret);
				});
				
		// MIDI control of sliders		
		if(midi == true, {
			"MIDI is ON".postln;
			MIDIIn.control = { arg src, chan, num, val;
				var wcnt;					
				if(num == midiRotateWindowChannel, {
					{
					wcnt = SCWindow.allWindows.size;
					if(XQ.globalWidgetList.size > 0, {
						"in here".postln;
						chosenWidget = val % wcnt;
						SCWindow.allWindows.at(chosenWidget).front;
						XQ.globalWidgetList.do({arg widget, i;
							if(widget.xiigui.isKindOf(XiiEffectGUI), {
								if(SCWindow.allWindows.at(chosenWidget) === widget.xiigui.win, {
									effectnum = i;
								});
							});
						});
						"and where is the bug?".postln;
					});
					}.defer;
				},{
				{
				XQ.globalWidgetList[effectnum].xiigui.setSlider_(
					midiControllerNumbers.detectIndex({arg i; i == num}), val/127);
				}.defer;
				});
			};
			
			MIDIClient.init(midiInPorts,midiOutPorts);
			midiInPorts.do({ arg i; 
				MIDIIn.connect(i, MIDIClient.sources.at(i));
			});
		});
		
		win.onClose_({ 
			point = Point(win.bounds.left, win.bounds.top);
			XiiWindowLocation.storeLoc(name, point);
		}); 
	
		txtv.focus(true);
		
		win.drawHook = {
			XiiColors.ixiorange.set;
			Pen.width = 3;
			Pen.translate(30,170);
			Pen.scale(0.6,0.6);
			Pen.moveTo(1@7);
			ixilogo.do({arg point;
				Pen.lineTo(point+0.5);
			});
			Pen.stroke
		};
		win.refresh;
	}
}
