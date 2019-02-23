
// called from each effect in the XiiEffects.sc file
// NOTE: The creator argument is the effect object which
// is necessary to have here on closing the window in order to 
// free the reference to the object in the XQ.globalWidgetList

// the setting argument is passed from the settings of XiiQuarks.sc
// if the gui is run from a setting

XiiEffectGUI {
	var <>win; 
	var slider, specs, param, channels, inbus, outbus, tgt, addAct;
	var bufnum, synth, fxOn;
	
	*new { arg name, synthdef, param, ch, creator, setting=nil;
		^super.new.initGUI(name, synthdef, param, ch, creator, setting);
		}
		
	initGUI { arg name, synthdef, par, ch, creator, setting; 
		var lay, moveSynth;
		var nodeLabel, help, synthParams, point, stereoChList, monoChList;
		var onOffButt, cmdPeriodFunc;
		
		param = par; // I need to return this from a func so put into a var
		tgt = if(setting.notNil, {setting[3]}, {1});
		inbus = if(setting.notNil, {setting[1]}, {0}); 
		outbus = if(setting.notNil, {setting[2]}, {0}); 
//		addAct = if(setting.notNil, {setting[4]}, {\addToTail}); 
		addAct = \addToTail; 
		fxOn = if(setting.notNil, {setting[6]}, { false }); 
		slider = Array.newClear(param[0].size); 
		bufnum = nil;
		specs = param[2];
		
		// mono or stereo?
		channels = ch;
		stereoChList = XiiACDropDownChannels.getStereoChnList;
		monoChList = 	 XiiACDropDownChannels.getMonoChnList;
					
		point = if(setting.isNil.not, {setting[4]}, {XiiWindowLocation.new(name)});
		
		win = SCWindow(name, 
				Rect(point.x, point.y, 310, (param[0].size * 20) + 50), 
				resizable:false); 
		win.view.decorator = lay = FlowLayout(win.view.bounds, 5@5, 5@5); 

		SCStaticText(win, 12 @ 15).font_(Font("Helvetica", 9)).string_("in").align_(\right); 
		SCPopUpMenu(win, 40 @ 15)
			.items_(if(channels==1, {monoChList},{stereoChList}))
			.value_(inbus/ch) 
			.font_(Font("Helvetica", 9))
			.background_(Color.white)
			.canFocus_(false)
			.action_({ arg ch;
				if(channels==1, {inbus = ch.value}, {inbus = ch.value * 2});
				if (fxOn, { synth.set(\inbus, inbus) });
			});

		SCStaticText(win, 14 @ 15).font_(Font("Helvetica", 9)).string_("out").align_(\right); 
		SCPopUpMenu(win, 40 @ 15)
			.items_(if(channels==1, {monoChList},{stereoChList}))
			.value_(outbus/ch)
			.font_(Font("Helvetica", 9))
			.background_(Color.white)
			.canFocus_(false)
			.action_({ arg ch;
				if(channels==1, {outbus = ch.value}, {outbus = ch.value * 2});
				if (fxOn, { synth.set(\outbus, outbus) });
			});
			
		SCStaticText(win, 15 @ 15).font_(Font("Helvetica", 9)).string_("Tgt").align_(\right); 
		SCNumberBox(win, 25 @ 15).font_(Font("Helvetica", 9)).value_(tgt).action_({|v| 
		Ê Êv.value = 0.max(v.value); 
		Ê Êtgt = v.value.asInteger; 
		Ê ÊmoveSynth.value; 
		}); 
		
		SCPopUpMenu(win, 60@15) 
		Ê Ê.font_(Font("Helvetica", 9)) 
		Ê Ê.items_(["addToHead", "addToTail", "addAfter", "addBefore"]) 
		Ê Ê.value_(1) 
		Ê Ê.action_({|v| 
		Ê ÊÊ ÊaddAct = v.items.at(v.value).asSymbol; 
		Ê ÊÊ ÊmoveSynth.value; 
		Ê Ê}); 
		Ê Ê 
		SCButton(win,12@15) 
		Ê Ê.font_(Font("Helvetica", 9)) 
		Ê Ê.states_([["#"]]) 
		Ê Ê.action_({|v| 
		Ê ÊÊ ÊÊ ÊsynthParams = Array.new; 
		Ê ÊÊ ÊÊ ÊsynthParams = synthParams.add(param[1]); // synth params 
		Ê ÊÊ ÊÊ ÊsynthParams = synthParams.add(param[3]); // argument values 
		Ê ÊÊ ÊÊ ÊsynthParams = ['inbus', inbus].add(synthParams.flop).flat; 
		Ê ÊÊ ÊÊ ÊsynthParams = ['outbus', outbus].add(synthParams.flop).flat.asCompileString; 
		Ê ÊÊ ÊÊ Ê("Synth.new(\\" ++ synthdef ++ ", " ++ synthParams ++ 
		Ê ÊÊ ÊÊ ÊÊ Ê", target: " ++ tgt ++ ", addAction: \\" ++ addAct ++ ")").postln; 
		Ê Ê}); 
		
		onOffButt = SCButton(win, 30@15) 
		Ê Ê.font_(Font("Helvetica", 9)) 
		Ê Ê.states_([["On", Color.black, Color.clear], 
					["Off", Color.black, Color.green(alpha:0.2)]]) 
		Ê Ê.action_({|v| 
		Ê ÊÊ Êif ( v.value == 0, { 
		Ê ÊÊ ÊÊ ÊfxOn = false; 
		Ê ÊÊ ÊÊ ÊnodeLabel.string = "none"; 
		Ê ÊÊ ÊÊ Êsynth.free;
		Ê ÊÊ Ê},{ 
		Ê ÊÊ ÊÊ ÊfxOn = true; 
		Ê ÊÊ ÊÊ ÊsynthParams = Array.new; 
		Ê ÊÊ ÊÊ ÊsynthParams = synthParams.add(param[1]); // synth params 
		Ê ÊÊ ÊÊ ÊsynthParams = synthParams.add(param[3]); // argument values 
		Ê ÊÊ ÊÊ ÊsynthParams = ['inbus', inbus].add(synthParams.flop).flat; 
		Ê ÊÊ ÊÊ ÊsynthParams = ['outbus', outbus].add(synthParams.flop).flat; 
		Ê ÊÊ ÊÊ Êsynth = Synth.new(synthdef, synthParams, target: tgt.asTarget, addAction: addAct); 
		Ê ÊÊ ÊÊ ÊnodeLabel.string = synth.nodeID;
		Ê ÊÊ Ê }) 
		Ê Ê}); 
				
		param[0].size.do({|i| 
		Ê Êslider[i] = EZSlider(win, 288@15, param[0][i], param[2][i], 
									labelWidth: 50, numberWidth: 40); 
		Ê Êslider[i].labelView.font_(Font("Helvetica", 9)); 
		Ê Êslider[i].numberView.font_(Font("Helvetica", 9)); 
		Ê Êslider[i].sliderView.background_(Gradient(Color.new255(103, 148, 103, 0), 
		Ê ÊÊ ÊColor.new255(103, 148, 103, 200), \h, 31)); 
		Ê Êslider[i].action = {|v| 
		Ê ÊÊ Êparam[3][i] = v.value; 
		Ê ÊÊ Êif (fxOn, { synth.set(param[1][i], v.value) }) 
		Ê Ê }; 
		Ê Êslider[i].value = param[3][i]; 
		Ê Êlay.nextLine; 
		}); 
		
		// look for a buffer.bufnum - in those effects that use buffers (mrroque, multidelay)
		param[1].size.do({|i| 
			if(param[1][i] == \bufnum, { //"buffer found".postln; 
				bufnum = param[3][i];
			});
		});

		SCStaticText(win,50 @ 15).font_(Font("Helvetica", 9)).align_(\right).string_("nodeID"); 
		nodeLabel = SCStaticText(win,50 @ 15)
				.font_(Font("Helvetica", 9))
				.align_(\left).string_("none"); 
		moveSynth = { 
		Ê Êif ( fxOn, { 
		Ê ÊÊ Êcase 
		Ê ÊÊ ÊÊ Ê{ addAct === \addToHead }Ê { synth.moveToHead(tgt.asTarget) } 
		Ê ÊÊ ÊÊ Ê{ addAct === \addToTail }Ê { synth.moveToTail(tgt.asTarget) } 
		Ê ÊÊ ÊÊ Ê{ addAct === \addAfterÊ }Ê { synth.moveAfter(tgt.asTarget)Ê } 
		Ê ÊÊ ÊÊ Ê{ addAct === \addBefore }Ê { synth.moveBefore(tgt.asTarget) } 
		Ê Ê}) 
		}; 
		win.view.keyDownAction = { arg ascii, char; 
		Ê Êcase 
		Ê ÊÊ Ê{char == $n} { Server.default.queryAllNodes } 
		}; 
		
		cmdPeriodFunc = { onOffButt.valueAction_(0)};
		CmdPeriod.add(cmdPeriodFunc);

		win.onClose_({
			var t;
			if (fxOn, { synth.free; });
			CmdPeriod.remove(cmdPeriodFunc);
			if(bufnum != nil, { // if the effect is using a buffer
				Server.default.sendMsg(\b_free, bufnum);
				Server.default.bufferAllocator.free(bufnum);
			});
			
			XQ.globalWidgetList.do({arg widget, i; if(widget === creator, { t = i})});
			try{XQ.globalWidgetList.removeAt(t)};

			// write window position to archive.sctxar
			point = Point(win.bounds.left, win.bounds.top);
			XiiWindowLocation.storeLoc(name, point);
		}); 
		
		// on or off?
		if(fxOn, { onOffButt.valueAction_(1) });

		win.front; 
	} // end of initGui
	
	setSlider_ {arg slnum, val;
		if((slnum>=0) && (slnum<slider.size), {
			slider[slnum].valueAction_(val);
		});
	}
	
	getState {
		var point;
		point = Point(win.bounds.left, win.bounds.top);
		^[channels, inbus, outbus, tgt, point, param[3], fxOn];
	}
	
}


