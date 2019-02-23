XiiBandpass {	
	var <>xiigui;
	
	*new { arg server, channels, setting = nil;
		^super.new.initXiiBandpass(server, channels, setting);
		}
		
	initXiiBandpass {arg server, channels, setting;

		var freqSpec, rqSpec, params, s; 
		
		s = server ? Server.local;
		
		// mono
		SynthDef(\xiiBPF1x1, {arg inbus=0,
							outbus=0, 
							freq=200,
							rq=0.4, 
							fxlevel = 0.7, 
							level=1.0;
							
		Ê Êvar fx, sig; 
		Ê Êsig = InFeedback.ar(inbus,1); 
		Ê Êfx = BPF.ar(sig, freq, rq); 
		Ê ÊOut.ar(outbus, (fx * fxlevel) + (sig * level)) 
		}).add; 	

		// stereo
		SynthDef(\xiiBPF2x2, {arg inbus=0,
							outbus=0, 
							freq=200,
							rq=0.4, 
							fxlevel = 0.7, 
							level=1.0;
							
		Ê Êvar fx, sig; 
		Ê Êsig = InFeedback.ar(inbus, 2); 
		Ê Êfx = BPF.ar(sig, freq, rq); 
		Ê ÊOut.ar(outbus, (fx * fxlevel) + (sig * level)) 
		}).add; 	

		freqSpec = ControlSpec.new(20, 20000, \exponential, 1, 2000); 
		rqSpec = ControlSpec.new(0.0001, 1, \exponential, 0.0001, 0.5); 
		
		
		params = [ 
		Ê Ê["Freq", "RQ", "Fx level", "Dry Level"], 
		Ê Ê[ \freq, \rq, \fxlevel, \level], 
		Ê Ê[freqSpec, rqSpec, \amp, \amp], 
		Ê Êif(setting.notNil, {setting[5]}, {[2000, 0.5, 1, 0]})
		]; 
		
		xiigui = if(channels == 2, { 	// stereo
			XiiEffectGUI.new("Bandpass Filter 2x2", \xiiBPF2x2, params, channels, this, setting); /// 
			}, {				// mono
			XiiEffectGUI.new("Bandpass Filter 1x1", \xiiBPF1x1, params, channels, this, setting); /// 
		});
	}
}

XiiLowpass {	
	var <>xiigui;
	
	*new { arg server, channels, setting = nil;
		^super.new.initXiiLowpass(server, channels, setting);
		}
		
	initXiiLowpass {arg server, channels, setting;
		var freqSpec, params, s; 
		s = server ? Server.local;
		
		// mono
		SynthDef(\xiiLPF1x1, {arg inbus=0,
							outbus=0, 
							freq=200, 
							fxlevel = 0.7, 
							level=1.0;
							
		Ê Êvar fx, sig; 
		Ê Êsig = InFeedback.ar(inbus,1); 
		Ê Êfx = LPF.ar(sig, freq); 
		Ê ÊOut.ar(outbus, (fx * fxlevel) + (sig * level)) 
		}).add; 	

		// stereo
		SynthDef(\xiiLPF2x2, {arg inbus=0,
							outbus=0, 
							freq=200,
							fxlevel = 0.7, 
							level=1.0;
							
		Ê Êvar fx, sig; 
		Ê Êsig = InFeedback.ar(inbus, 2); 
		Ê Êfx = LPF.ar(sig, freq); 
		Ê ÊOut.ar(outbus, (fx * fxlevel) + (sig * level)) 
		}).add; 	

		freqSpec = ControlSpec.new(20, 20000, \exponential, 1, 2000); 
		
		params = [ 
		Ê Ê["Freq", "Fx level", "Dry Level"], 
		Ê Ê[ \freq, \fxlevel, \level], 
		Ê Ê[freqSpec, \amp, \amp], 
		Ê Êif(setting.notNil, {setting[5]}, {[2000, 1, 0]})
		]; 
		
		xiigui = if(channels == 2, { 	// stereo
			XiiEffectGUI.new("Lowpass Filter 2x2", \xiiLPF2x2, params, channels, this, setting); /// 
			}, {				// mono
			XiiEffectGUI.new("Lowpass Filter 1x1", \xiiLPF1x1, params, channels, this, setting); /// 
		})
	}
}

