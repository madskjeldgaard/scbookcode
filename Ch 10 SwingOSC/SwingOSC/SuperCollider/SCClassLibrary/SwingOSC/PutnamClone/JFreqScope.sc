// converted to SwingOSC compatibility ; last mod : 25-apr-06 sciss
// this has to be in a file separate from JSCFreqScope,
// otherwise i can't compile (on mac) ...

// SCFreqScope and FreqScope
// by Lance Putnam
// lance@uwalumni.com

//	@version	0.59, 30-Jan-08
JFreqScope {
	
	var <scope, <window;

	*new { arg width=512, height=300, busNum=0, scopeColor, bgColor;
		//make scope
		var rect, scope, window, pad, font, freqLabel, freqLabelDist, dbLabel, dbLabelDist;
		var setFreqLabelVals, setDBLabelVals;
		var nyquistKHz;
		if(scopeColor.isNil, { scopeColor = Color.green });
		if(bgColor.isNil, { bgColor = Color.green(0.1) });
		
		rect = Rect(0, 0, width, height);
		pad = [30, 38, 14, 10]; // l,r,t,b
		font = JFont( JFont.defaultMonoFace, 9);
		freqLabel = Array.newClear(12);
		freqLabelDist = rect.width/(freqLabel.size-1);
		dbLabel = Array.newClear(17);
		dbLabelDist = rect.height/(dbLabel.size-1);
		
//		nyquistKHz = Server.default.sampleRate;
		nyquistKHz = JSCFreqScope.audioServer.sampleRate;
		if( (nyquistKHz == 0) || nyquistKHz.isNil, {
			nyquistKHz = 22.05 // best guess?
		},{
			nyquistKHz = nyquistKHz * 0.0005;
		}); 
		
		
		setFreqLabelVals = { arg mode, bufsize;
			var kfreq, factor, halfSize;
			
			factor = 1/(freqLabel.size-1);
			halfSize = bufsize * 0.5;
			
			freqLabel.size.do({ arg i;
				if(mode == 1, {
					kfreq = (halfSize.pow(i * factor) - 1)/(halfSize-1) * nyquistKHz;
				},{
					kfreq = i * factor * nyquistKHz;
				});
					
				if(kfreq > 1.0, {
					freqLabel[i].string_( kfreq.asString.keep(4) ++ "k" )
				},{
					freqLabel[i].string_( (kfreq*1000).asInteger.asString)
				});
			});
		};
		
		setDBLabelVals = { arg db;
			dbLabel.size.do({ arg i;
				dbLabel[i].string = (i * db/(dbLabel.size-1)).asInteger.neg.asString;
			});
		};

		window = JSCWindow("Freq Analyzer", rect.resizeBy(pad[0] + pad[1] + 24, pad[2] + pad[3] + 4), false);

		scope = JSCFreqScope(window, rect.moveBy(pad[0], pad[2]));
		scope.opaque = false;	// ... so the grid shines through (don't know why we need this)
		
		freqLabel.size.do({ arg i;
			freqLabel[i] = JSCStaticText(window, Rect(pad[0] - (freqLabelDist*0.5) + (i*freqLabelDist), pad[2] - 10, freqLabelDist, 10))
				.font_(font)
				.align_(0)
			;
			JSCStaticText(window, Rect(pad[0] + (i*freqLabelDist), pad[2], 1, rect.height))
				.string_("")
				.background_(scopeColor.alpha_(0.25))
			;
		});
		
		dbLabel.size.do({ arg i;
			dbLabel[i] = JSCStaticText(window, Rect(0, pad[2] + (i*dbLabelDist) - 4, pad[0] - 2, 10))
				.font_(font)
				.align_(1)
			;
			JSCStaticText(window, Rect(pad[0], dbLabel[i].bounds.top + 4, rect.width, 1))
				.string_("")
				.background_(scopeColor.alpha_(0.25))
			;		
		});
		
//		scope = JSCFreqScope(window, rect.moveBy(pad[0], pad[2]));
//		scope.opaque = false;
		scope.xZoom_((scope.bufSize*0.25) / width);
		
		setFreqLabelVals.value(scope.freqMode, 2048);
		setDBLabelVals.value(scope.dbRange);

		rect = rect.resizeBy( 4, 0 );

		JSCButton(window, Rect(pad[0] + rect.width, pad[2], pad[1], 16))
			.states_([["Power", Color.white, Color.green(0.5)], ["Power", Color.white, Color.red(0.5)]])
			.action_({ arg view;
				if(view.value == 0, {
					scope.active_(true);
				},{
					scope.active_(false);
				});
			})
			.font_(font)
			.canFocus_(false)
		;
		
		JSCStaticText(window, Rect(pad[0] + rect.width, pad[2]+20, pad[1] + 20, 10))
			.string_("BusIn")
			.font_(font)
		;

		JSCNumberBox(window, Rect(pad[0] + rect.width, pad[2]+30, pad[1], 18))
			.action_({ arg view;
				view.value_(view.value.asInteger.clip(0, Server.default.options.numAudioBusChannels));
				scope.inBus_(view.value);
			})
			.value_(busNum)
			.font_(font)
		;

		JSCStaticText(window, Rect(pad[0] + rect.width, pad[2]+54, pad[1] + 20, 10))
			.string_("FrqScl")
			.font_(font)
		;
		JSCPopUpMenu(window, Rect(pad[0] + rect.width, pad[2]+68, pad[1] + 20, 22))
			.items_(["lin", "log"])
			.action_({ arg view;
				scope.freqMode_(view.value);
				setFreqLabelVals.value(scope.freqMode, 2048);
			})
			.canFocus_(false)
			.font_(font)
		;
		
		JSCStaticText(window, Rect(pad[0] + rect.width, pad[2]+98, pad[1] + 20, 10))
			.string_("dbCut")
			.font_(font)
		;
		JSCPopUpMenu(window, Rect(pad[0] + rect.width, pad[2]+112, pad[1] + 20, 22))
			.items_(Array.series(12, 12, 12).collect({ arg item; item.asString }))
			.action_({ arg view;
				scope.dbRange_((view.value + 1) * 12);
				setDBLabelVals.value(scope.dbRange);
			})
			.canFocus_(false)
			.font_(font)
			.value_(7)
		; 

		scope
			.background_(bgColor)
			.style_(1)
			.waveColors_([scopeColor.alpha_(1)])
			.inBus_(busNum)
			.active_(true)
			.canFocus_(false)
		;
		
		window.onClose_({ scope.kill }).front;
		^this.newCopyArgs(scope, window)
	}

}
