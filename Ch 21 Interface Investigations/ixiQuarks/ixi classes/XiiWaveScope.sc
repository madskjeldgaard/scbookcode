
XiiWaveScope {

	var <>xiigui;
	var <>win, params;
	
	classvar ugenScopes;
	var <server, <numChannels,  <index;
	var <bufsize, buffer, synth;
	var n, c, d, sl, style=0, sizeToggle=0, zx, zy, ai=0;
	var onOffButt;
	
	
	*new { arg server, numChannels = 2, setting = nil;
		var win, name, point;
		if(Server.default == Server.local, {
			XiiAlert("The WaveScope works only on the internal server!");
		},{
			name = "wavescope";
			point = if(setting.isNil, {XiiWindowLocation.new(name)}, {setting[1]});
			win = SCWindow.new(name, Rect(point.x, point.y, 400, 328));
			win.view.decorator = FlowLayout(Rect(6, 0, 400, 328));
			win.front;
			^super.new.initWaveScope(server, numChannels, setting, win:win);
		});
	}

	initWaveScope { arg server, numChannels = 2, setting, win;
		if(server.inProcess.not, { " ixi wavescope works only with internal server".warn; ^nil });
		this.makeWindow(win, setting);
		this.index_(0);
		this.zoom_(10);
		this.allocBuffer(4096, server.bufferAllocator.alloc(1));
		this.numChannels = 2;
	}
	
	*tileBounds {
		var screenBounds = SCWindow.screenBounds;
		var x = 544 + (ugenScopes.size * 222);
		var right = x + 212;
		var y = floor(right / screenBounds.width) * 242 + 10;
		if(right > screenBounds.right) { x = floor(right % screenBounds.width / 222) * 222 };
		^Rect(x, y, 212, 212)
	}
	
	makeBounds { arg point, size=212; ^Rect(point.x, point.y, size, size) }
	
	makeWindow { arg wind, setting;
		var view, cmdPeriodFunc, name, point, style;
		
		view = wind.view;
		win = wind;
		
		xiigui = nil; // not using window server class here
		params = if(setting.isNil, {[0,1,0]}, {setting[2]});
		
		style = params[2];
//		if(view.isNil) {
//			"view is NIL".postln;
//			win = SCWindow(name, this.makeBounds(point));
//			view = win.view;
//			view.decorator = FlowLayout(win.view.bounds);
//			win.front;
//			//win.onClose = { this.free };
//		};
		
		n = SCScope(view, Rect(view.bounds.left+20, 5, view.bounds.width - 20, view.bounds.height - 40));
		n.background = Color.green(0.1);
		n.resize = 5;
		n.style = style;
		view.keyDownAction = { arg view, char, modifiers, unicode, keycode; 
			this.keyDown(char, modifiers, unicode, keycode) 
		};
			
		zx = n.xZoom.log2;
		zy = n.yZoom.log2;
							
		SCStaticText(view, Rect(20, 18, 27, 18))
			.string_("inbus :")
			.resize_(9)
			.font_(Font("Helvetica", 9));
			
		SCPopUpMenu(view, Rect(38, 15, 30, 18))
			.items_(XiiACDropDownChannels.getMonoChnList)
			.value_(params[0])
			.font_(Font("Helvetica", 9))
			.background_(Color.white)
			.resize_(9)		
			.canFocus_(false)
			.action_({ arg ch; 
				var i; 
				this.index = ch.value;
			});
			
		SCStaticText(view, Rect(100, 18, 33, 18))
			.string_("chNum :")
			.resize_(9)
			.font_(Font("Helvetica", 9));
			
		SCPopUpMenu(view, Rect(135, 15, 30, 18))
			.items_([ "1", "2" ,"3", "4", "5", "6", "7", "8", "9", "10", "11", 
					"12", "13", "14", "15", "16"])
			.value_(params[1])
			.font_(Font("Helvetica", 9))
			.background_(Color.white)
			.canFocus_(false)
			.resize_(9)
			.action_({ arg ch; 
				this.numChannels = (ch.value+1).asInteger;
			});
		SCStaticText(view, Rect(155, 18, 27, 18))
			.string_("style :")
			.resize_(9)
			.font_(Font("Helvetica", 9));
			
		SCPopUpMenu(view, Rect(155, 15, 30, 18))
			.items_([ "0", "1" ,"2"])
			.value_(style)
			.font_(Font("Helvetica", 9))
			.background_(Color.white)
			.canFocus_(false)
			.resize_(9)
			.action_({ arg ch; 
				n.style = ch.value.asInteger;
			});

		onOffButt = SCButton(view, Rect(195, 15, 30, 16))
			.states_([["On", Color.black, Color.clear], 
			["Off", Color.black, Color.green(alpha:0.2)]]) 
			.resize_(9)
			.action_({ arg view;
				if(view.value == 1, {
					this.run;
				},{
					this.stop;
				});
			})
			.font_(Font("Helvetica", 9))
			.canFocus_(false);
			
		// this is just a spacer for the flowlayout
		SCStaticText(view, Rect(300, 18, 20, 18))
			.string_("   ")
			.resize_(9)
			.font_(Font("Helvetica", 9));
			
		SCStaticText(view, Rect(300, 18, 100, 18))
			.string_("use arrows to zoom")
			.resize_(9)
			.font_(Font("Helvetica", 9));

		this.updateColors;
		
		cmdPeriodFunc = { onOffButt.valueAction_(0)};
		CmdPeriod.add(cmdPeriodFunc);

		win.onClose_({ 
			var t;
			CmdPeriod.remove(cmdPeriodFunc);
			XQ.globalWidgetList.do({arg widget, i; if(widget === this, {t = i})});
			try{XQ.globalWidgetList.removeAt(t)};
			this.free;		
 		});
	}
	
	keyDown { arg char, modifiers, unicode, keycode;
				if(char === $i) { this.toInputBus; ^this }; 
				if(char === $o) { this.toOutputBus;  ^this  }; 
				if(char === $ ) { this.run;  ^this  };
				if(char === $s) { this.style = (style + 1) % 2; ^this  };
				if(char === $S) { this.style = 2;  ^this  };
				if(char === $j) { this.index = index - 1; ^this  };
				if(char === $l) { this.index = index + 1 };
				
				if(char === $-) {  zx = zx + 0.25; this.xZoom = 2 ** zx; ^this  };
				if(char === $+) {  zx = zx - 0.25; this.xZoom = 2 ** zx; ^this  };				if(char === $*) {  zy = zy + 0.25; this.yZoom = 2 ** zy; ^this  };
				if(char === $_) {  zy = zy - 0.25; this.yZoom = 2 ** zy; ^this  };
				
				if(keycode === 124) {  zx = zx + 0.25; this.xZoom = 2 ** zx; ^this  };
				if(keycode === 123) {  zx = zx - 0.25; this.xZoom = 2 ** zx; ^this  };
				if(keycode === 125) {  zy = zy + 0.25; this.yZoom = 2 ** zy; ^this  };
				if(keycode === 126) {  zy = zy - 0.25; this.yZoom = 2 ** zy; ^this  };
				
				if(char === $A) {  this.adjustBufferSize; ^this  };
				if(char === $m) { this.toggleSize; ^this  };
				if(char === $.) { if(synth.isPlaying) { synth.free } };

	}
	
	setProperties { arg numChannels, index, bufsize=4096, zoom;
				if(index.notNil) { this.index = index };
				if(numChannels.notNil) { this.numChannels = numChannels };
				if(this.bufsize != bufsize) { this.allocBuffer(bufsize) };
				if(zoom.notNil) { this.zoom = zoom };
	}
	
	allocBuffer { arg argbufsize, argbufnum;
		bufsize = argbufsize ? bufsize;
		if(buffer.notNil) { buffer.free };
		buffer = Buffer.alloc(server, bufsize, numChannels, nil, argbufnum);
		n.bufnum = buffer.bufnum;
		if(synth.isPlaying) { synth.set(\bufnum, buffer.bufnum) };
	}
	
	run {
		if(synth.isPlaying.not) {
			synth = SynthDef("xiistethoscope", { arg in, switch, bufnum;
				ScopeOut.ar(In.ar(in, numChannels), bufnum);
			}).play(RootNode(server), [\bufnum, buffer.bufnum, \in, index],
				\addToTail
			);
			synth.isPlaying = true;
			NodeWatcher.register(synth);
		}
	}
	
	stop { // added by THOR
		if(synth.isPlaying) {
			synth.free;
			synth.isPlaying = false;
			NodeWatcher.unregister(synth);
		}
	}
	
	free {
		buffer.free;
		if(synth.isPlaying) {  synth.free };
		synth = nil;
	}
	
	quit {
		win.close;
		this.free;
	}
	
	numChannels_ { arg n;
		var isPlaying;
		if(n > 16) { "cannot display more than 16 channels at once".inform; n = 16 };
		if(n != numChannels and: { n > 0 }) {  
			isPlaying = synth.isPlaying;
			if(isPlaying) { synth.free; synth.isPlaying = false; synth = nil }; // immediate
			numChannels = n;
			this.allocBuffer;
			if(isPlaying) {  this.run };
			this.updateColors;
		};
	}
	
	index_ { arg val=0;
		if(synth.isPlaying) { synth.set(\in, val) };
	}
	
	size_ { arg val; if(win.notNil) { win.bounds = this.makeBounds(val) } }
	toggleSize {  if(sizeToggle == 0) 
					{ sizeToggle = 1; this.size_(500) }
					{ sizeToggle = 0; this.size_(212) } 
	}
	
	xZoom_ { arg val; n.xZoom = val; zx = val.log2 }
	yZoom_ { arg val; n.yZoom = val; zy = val.log2 }
	xZoom { ^2.0 ** zx }
	yZoom { ^2.0 ** zy }
	
	zoom_ { arg val; this.xZoom_(val ? 1) }

	style_ { arg val; n.style = val; style = val; }
	
	updateColors {
		n.waveColors = Array.fill(numChannels, { Color.green }); //rgb(255, 218, 000) 
	}
		
	toInputBus {
		this.index = 8; //server.options.numOutputBusChannels;
		this.numChannels = 8; //server.options.numInputBusChannels;
	}
	toOutputBus {
		this.index = 0;
		this.numChannels = 8; //server.options.numOutputBusChannels;
	}
	adjustBufferSize {
		this.allocBuffer(max(256,nextPowerOfTwo(asInteger(n.bounds.width * n.xZoom))))
	}
	
	// ugenScopes
	*ugenScopes {
		if(ugenScopes.isNil, { ugenScopes = Set.new; });
		^ugenScopes
	}
	
	getState { // for save settings
		var point;		
		point = Point(win.bounds.left, win.bounds.top);
		^[2, point, params]; // channel, point, params
	}
}