XiiHighpass {	
	var <>xiigui;
	
	*new { arg server, channels, setting = nil;
		^super.new.initXiiHighpass(server, channels, setting);
		}
		
	initXiiHighpass {arg server, channels, setting;
		var freqSpec, params, s; 
		s = server ? Server.local;
		
		// mono
		SynthDef(\xiiHPF1x1, {arg inbus=0,
							outbus=0, 
							freq=200,
							fxlevel = 0.7, 
							level=1.0;
							
		Ê Êvar fx, sig; 
		Ê Êsig = InFeedback.ar(inbus,1); 
		Ê Êfx = HPF.ar(sig, freq); 
		Ê ÊOut.ar(outbus, (fx * fxlevel) + (sig * level)) 
		}).add; 	

		// stereo
		SynthDef(\xiiHPF2x2, {arg inbus=0,
							outbus=0, 
							freq=200,
							fxlevel = 0.7, 
							level=1.0;
							
		Ê Êvar fx, sig; 
		Ê Êsig = InFeedback.ar(inbus, 2); 
		Ê Êfx = HPF.ar(sig, freq); 
		Ê ÊOut.ar(outbus, (fx * fxlevel) + (sig * level)) 
		}).add; 	

		freqSpec = ControlSpec.new(20, 20000, \exponential, 1, 2000); 
		
		params = [ 
		Ê Ê["Freq", "Fx level", "Dry Level"], 
		Ê Ê[ \freq, \fxlevel, \level], 
		Ê Ê[freqSpec, \amp, \amp], 
		Ê Êif(setting.notNil, {setting[5]}, {[2000, 1, 0]})
		]; 
		
		xiigui = if(channels == 2, { 	// stereo
			XiiEffectGUI.new("Highpass Filter 2x2", \xiiHPF2x2, params, channels, this, setting); /// 
			}, {				// mono
			XiiEffectGUI.new("Highpass Filter 1x1", \xiiHPF1x1, params, channels, this, setting); /// 
		})
	}
}


XiiRLowpass {	
	var <>xiigui;
	
	*new { arg server, channels, setting = nil;
		^super.new.initXiiRLowpass(server, channels, setting);
		}
		
	initXiiRLowpass {arg server, channels, setting;

		var freqSpec, rqSpec, params, s; 
		
		s = server ? Server.local;
		
		// mono
		SynthDef(\xiiRLPF1x1, {arg inbus=0,
							outbus=0, 
							freq=200,
							rq=0.4, 
							fxlevel = 0.7, 
							level=1.0;
							
		Ê Êvar fx, sig; 
		Ê Êsig = InFeedback.ar(inbus,1); 
		Ê Êfx = RLPF.ar(sig, freq, rq); 
		Ê ÊOut.ar(outbus, (fx * fxlevel) + (sig * level)) 
		}).add; 	

		// stereo
		SynthDef(\xiiRLPF2x2, {arg inbus=0,
							outbus=0, 
							freq=200,
							rq=0.4, 
							fxlevel = 0.7, 
							level=1.0;
							
		Ê Êvar fx, sig; 
		Ê Êsig = InFeedback.ar(inbus, 2); 
		Ê Êfx = RLPF.ar(sig, freq, rq); 
		Ê ÊOut.ar(outbus, (fx * fxlevel) + (sig * level)) 
		}).add; 	

		freqSpec = ControlSpec.new(20, 20000, \exponential, 1, 2000); 
		rqSpec = ControlSpec.new(0.01, 1, \exponential, 0.01, 0.5); 
		
		params = [ 
		Ê Ê["Freq", "RQ", "Fx level", "Dry Level"], 
		Ê Ê[ \freq, \rq, \fxlevel, \level], 
		Ê Ê[freqSpec, rqSpec, \amp, \amp], 
		Ê Êif(setting.notNil, {setting[5]}, {[2000, 0.5, 1, 0]})
		]; 
		
		xiigui = if(channels == 2, { 	// stereo
			XiiEffectGUI.new("Resonant Lowpass Filter 2x2", \xiiRLPF2x2, params, channels, this, setting);
			}, {				// mono
			XiiEffectGUI.new("Resonant Lowpass Filter 1x1", \xiiRLPF1x1, params, channels, this, setting); 
		})
	}
}


