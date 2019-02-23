// figure 8.29 - a lightweight graphical user interface for CloudGenMini 
(
q.makeCloudGui = { |q, tdef, posPoint| 
	var w, ezRangers, fdBox; 
	var setMinis, skipjack; 
	
	posPoint = posPoint ? 400@400;	// where to put the gui window
	
	w = Window.new("CloudGen_mini_sc3", 
		Rect.fromPoints(posPoint, (posPoint + (400@320))), false).front;	w.view.decorator_(FlowLayout(w.bounds.copy.moveTo(0, 0)));
		
	w.view.decorator.nextLine; 
		// the range sliders display the current values
	ezRangers = ();
	
	q.paramRNames.do { |name, i| 
		ezRangers.put(name, 
		EZRanger(w, 400@20, name, q.paramNames[i], 
			{ |sl| tdef.envir.current[name] = sl.value; }, 
			tdef.envir.current[name], labelWidth: 70, numberWidth: 50, unitWidth: 10)
			.round_([0.1, 0.00001, 0.0001, 0.0001, 0.01][i])
		);
	};
		// a just in time - gui for the Tdef
	TdefGui(tdef, parent: w); 

	w.view.decorator.nextLine;
	
	Button.new(w, 80@20).states_([[\randomize]])
		.action_({ 
			tdef.envir.target_(d.randSet);
			tdef.envir.morphtask.stop.play;
		});
	
	fdBox = EZNumber.new(w, 110@20, \xFadeTime, [0, 100, \amp], 
		{ |nbx| tdef.envir.xfadeTime = nbx.value }, 
		 tdef.envir.xfadeTime, false, 65); 

			// skipjack is a task that survives cmd-period:
			// used here for lazy-updating the control views.
	skipjack = SkipJack({  
		q.paramRNames.do { |name| ezRangers[name].value_(tdef.envir.current[name]) };
		fdBox.value_(tdef.envir.xfadeTime); 
		
		// mark last settings that were used by color? 
		// a separate color when changed? 
		
	}, 0.5, { w.isClosed }, name: tdef.key);
	
	w.view.decorator.nextLine;

	// make a new layoutView for the 8 presets; 
	// put button to switch to that preset, 
	// a button to save current settings to that place, 
	// and a miniview of the settings as a visual reminder in it.
	
		// make 8 setButtons
	tdef.envir.setNames.do { |setname, i|	
		var minisliders, setMinis;
		var zone = CompositeView.new(w, Rect(0,0,45, 84));
		zone.decorator = FlowLayout(zone.bounds, 0@0, 5@0); 
		zone.background_(Color.white); 
		
		Button.new(zone, Rect(0,0,45,20)).states_([[setname]])
			.action_({ 
				// just switch: // tdef.envir.current.putAll(d[setname] ? ()) 
				tdef.envir.target = tdef.envir[setname];
				tdef.envir.morphtask.stop.play;
			});
		
		Button.new(zone, Rect(0,0,45,20))
			.states_([["save" ++ (i + 1)]])
			.action_({ 
				d[setname] = tdef.envir.current.copy; 
				setMinis.value;
			});
		
		minisliders = q.paramRNames.collect { |paramRname| 
			RangeSlider.new(zone, 45@8).enabled_(false);
		};
		setMinis = { 
			q.paramRNames.do { |paramRname, i| 
				var paramName = q.paramNames[i];
				var myrange = d[setname][paramRname];
				var unmapped = paramName.asSpec.unmap(myrange); 
				minisliders[i].lo_(unmapped[0]).hi_(unmapped[1]);
			} 
		};
		setMinis.value;
	};

/* 	Some extras: 
	a volume slider for simple mixing, 
	a popup menu for switching syndefnames; 
	a button to stop/start the skipjack for refreshing, 
	so one can use numberboxes to enter values.
*/
	EZSlider(w, 245@20, "vol", \amp, { |sl|tdef.set(\vol, sl.value) }, 
		0.25, false, 20, 36);

	StaticText.new(w, 55@20).string_("synthdef:").align_(\right); 
	PopUpMenu.new(w, Rect(0,0,80,20))
		.items_([\gab1st, \gabWide, \percSin, \percSinRev, \percNoise])
		.action_({ |pop| tdef.envir.synName = pop.items[pop.value] });
	
	Button.new(w, 80@20).states_([[\continuous], [\fadeStops]])
		.value_(tdef.envir.stopAfterFade.binaryValue)
		.action_({ |btn|
			tdef.set(\stopAfterFade, btn.value == 1)
		});

	Button.new(w, 80@20).states_([[\skipWatching], [\skipWaiting]])
		.action_({ |btn|
			[ { skipjack.play }, { skipjack.stop }][btn.value].value
		});

};
q.makeCloudGui(Tdef(\cloud0))
);



