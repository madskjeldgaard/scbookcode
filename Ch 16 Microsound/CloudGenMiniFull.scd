// CloudGenMini is based on CloudGenerator, a granular synthesis program 
// by Curtis Roads and John Alexander. 
// This partial miniature version was implemented by Alberto de Campo, 2007. 


	// figure 6.23 - some granular synthdefs and tests
(
	// a gabor (approx. gaussian-shaped) grain
SynthDef(\gab1st, { |out, amp=0.1, freq=440, sustain=0.01, pan| 
	var snd = FSinOsc.ar(freq);
	var env = EnvGen.ar(Env.sine(sustain, amp * AmpComp.ir(freq) * 0.5), doneAction: 2);
	OffsetOut.ar(out, Pan2.ar(snd * env, pan));
}, \ir ! 5).store;

			// wider, quasi-gaussian envelope, with a hold time in the middle. 
SynthDef(\gabWide, { |out, amp=0.1, freq=440, sustain=0.01, pan, width=0.5| 
	var holdT = sustain * width;
	var fadeT = 1 - width * sustain * 0.5;
	var snd = FSinOsc.ar(freq);
	var env = EnvGen.ar(Env([0, 1, 1, 0], [fadeT, holdT, fadeT], \sin), 
		levelScale: amp * AmpComp.ir(freq) * 0.5, 
		doneAction: 2);
	OffsetOut.ar(out, Pan2.ar(snd * env, pan));
}, \ir ! 5).store;

			// a simple percussive envelope
SynthDef(\percSin, { |out, amp=0.1, freq=440, sustain=0.01, pan| 
	var snd = FSinOsc.ar(freq);
	var env = EnvGen.ar(
		Env.perc(0.1, 0.9, amp * AmpComp.ir(freq) * 0.5), 
			timeScale: sustain, doneAction: 2
		);
	OffsetOut.ar(out, Pan2.ar(snd * env, pan));
}, \ir ! 5).store;

			// a reversed  percussive envelope
SynthDef(\percSinRev, { |out, amp=0.1, freq=440, sustain=0.01, pan| 
	var snd = FSinOsc.ar(freq);
	var env = EnvGen.ar(
		Env.perc(0.9, 0.1, amp * AmpComp.ir(freq) * 0.5, 4), 
			timeScale: sustain, doneAction: 2
		);
	OffsetOut.ar(out, Pan2.ar(snd * env, pan));
}, \ir ! 5).store;

		// a noise band grain with percussive envelope
SynthDef(\percNoise, { |out, amp=0.2, freq=440, sustain=0.01, pan, rq=0.1| 
	var snd = BPF.ar(GrayNoise.ar, freq, rq, 3);
	var env = EnvGen.ar(Env.perc, timeScale: sustain, doneAction: 2);
	OffsetOut.ar(out, 
		Pan2.ar(snd * env, pan, amp)
	);
}, \ir ! 6).store

/*	
	// tests for the synthdefs:
Synth(\gab1st);
Synth(\gabWide);
Synth(\percSin);
Synth(\percSinRev);

Synth(\percSin, [\amp, 0.2, \sustain, 0.1]);
Synth(\percNoise, [\amp, 0.2, \sustain, 0.1]);

Synth(\gab1st, [\out, 0, \amp, 0.2, \freq, 2000, \sustain, 0.05, \pan, 0.5] );
*/
);





	// figure 6.24 - global setup and a player Tdef for the cloud.