XiiRHighpass {	
	var <>xiigui;
	
	*new { arg server, channels, setting = nil;
		^super.new.initXiiRHighpass(server, channels, setting);
		}
		
	initXiiRHighpass {arg server, channels, setting;

		var freqSpec, rqSpec, params, s; 
		
		s = server ? Server.local;
		
		// mono
		SynthDef(\xiiRHPF1x1, {arg inbus=0,
							outbus=0, 
							freq=200,
							rq=0.4, 
							fxlevel = 0.7, 
							level=1.0;
							
		Ê Êvar fx, sig; 
		Ê Êsig = InFeedback.ar(inbus,1); 
		Ê Êfx = RHPF.ar(sig, freq, rq); 
		Ê ÊOut.ar(outbus, (fx * fxlevel) + (sig * level)) 
		}).add; 	

		// stereo
		SynthDef(\xiiRHPF2x2, {arg inbus=0,
							outbus=0, 
							freq=200,
							rq=0.4, 
							fxlevel = 0.7, 
							level=1.0;
							
		Ê Êvar fx, sig; 
		Ê Êsig = InFeedback.ar(inbus, 2); 
		Ê Êfx = RHPF.ar(sig, freq, rq); 
		Ê ÊOut.ar(outbus, (fx * fxlevel) + (sig * level)) 
		}).add; 	

		freqSpec = ControlSpec.new(20, 20000, \exponential, 1, 2000); 
		rqSpec = ControlSpec.new(0.01, 1, \exponential, 0.01, 0.5); 
		
		params = [ 
		Ê Ê["Freq", "RQ", "Fx level", "Dry Level"], 
		Ê Ê[ \freq, \rq, \fxlevel, \level], 
		Ê Ê[freqSpec, rqSpec, \amp, \amp], 
		Ê Êif(setting.notNil, {setting[5]}, {[2000, 0.5, 1, 0]})
		]; 
		
		xiigui = if(channels == 2, { 	// stereo
			XiiEffectGUI.new("Resonant Highpass Filter 2x2", \xiiRHPF2x2, params, channels, this, setting);
			}, {				// mono
			XiiEffectGUI.new("Resonant Highpass Filter 1x1", \xiiRHPF1x1, params, channels, this, setting);
		})
	}
}


XiiResonant {	
	var <>xiigui;
	
	*new { arg server, channels, setting = nil;
		^super.new.initXiiResonant(server, channels, setting);
		}
		
	initXiiResonant {arg server, channels, setting;

		var freqSpec, rqSpec, params, s; 
		
		s = server ? Server.local;
		
		// mono
		SynthDef(\xiiResonant1x1, {arg inbus=0,
							outbus=0, 
							freq=200,
							rq=0.4, 
							fxlevel = 0.7, 
							level=1.0;
							
		Ê Êvar fx, sig; 
		Ê Êsig = InFeedback.ar(inbus,1); 
		Ê Êfx = Resonz.ar(sig, freq, rq); 
		Ê ÊOut.ar(outbus, (fx * fxlevel) + (sig * level)) 
		}).add; 	

		// stereo
		SynthDef(\xiiResonant2x2, {arg inbus=0,
							outbus=0, 
							freq=200,
							rq=0.4, 
							fxlevel = 0.7, 
							level=1.0;
							
		Ê Êvar fx, sig; 
		Ê Êsig = InFeedback.ar(inbus, 2); 
		Ê Êfx = Resonz.ar(sig, freq, rq); 
		Ê ÊOut.ar(outbus, (fx * fxlevel) + (sig * level)) 
		}).add; 	

		freqSpec = ControlSpec.new(20, 20000, \exponential, 1, 2000); 
		rqSpec = ControlSpec.new(0.0001, 1, \exponential, 0.0001, 0.5); 
		
		params = [ 
		Ê Ê["Freq", "RQ", "Fx level", "Dry Level"], 
		Ê Ê[ \freq, \rq, \fxlevel, \level], 
		Ê Ê[freqSpec, rqSpec, \amp, \amp], 
		Ê Êif(setting.notNil, {setting[5]}, {[2000, 0.5, 1, 0]})
		]; 
		
		xiigui = if(channels == 2, { 	// stereo
			XiiEffectGUI.new("Resonant Filter 2x2", \xiiResonant2x2, params, channels, this, setting);
			}, {				// mono
			XiiEffectGUI.new("Resonant Filter 1x1", \xiiResonant1x1, params, channels, this, setting);
		})
	}
}


