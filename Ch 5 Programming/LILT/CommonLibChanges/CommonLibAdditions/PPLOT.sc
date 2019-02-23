/*
pplot is a little utility whose purpose is to create plots of synth output in small windows mainly to serve as picture snapshots for inclusion in documentation, tutorials etc. 

{ SinOsc.ar(40, 0, 0.1) }.pplot(0.1);
{ SinOsc.ar(40, 0, 0.1) }.plot100(0.1);
{ SinOsc.ar(40, 0, 0.1) }.plot150(0.1);
// { SinOsc.ar(40, 0, 0.1) }.plot180b(0.1);
{ LFSaw.ar(400, 0.0, 0.1) }.plot180(0.1);
{ LFPulse.ar(40, 0.0, 0.5, 0.1) }.plot180(0.1);
{ Pulse.ar(40, 0.5, 0.1) }.plot180(0.1);
{ Pulse.ar(10, 0.5, 0.1) }.plot180(1.0);
{ Pulse.ar(100, 0.5, 0.1) }.plot500(0.1);
{ Pulse.ar(500, 0.5, 0.1) }.plot500(0.1);
{ LFSaw.ar(400, 0.0, 0.1) }.plot500(0.1);
{ WhiteNoise.ar(0.1) }.plot180(0.1);
{ PinkNoise.ar(0.1) }.plot180(0.1);
{ BrownNoise.ar(0.1) }.plot180(0.1);
{ GrayNoise.ar(0.1) }.plot180(0.1);
{ WhiteNoise.ar(0.1) }.plot500(0.01);
{ GrayNoise.ar(0.1) }.plot500(0.01);
{ GrayNoise.ar(0.1) }.plot500(0.005);
{ LFNoise0.ar(100, 0.1) }.plot180(0.1);
{ LFNoise1.ar(100, 0.1) }.plot180(0.1);
{ LFNoise2.ar(100, 0.1) }.plot180(0.1);
{ LFClipNoise.ar(400, 0.1) }.plot180(0.1);
{ LFDNoise3.ar(100, 0.1) }.plot180(0.1);
{ Saw.ar(300, 0.1) }.plot400(0.1);
{ SinOsc.ar(40, 0, 0.1) }.plot200(0.1);
{ SinOsc.ar(40, 0, 0.1) }.plot300(0.1);
{ SinOsc.ar(40, 0, 0.1) }.plot400(0.1);
{ SinOsc.ar([40,50], 0, 0.1) }.plot500(0.1);

({ var m;
	m = LFNoise0.ar(5, 20, 32);
	[m, 
	SinOsc.ar(m, 0, 0.1)
	] }.plot500(1)
)
*/

+ ArrayedCollection {
	
	pplot { arg name, bounds, discrete=false, numChannels = 1;
		var plotter, txt, chanArray, unlaced, val, minval, maxval, window, thumbsize, zoom, width, 
			layout, write=false;
		bounds = bounds ?  Rect(200 , 140, 300, 200);
		
		width = bounds.width-8;
		zoom = (width / (this.size / numChannels));
		
		if(discrete) {
			thumbsize = max(1.0, zoom);
		}{
			thumbsize = 1;
		};
		
		name = name ? "plot";
		unlaced = this.unlace(numChannels);
		chanArray = Array.newClear(numChannels);
		unlaced.do({ |chan, j|
			val = Array.newClear(width);
			minval = chan.minItem;
			maxval = chan.maxItem;
			minval = minval - ((maxval - minval) / 10);
			maxval = maxval + ((maxval - minval) / 10);

			width.do { arg i;
				var x;
				x = chan.blendAt(i / zoom);
				val[i] = x.linlin(minval, maxval, 0.0, 1.0);
			};
			chanArray[j] = val;
		});
		window = SCWindow(name, bounds);
		window.view.background = Color.gray(0.8);		// Color.white;
		txt = SCStaticText(window, Rect(8, 0, width, 18))
				.string_("index: 0, value: " ++ this[0].asString);
		layout = SCVLayoutView(window, Rect(10, txt.bounds.height + 5, width - 10, 
			bounds.height - 30 - txt.bounds.height)).resize_(5);
//		layout.spacing = 5;
/*
{ SinOsc.ar([40,50], 0, 0.1) }.plot500(0.1);
*/
		numChannels.do({ |i|
			plotter = SCMultiSliderView(layout, Rect(0, 0, 
					layout.bounds.width - 50,layout.bounds.height))
				.readOnly_(true)
				.drawLines_(discrete.not)
				.drawRects_(discrete)
				.thumbSize_(thumbsize) 
				.valueThumbSize_(1)
				.colors_(Color.black, Color.blue(1.0,1.0))
				.action_({|v| 
					var curval;
					curval = v.currentvalue.linlin(0.0, 1.0, minval, maxval);
					
					txt.string_("index: " ++ (v.index / zoom).roundUp(0.01).asString ++ 
					", value: " ++ curval);
					if(write) { this[(v.index / zoom).asInteger] = curval };
				})
				.keyDownAction_({ |v, char|
					if(char === $l) { write = write.not; v.readOnly = write.not;  };
				})
				.value_(chanArray[i])
				.resize_(5)
				.elasticMode_(1)
				.background_(Color.gray(0.8));	// (Color.white);

		});
		
		^window.front;
		
	}
}