(
q = q ? (); 

	// some globals
q.paramRNames = [\freqRange, \durRange, \densRange, \ampRange, \panRange]; 
q.paramNames = [\freq, \grDur, \dens, \amp, \pan]; 
q.syndefNames = [\gab1st, \gabWide, \percSin, \percSinRev, \percNoise];

	// specs for some parameters
Spec.add(\xfadeTime, [0.001, 1000, \exp]);
Spec.add(\ring, [0.03, 30, \exp]);
Spec.add(\grDur, [0.0001, 1, \exp]);
Spec.add(\dens, [1, 1000, \exp]);

	// make an empty tdef that plays it, 
	// and put the cloud parameter ranges in the tdef's environment
Tdef(\cloud0)
	.set(
	\synName, \gab1st,
	\vol, 0.25,
	\current, (
		freqRange: [200, 2000],
		ampRange: [0.1, 1],
		durRange: [0.001, 0.01], 
		densRange: [1, 1000],
		panRange: [-1.0, 1.0] 
	)
); 
		
		// make the tdef that plays the cloud of sound particles here, 
		// based on parameter range settings.
Tdef(\cloud0, { |e| 

	loop {
		s.sendBundle(s.latency, [
			"/s_new", e.synName ? \gab1st, 
			-1, 0, 0,
			\freq, 	exprand(e.current.freqRange[0], e.current.freqRange[1]),
			\amp,	exprand(e.current.ampRange[0], e.current.ampRange[1]) * e.vol,
			\sustain,	exprand(e.current.durRange[0], e.current.durRange[1]),
			\pan, 	rrand(e.current.panRange[0], e.current.panRange[1])
		]);
		exprand(e.current.densRange[0].reciprocal, e.current.densRange[1].reciprocal).wait; 
	}
}).quant_(0);	
);




/*
	// figure  6.25	-  tests for the cloud
	
Tdef(\cloud0).play;

	// try changing various things from outside the loop. 
	// change its playing settings 

Tdef(\cloud0).envir.current.put('densRange', [ 50, 200 ]); // dense, async
Tdef(\cloud0).envir.current.put('densRange', [ 1, 10 ]);  // sparse, async
Tdef(\cloud0).envir.current.put('densRange', [ 30, 30 ]); // synchronous

	// for faster access, call the tdef's envir d
d = Tdef(\cloud0).envir;
d.current.put('freqRange', [ 800, 1200 ]);
d.current.put('durRange', [ 0.02, 0.02 ]);

d.current.put('ampRange', [ 0.1, 0.1 ]);

d.current.put('panRange', [ 1.0, 1.0 ]);
d.current.put('panRange', [ -1.0, 1.0 ]);

d.current.put('densRange', [ 30, 60 ]);
d.synName = \percSin;
d.synName = \gab1st;
d.synName = \gabWide;
d.synName = \percSinRev;
d.synName = \percNoise;
d.synName = \percSinRev;
d.synName = \gab1st;
d.current.put('durRange', [ 0.001, 0.08 ]);


*/




	// figure 6.26 - making random settings, and 8 random presets to switch between
(
	// make the Tdef's envir a global variable for easier experimenting
d = Tdef(\cloud0).envir;
	// a pseudo-method to make random settings, kept in the Tdef's environment
		// randomize could also do limited variation on existing setting. 
d.randSet = { |d|
	var randSet = ();	
	q.paramRNames.do { |pName, i| 
		randSet.put(pName, 
			q.paramNames[i].asSpec.map([1.0.rand, 1.0.rand].sort)
		);
	};
	randSet;
};

/* 	test randSet: 
d.current = d.randSet;
*/

// make 8 sets of parameter range settings: 
d.setNames = (1..8).collect { |i| ("set" ++ i).asSymbol };
d.setNames.do { |key| d[key] = d.randSet; }

/*	test switching to the random presets
d.current = d.set1.copy;	// copy to avoid writing into a stored setting when it is current.
d.current = d.set3.copy;	
d.current = d.set8.copy;
*/
);




	// ex. 6.27 - crossfading between different settings with a taskproxy
	