XiiKlanks {	
	var <>xiigui;
	
	*new { arg server, channels, setting = nil;
		^super.new.initXiiKlanks(server, channels, setting);
		}
		
	initXiiKlanks {arg server, channels, setting;

		var freqSpec, gainSpec, ringSpec, params, s; 
		
		s = server ? Server.local;
		
		// mono
		SynthDef(\xiiKlanks1x1, {arg inbus=0,
							outbus=0, gain=0.01,
							freq1, freq2, freq3, freq4,
							amp1, amp2, amp3, amp4,
							ring1, ring2, ring3, ring4,
							fxlevel = 0.7, 
							level=0;
							
		Ê Êvar fx1, fx2, fx3, fx4, sig; 
		Ê Êsig = InFeedback.ar(inbus, 1); 
		Ê Êfx1 = Ringz.ar(sig*gain, freq1, ring1, amp1); 
		Ê Êfx2 = Ringz.ar(sig*gain, freq2, ring2, amp2); 
		Ê Êfx3 = Ringz.ar(sig*gain, freq3, ring3, amp3); 
		Ê Êfx4 = Ringz.ar(sig*gain, freq4, ring4, amp4); 
		Ê ÊOut.ar(outbus, ((fx1+fx2+fx3+fx4) *fxlevel) + (sig * level)) 
		}).add; 	

		// stereo
		SynthDef(\xiiKlanks2x2, {arg inbus=0,
							outbus=0, gain=0.01,
							freq1, freq2, freq3, freq4,
							amp1, amp2, amp3, amp4,
							ring1, ring2, ring3, ring4,
							fxlevel = 0.7, 
							level=0;
							
		Ê Êvar fx1, fx2, fx3, fx4, sig; 
		Ê Êsig = InFeedback.ar(inbus, 2); 
		Ê Êfx1 = Ringz.ar(sig*gain, freq1, ring1, amp1); 
		Ê Êfx2 = Ringz.ar(sig*gain, freq2, ring2, amp2); 
		Ê Êfx3 = Ringz.ar(sig*gain, freq3, ring3, amp3); 
		Ê Êfx4 = Ringz.ar(sig*gain, freq4, ring4, amp4); 
		Ê ÊOut.ar(outbus, ((fx1+fx2+fx3+fx4) *fxlevel) + (sig * level)) 
		}).add; 	

		freqSpec = ControlSpec.new(20, 20000, \exponential, 1, 2000); 
		gainSpec = ControlSpec.new(0.001, 1, \exponential, 0.001, 0.01); 
		ringSpec = ControlSpec.new(0.01, 4, \linear, 0.01, 1); 
		
		params = [ 
		Ê Ê["Gain", "Freq1", "Amp1", "Ring1", "Freq2", "Amp2", "Ring2", "Freq3", "Amp3", "Ring3", 
			"Freq4", "Amp4", "Ring4", "Fx level", "Dry Level"], 
		Ê Ê[\gain, \freq1, \amp1, \ring1, \freq2, \amp2, \ring2, \freq3, \amp3, \ring3, 
			\freq4, \amp4, \ring4, \fxlevel, \level], 
		Ê Ê[gainSpec, freqSpec, \amp, ringSpec, freqSpec, \amp, ringSpec, 
			freqSpec, \amp, ringSpec, freqSpec, \amp, ringSpec, \amp, \amp], 
		Ê Êif(setting.notNil, {setting[5]}, 
				{[0.004, 400, 1.0, 1.0, 600, 0.8, 0.9, 800, 0.7, 1.0, 1000, 0.8, 0.6, 0.4, 0]})
		]; 
		
		xiigui = if(channels == 2, { 	// stereo
			XiiEffectGUI.new("Klank Filters 2x2", \xiiKlanks2x2, params, channels, this, setting);
			}, {				// mono
			XiiEffectGUI.new("Klank Filters 1x1", \xiiKlanks1x1, params, channels, this, setting);
		})
	}
}




