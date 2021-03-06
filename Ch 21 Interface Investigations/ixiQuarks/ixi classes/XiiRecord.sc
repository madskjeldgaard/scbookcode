// adoptation of Newton Armstrong's Record class

XiiRecord {
	var <server, <inbus, <numChannels, <headerFormat, <sampleFormat;
	var <isRecording=false, <bufnum, synth;
	
	*new { arg server, inbus=0, numChannels=2, headerFormat='aiff', sampleFormat='int16';		^super.new.init(server, inbus, numChannels, headerFormat, sampleFormat)
	}
	
	*initClass {
		for(1, 8, { arg i;
			SynthDef.writeOnce("xii-diskout-" ++ i.asString, { arg i_in, i_bufNum=0, amp=1;
				DiskOut.ar(i_bufNum, amp * InFeedback.ar(i_in, i));
			});
		});
	}
	
	init { arg argServer, argInbus, argChans, argHeaderFormat, argSampleFormat;
	
		server = argServer ? Server.local;
		inbus = argInbus;
		numChannels = argChans;
		headerFormat = argHeaderFormat.asString.collect({ arg char; char.toLower });
		sampleFormat = argSampleFormat;
		CmdPeriod.add(this);
	}
	
	start { arg path, argBufnum; var ext;
		
		if( isRecording, { ^nil });
		
		ext = headerFormat;
		
		if( ext == "none", { ext = "" }, {
			if( ext == "sun", { ext = "au" });
			if( ext == "ircam", { ext = "sf" });
			ext = "." ++ ext;
		});
	
		path = path ? (Date.localtime.stamp ++ ext);
		bufnum = argBufnum ? server.bufferAllocator.alloc(1);
	
		server.sendMsg("/b_alloc", bufnum, 32768, numChannels,
			["/b_write", bufnum, path, headerFormat, sampleFormat, 0, 0, 1]
		);
//		synth = Synth.new("xii-diskout-" ++ numChannels, 
//					[\i_in, inbus, \i_bufNum, bufnum], 
//					target: server,
//					addAction: \addToTail // added by thor
//					);
		synth = Synth.tail(RootNode(Server.default), "xii-diskout-" ++ numChannels, // RootNode by thor
					[\i_in, inbus, \i_bufNum, bufnum]
					//target: server,
					//addAction: \addToTail // added by thor
					);
	
		isRecording = true;
		this.changed;
		inform("RECORDING...");
	}
	
	stop {
		if( isRecording.not, { ^nil });
		try{ synth.free };
		server.sendMsg("/b_close", bufnum, ["/b_free", bufnum]);
		isRecording = false;
		this.changed;
		inform("RECORDING STOPPED.");
	}
	
	cmdPeriod {
		this.stop;
	}
	
	inbus_ { arg argInbus;
		inbus = argInbus;
		this.changed(thisMethod.name);
	}
		
	setAmp_ {arg amp;
		synth.set(\amp, amp);
	}
	
	numChannels_ { arg argNumChannels;
		numChannels = argNumChannels;
		this.changed(thisMethod.name);
	}
	
	headerFormat_ { arg argHeaderFormat;
		headerFormat = argHeaderFormat.asString.collect({ arg char; char.toLower });
		this.changed(thisMethod.name);
	}
	
	sampleFormat_ { arg argSampleFormat;
		sampleFormat = argSampleFormat;
		this.changed(thisMethod.name);
	}
}