(
	// and some parameters for controlling the fade
d.stopAfterFade = false;
d.xfadeTime = 5; 

d.morphtask = TaskProxy({
	var startSet = d[\current], endSet = d[\target];
	var stepsPerSec = 20;
	var numSteps = d.xfadeTime * stepsPerSec;
	var blendVal, morphSettings;
	
	if (d.target.notNil) { 
		(numSteps).do { |i| 
		//	["numSteps", i].postln;
			blendVal = (i + 1) / numSteps; 
			morphSettings = endSet.collect({ |val, key| 
				(startSet[key] ? val).blend(val, blendVal) 
			});
			d.current_(morphSettings);
			(1/stepsPerSec).wait;
		};
		d.current_(d.target.copy);
		"morph done.".postln;
		if (d.stopAfterFade) { Tdef(\cloud0).stop; };
	};
}).quant_(0);		// no quantization so the task starts immediately

/* test morphing
(
Tdef(\cloud0).play;
d.target = d.set6.copy;
d.morphtask.play;
)
Tdef(\cloud0).stop;

	// playing a a finite cloud with tendency mask: 
(
Tdef(\cloud0).play;		// begin playing
d.stopAfterFade = true; 	// end cloud when crossfade ends
d.xfadeTime = 10; 			// set fade time
d.target = d.set8.copy;		// and target
d.morphtask.play;			// and start crossfade.
)
*/

	// put fading into its own method, with optional stop.
d.fadeTo = { |d, start, end, time, autoStop| 
	d.current = d[start] ? d.current;
	d.target = d[end]; 
	d.xfadeTime = time ? d.xfadeTime;
	if (autoStop.notNil) { d.stopAfterFade = autoStop };
	d.morphtask.stop.play;
};

/* 	// tests fadeTo:
Tdef(\cloud0).play;
d.fadeTo(\current, \set2, 20);
d.fadeTo(\current, \set6, 10);
d.fadeTo(\current, \set5, 3, true);

Tdef(\cloud0).play;
d.fadeTo(\current, \set1, 3, false);
*/ 
);



	// figure 6.28 is an image, the CloudGenMini GUI //




	// figure 6.29 - a lightweight graphical user interface for CloudGenMini 
(
q.makeCloudGui = { |q, tdef, posPoint| 
	var w, ezRangers, fdBox; 
	var setMinis, skipjack; 
	
	posPoint = posPoint ? 0@0;	// where to put the gui window
	
	w = GUI.window.new("CloudGen_mini_sc3", 
		Rect.fromPoints(posPoint, (posPoint + (400@300)).postln)).front;
	w.view.decorator_(FlowLayout(w.bounds.copy.moveTo(0, 0)));
		
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
	TdefEditor(tdef, height: 20, w: w); 
	
	GUI.button.new(w, 80@20).states_([[\randomize]])
		.action_({ 
			tdef.envir.target_(d.randSet);
			tdef.envir.morphtask.stop.play;
		});
	
	GUI.staticText.new(w, 60@20).string_("xfadeTime"); 
	fdBox = GUI.numberBox.new(w, 40@20).action_{ |nbx| tdef.envir.xfadeTime = nbx.value }; 

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
		var zone = GUI.compositeView.new(w, Rect(0,0,45, 84));
		zone.decorator = FlowLayout(zone.bounds, 0@0, 5@0); 
		zone.background_(Color.white); 
		
		GUI.button.new(zone, Rect(0,0,45,20)).states_([[setname]])
			.action_({ 
				// just switch: // tdef.envir.current.putAll(d[setname] ? ()) 
				tdef.envir.target = tdef.envir[setname];
				tdef.envir.morphtask.stop.play;
			});
		
		GUI.button.new(zone, Rect(0,0,45,20))
			.states_([["save" ++ (i + 1)]])
			.action_({ 
				d[setname] = tdef.envir.current.copy; 
				setMinis.value;
			});
		
		minisliders = q.paramRNames.collect { |paramRname| 
			GUI.rangeSlider.new(zone, 45@8).enabled_(false);
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
	EZSlider(w, 250@20, "vol", \amp, { |sl|tdef.set(\vol, sl.value) }, 
		0.25, false, 20, 36);

	GUI.staticText.new(w, 50@20).string_("synthdef:"); 
	GUI.popUpMenu.new(w, Rect(0,0,80,20))
		.items_([\gab1st, \gabWide, \percSin, \percSinRev, \percNoise])
		.action_({ |pop| tdef.envir.synName = pop.items[pop.value] });
	
	GUI.button.new(w, 80@20).states_([[\continuous], [\fadeStops]])
		.value_(tdef.envir.stopAfterFade.binaryValue)
		.action_({ |btn|
			tdef.set(\stopAfterFade, btn.value == 1)
		});

	GUI.button.new(w, 80@20).states_([[\skipWatching], [\skipWaiting]])
		.action_({ |btn|
			[ { skipjack.play }, { skipjack.stop }][btn.value].value
		});

};
q.makeCloudGui(Tdef(\cloud0))
);