XiiMoogVCFFF {	
	var <>xiigui;
	
	*new { arg server, channels, setting = nil;
		^super.new.initXiiResonant(server, channels, setting);
		}
		
	initXiiResonant {arg server, channels, setting;

		var freqSpec, gainSpec, params, s; 
		
		s = server ? Server.local;
		
		// mono
		SynthDef(\xiiMoogVCFFF1x1, {arg inbus=0,
							outbus=0, 
							freq=200,
							gain=1, 
							fxlevel = 0.7, 
							level=1.0;
							
		Ê Êvar fx, sig; 
		Ê Êsig = InFeedback.ar(inbus,1); 
		Ê Êfx = MoogFF.ar(sig, freq, gain); 
		Ê ÊOut.ar(outbus, (fx * fxlevel) + (sig * level)) 
		}).add; 	

		// stereo
		SynthDef(\xiiMoogVCFFF2x2, {arg inbus=0,
							outbus=0, 
							freq=200,
							gain=1, 
							fxlevel = 0.7, 
							level=1.0;
							
		Ê Êvar fx, sig; 
		Ê Êsig = InFeedback.ar(inbus, 2); 
		Ê Êfx = MoogFF.ar(sig, freq, gain); 
		Ê ÊOut.ar(outbus, (fx * fxlevel) + (sig * level)) 
		}).add; 	

		freqSpec = ControlSpec.new(20, 20000, \exponential, 1, 2000); 
		gainSpec = ControlSpec.new(0.01, 4, \linear, 0.01, 1); 
		
		params = [ 
		Ê Ê["Freq", "Gain", "Fx level", "Dry Level"], 
		Ê Ê[ \freq, \gain, \fxlevel, \level], 
		Ê Ê[freqSpec, gainSpec, \amp, \amp], 
		Ê Êif(setting.notNil, {setting[5]}, {[2000, 1, 1, 0]})
		]; 
		
		xiigui = if(channels == 2, { 	// stereo
			XiiEffectGUI.new("Moog VCF FF 2x2", \xiiMoogVCFFF2x2, params, channels, this, setting);
			}, {				// mono
			XiiEffectGUI.new("Moog VCF FF 1x1", \xiiMoogVCFFF1x1, params, channels, this, setting);
		})
	}
}


XiiMoogVCF {	
	var <>xiigui;
	
	*new { arg server, channels, setting = nil;
		^super.new.initXiiResonant(server, channels, setting);
		}
		
	initXiiResonant {arg server, channels, setting;

		var freqSpec, gainSpec, params, s; 
		
		s = server ? Server.local;
		
		// mono
		SynthDef(\xiiMoogVCF1x1, {arg inbus=0,
							outbus=0, 
							freq=200,
							gain=1, 
							fxlevel = 0.7, 
							level=1.0;
							
		Ê Êvar fx, sig; 
		Ê Êsig = InFeedback.ar(inbus,1); 
		Ê Êfx = MoogVCF.ar(sig, freq, gain); 
		Ê ÊOut.ar(outbus, (fx * fxlevel) + (sig * level)) 
		}).add; 	

		// stereo
		SynthDef(\xiiMoogVCF2x2, {arg inbus=0,
							outbus=0, 
							freq=200,
							gain=1, 
							fxlevel = 0.7, 
							level=1.0;
							
		Ê Êvar fx, sig; 
		Ê Êsig = InFeedback.ar(inbus, 2); 
		Ê Êfx = MoogVCF.ar(sig, freq, gain); 
		Ê ÊOut.ar(outbus, (fx * fxlevel) + (sig * level)) 
		}).add; 	

		freqSpec = ControlSpec.new(20, 20000, \exponential, 1, 2000); 
		gainSpec = ControlSpec.new(0.01, 1, \linear, 0.01, 1); 
		
		params = [ 
		Ê Ê["Freq", "Gain", "Fx level", "Dry Level"], 
		Ê Ê[ \freq, \gain, \fxlevel, \level], 
		Ê Ê[freqSpec, gainSpec, \amp, \amp], 
		Ê Êif(setting.notNil, {setting[5]}, {[2000, 1, 1, 0]})
		]; 
		
		xiigui = if(channels == 2, { 	// stereo
			XiiEffectGUI.new("Moog VCF 2x2", \xiiMoogVCF2x2, params, channels, this, setting);
			}, {				// mono
			XiiEffectGUI.new("Moog VCF 1x1", \xiiMoogVCF1x1, params, channels, this, setting);
		})
	}
}