/*
+ Signal {
	pplot { arg name, bounds;
		//this.asciiPlot;
		super.pplot(name, bounds);
		
	}
}
*/

+ Wavetable {
	pplot { arg name, bounds;
		^this.asSignal.pplot;
	}
}

+ Buffer {
	pplot { arg name, bounds;
		this.loadToFloatArray(action: { |array, buf| {array.pplot(name, bounds, 
			numChannels: buf.numChannels) }.defer;});
	}
}

+ Function {
 	plot100 { | duration = 1 |
 		this.pplot(duration, bounds: Rect(200,200,100, 100));
 	}
 	plot150 { | duration = 1 |
 		this.pplot(duration, bounds: Rect(200,200,150, 100));
 	}
 	plot180 { | duration = 1 |
 		this.pplot(duration, bounds: Rect(200,200,180, 100));
 	}
 	plot200 { | duration = 1 |
 		this.pplot(duration, bounds: Rect(200,200,200, 100));
 	}
 	plot300 { | duration = 1 |
 		this.pplot(duration, bounds: Rect(200,200,300, 100));
 	}
 	plot400 { | duration = 1 |
 		this.pplot(duration, bounds: Rect(200,200,400, 100));
 	}
 	plot500 { | duration = 1 |
 		this.pplot(duration, bounds: Rect(200,200,500, 200));
 	}

	pplot { arg duration  = 1, server, bounds;
		var buffer, def, synth, name, value, numChannels;
		server = server ? Server.default;
		server.isLocal.not.if({"Function-pplot only works with a localhost server".warn; ^nil });
		server.serverRunning.not.if({"Server not running!".warn; ^nil });
		value = this.value;
		if(value.size == 0, { numChannels = 1 }, { numChannels =  value.size });
		buffer = Buffer.new(server, duration * server.sampleRate, numChannels);
		// no need to check for rate as RecordBuf is ar only
		name = this.hash.asString;
		def = SynthDef(name, { 
			RecordBuf.ar(this.value,  buffer.bufnum, loop:0);
			Line.ar(dur: duration, doneAction: 2);
		});
		Routine.run({
			var c;
			c = Condition.new;
			server.sendMsgSync(c, *buffer.allocMsg);
			server.sendMsgSync(c, "/d_recv", def.asBytes);
			synth = Synth.basicNew(name, server);
			OSCpathResponder(server.addr, ['/n_end', synth.nodeID], { 
				buffer.loadToFloatArray(action: { |array, buf| 
					{array.pplot(bounds: bounds, numChannels: buf.numChannels) }.defer;
					buffer.free;
				});
			}).add.removeWhenDone;
			server.listSendMsg(synth.newMsg);
		});
	}

}


+ SoundFile{
	pplot{ arg bounds;
		var win, view;
		bounds = bounds ?  Rect(200 , 140, 705, 410);
		win = SCWindow(this.path.split.last, bounds).front;
		view = SCSoundFileView(win, win.bounds.width@win.bounds.height).resize_(5);
		view.soundfile_(this);
		view.readWithTask;
		view.elasticMode_(1);
	}
}

