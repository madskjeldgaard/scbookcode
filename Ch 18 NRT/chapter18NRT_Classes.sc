//Drop this file into ~/Library/Application Support/SuperCollider/Extensions and compile the 
// library (Menu:Lang:Compile Library, or Cmd-k) so that these classes are available for use
// in the examples in chapter 24, sections 3.2 and 3.3. The examples in these sections also
// require the Ctk library which you can install as a Quark, or download from
// http://sourceforge.net/projects/sc3-plugins/

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
VSO_ADR {

	var <control, <attackDur, <releaseDur, <totalDur;

	*new {arg start = 0.0, dur =  nil, peak = 0.707, decay = 0.01, attackDur = 0.125, 
		releaseDur = 0.125, addAction = 0, target = 1, server;
		^super.new.initVSO_ADR(start, dur, peak, decay, attackDur, releaseDur, addAction,
			target, server);
	}
	
	initVSO_ADR {arg start = 0.0, dur =  nil, peak = 0.707, decay = 0.01, aDur = 0.125, 
		rDur = 0.125, addAction = 0, target = 1, server;
		server = server ?? {Server.default};
		attackDur = aDur;
		releaseDur = rDur;
		totalDur = dur;
		control = CtkControl.env(Env.new([0, peak, decay, 0], 
			[attackDur, this.decayDur, releaseDur], \sine), 
			start, addAction, target, server: server, doneAction: 0);
	}
	
	decayDur {
		^(totalDur - (attackDur + releaseDur));
	}

}
VSO {

	classvar <sinoscdef;
	var <score, group, oscil, freqCntl, <ampCntl;
	
	*new {arg start = 0.0, dur = nil, freq = 622.254, ampPeakLevel = 0.707, ampDecayLevel = 0.01, 
		vibDepth = 0.21, vibRate = 3, addAction = 0, target = 1, server;
		^super.new.initVSO(start, dur, freq, ampPeakLevel, ampDecayLevel, 
			vibDepth, vibRate, addAction, target, server);
	}

	*initClass {
		sinoscdef.isNil.if({
			sinoscdef = CtkSynthDef.new(\NRT_sinosc, 
				{arg outbus = 0, freq = 622.254, phase = 0, amp = 1, offSet = 0;
					Out.ar(outbus, SinOsc.ar(freq, phase, amp, offSet));
				})
			});
	}

	initVSO {arg start = 0.0, dur = nil, freq = 622.254, ampPeakLevel = 0.707, 
		ampDecayLevel = 0.01, vibDepth = 0.21, vibRate = 3, addAction = 0, target = 1, 
			server;
		server = server ?? {Server.default};
		group = CtkGroup.new(start, dur, addAction: addAction, target: target, 
			server: server);
		freqCntl = VSO_Vib.new(start, dur, freq, vibDepth, vibRate, \head, group, server);
		ampCntl = VSO_ADR.new(start, dur, ampPeakLevel, ampDecayLevel, addAction: \head, 
			target: group, server: server);
		oscil = sinoscdef.new(start, dur, \tail, group, server)
			.freq_(freqCntl.control).amp_(ampCntl.control);
		score = CtkScore.new(group, oscil);
	}

}
NRT_TimeFrame {

	var >starttime, >duration;
	
	*new {arg starttime, duration;
		^super.newCopyArgs(starttime, duration);
	}

	starttime {
		^starttime.value;
	}

	duration {
		^duration.value;
	}

	endtime {
		^(this.starttime != nil).if({
			(this.duration != nil).if({
				//call the getter methods rather than accessing
				// the variables directly
				this.starttime + this.duration;
			}, {nil})
		}, {nil});
	}

}