//////////// crazy filters


///////////////////// experimental - not working right now

XiiResonzOsc {	
	var <>xiigui;
	
	*new { arg server, channels, setting = nil;
		^super.new.initXiiResonant(server, channels, setting);
		}
		
	initXiiResonant {arg server, channels, setting;

		var freqSpec, rqSpec, rq2Spec, params, s; 
		
		s = server ? Server.local;
		
		// mono
		SynthDef(\xiiResonzOsc1x1, {arg inbus=0,
							outbus=0, 
							ffreq=200,
							ffreq2=200,
							rq=1,
							rq2=2,
							gain=1, 
							fxlevel = 0.7, 
							level=1.0;
							
		Ê Êvar fx, sig; 
		Ê Êsig = InFeedback.ar(inbus,1); 
		
		fx = Resonz.ar(sig, ffreq, SinOsc.kr(rq).range(0.2, 0.8));
		fx = Resonz.ar(fx, SinOsc.kr(ffreq2), SinOsc.ar(rq2).range(20,8000));

		Ê ÊOut.ar(outbus, (fx * fxlevel) + (sig * level)) 
		}).add; 	

		// stereo
		SynthDef(\xiiResonzOsc2x2, {arg inbus=0,
							outbus=0, 
							ffreq=200,
							ffreq2=200,
							rq=1,
							rq2=2,
							gain=1, 
							fxlevel = 0.7, 
							level=1.0;
							
		Ê Êvar fx, sig; 
		Ê Êsig = InFeedback.ar(inbus,2); 
		
		fx = Resonz.ar(sig, ffreq, SinOsc.kr(rq).range(0.2, 0.8));
		fx = Resonz.ar(fx, SinOsc.kr(ffreq2), SinOsc.ar(rq2).range(20,8000));
		Ê ÊOut.ar(outbus, (fx * fxlevel) + (sig * level)) 
		}).add; 	

		freqSpec = ControlSpec.new(20, 20000, \exponential, 1, 2000); 
		rqSpec = ControlSpec.new(20, 20000, \exponential, 1, 2000); 
		rq2Spec = ControlSpec.new(1, 20, \exponential, 1, 1); 
		
		params = [ 
		Ê Ê["Freq", "rQ", "FFreq", "rQ2", "Fx level", "Dry Level"], 
		Ê Ê[ \freq, \rq, \ffreq, \rq2, \fxlevel, \level], 
		Ê Ê[freqSpec, rqSpec, freqSpec, rq2Spec,\amp, \amp], 
		Ê Êif(setting.notNil, {setting[5]}, {[2000, 2000, 2000, 2000, 1, 0]})
		]; 
		
		xiigui = if(channels == 2, { 	// stereo
			XiiEffectGUI.new("ResonzOsc 2x2", \xiiResonzOsc2x2, params, channels, this, setting);
			}, {				// mono
			XiiEffectGUI.new("ResonzOsc 1x1", \xiiResonzOsc1x1, params, channels, this, setting);
		})
	}
}

