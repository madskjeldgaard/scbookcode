Figure 18.11
VSO_Vib {

	var <pitch, <depth, <rate, <control;

	*new {arg start = 0.0, dur = nil, freq = 1, vibDepth = 0.21, vibRate = 1, 
		addAction = 0, target = 1, server;
		^super.new.initVSO_Vib(start, dur, freq, vibDepth, vibRate, addAction, 
			target, server);
	}
	
	initVSO_Vib {arg start, dur, freq, vibDepth, vibRate, add = 0, tgt = 1, server;
		server = server ?? {Server.default};
		pitch = freq;
		depth = vibDepth;
		rate = vibRate;
		control = CtkControl.lfo(SinOsc, rate, this.getLowerValue,
			this.getUpperValue, 0, start, dur, add, tgt, server: server);
	
	}
	
	getLowerValue {
		^(pitch - ((pitch / (pitch.log2)) * (depth * (1/3))));
	}

	getUpperValue {
		^(pitch + ((pitch / (pitch.log2)) * (depth * (2/3))));
	}
}


