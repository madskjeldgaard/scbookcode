
// clips bins to a threshold
XiiMagClip {	
	var <>xiigui;

	*new { arg server, channels, setting = nil;
		^super.new.initMagClip(server, channels, setting);
		}
		
	initMagClip {arg server, channels, setting;
		var threshSpec, params, s, buffer; 
		s = server ? Server.default;
		
		SynthDef(\xiiSpectral_MagClip1x1, { arg inbus=0, outbus=0, thresh = 0, bufnum=0, dryvol = 0, vol=1;
			var in, chain;
			in = InFeedback.ar(inbus, 1);
			chain = FFT(bufnum, in);
			chain = PV_MagClip(chain, thresh);
			// I need to delay the output of raw signal by the size of FFT buffer
			Out.ar(outbus, (DelayL.ar(in, 0.2, ((2*2048))/44100) * dryvol) + (IFFT(chain) * vol));
		}).add;
		
		
		SynthDef(\xiiSpectral_MagClip2x2, { arg inbus=0, outbus=0, thresh = 0, bufnum=0, dryvol=0, vol=1;
			var in, chain;
			in = InFeedback.ar(inbus, 2);
			chain = FFT(bufnum, in);
			chain = PV_MagClip(chain, thresh); 
			// I need to delay the output of raw signal by the size of FFT buffer
			Out.ar(outbus, (DelayL.ar(in, 0.2, ((2*2048))/44100) * dryvol) + (IFFT(chain).dup * vol));
		}).add;
		
		threshSpec = ControlSpec.new(0.01, 40, \exp, 0.01, 5); 

		if(channels == 2, { 	// stereo
			buffer = Buffer.alloc(s, 2048, 2); // a four second 2 channel Buffer
		}, {
			buffer = Buffer.alloc(s, 2048, 1); // a four second 1 channel Buffer
		});

		params = [ 
		Ê Ê["Thresh", "FxLevel", "DryLevel"], 
		Ê Ê[ \thresh, \vol, \dryvol, \bufnum], 
		Ê Ê[threshSpec, \amp, \amp], 
		Ê Êif(setting.notNil, {setting[5]}, {[5, 1.0, 0, buffer.bufnum]})
		]; 
		
		xiigui = if(channels == 2, { 	// stereo
			XiiEffectGUI.new("Spectral MagClip 2x2", \xiiSpectral_MagClip2x2, params, channels, this, setting); 
			}, {				// mono
			XiiEffectGUI.new("Spectral MagClip 1x1", \xiiSpectral_MagClip1x1, params, channels, this, setting);
		})
	}
}



// average magnitudes across bins - smears it with its neighbors 
XiiMagSmear {	
	var <>xiigui;

	*new { arg server, channels, setting = nil;
		^super.new.initMagSmear(server, channels, setting);
		}
		
	initMagSmear {arg server, channels, setting;
		var binsSpec, params, s, buffer; 
		s = server ? Server.default;
		
		SynthDef(\xiiSpectral_MagSmear1x1, { arg inbus=0, outbus=0, bins = 0, bufnum=0, dryvol = 0, vol=1;
			var in, chain;
			in = InFeedback.ar(inbus, 1);
			chain = FFT(bufnum, in);
			chain = PV_MagSmear(chain, bins);
			// I need to delay the output of raw signal by the size of FFT buffer
			Out.ar(outbus, (DelayL.ar(in, 0.2, ((2*2048))/44100) * dryvol) + (IFFT(chain) * vol));
		}).add;
		
		
		SynthDef(\xiiSpectral_MagSmear2x2, { arg inbus=0, outbus=0, bins = 0, bufnum=0, dryvol=0, vol=1;
			var in, chain;
			in = InFeedback.ar(inbus, 2);
			chain = FFT(bufnum, in);
			chain = PV_MagSmear(chain, bins); 
			// I need to delay the output of raw signal by the size of FFT buffer
			Out.ar(outbus, (DelayL.ar(in, 0.2, ((2*2048))/44100) * dryvol) + (IFFT(chain).dup * vol));
		}).add;
		
		binsSpec = ControlSpec.new(1, 50, \lin, 1, 1); 

		if(channels == 2, { 	// stereo
			buffer = Buffer.alloc(s, 2048, 2); // a four second 2 channel Buffer
		}, {
			buffer = Buffer.alloc(s, 2048, 1); // a four second 1 channel Buffer
		});

		params = [ 
		Ê Ê["Bins", "FxLevel", "DryLevel"], 
		Ê Ê[\bins, \vol, \dryvol, \bufnum], 
		Ê Ê[binsSpec, \amp, \amp], 
		Ê Êif(setting.notNil, {setting[5]}, {[1, 1.0, 0, buffer.bufnum]})
		]; 
		
		xiigui = if(channels == 2, { 	// stereo
			XiiEffectGUI.new("Spectral MagSmear 2x2", \xiiSpectral_MagSmear2x2, params, channels, this, setting); 
			}, {				// mono
			XiiEffectGUI.new("Spectral MagSmear 1x1", \xiiSpectral_MagSmear1x1, params, channels, this, setting);
		})
	}
}




// average magnitudes across bins - smears it with its neighbors 
XiiMagShift {	
	var <>xiigui;

	*new { arg server, channels, setting = nil;
		^super.new.initMagShift(server, channels, setting);
		}
		
	initMagShift {arg server, channels, setting;
		var stretchSpec, shiftSpec, params, s, buffer; 
		s = server ? Server.default;
		
		SynthDef(\xiiSpectral_MagSmear1x1, { arg inbus=0, outbus=0, stretch=0, shift=0, bufnum=0, dryvol = 0, vol=1;
			var in, chain;
			in = InFeedback.ar(inbus, 1);
			chain = FFT(bufnum, in);
			chain = PV_MagShift(chain, stretch, shift);
			// I need to delay the output of raw signal by the size of FFT buffer
			Out.ar(outbus, (DelayL.ar(in, 0.2, ((2*2048))/44100) * dryvol) + (IFFT(chain) * vol));
		}).add;
		
		
		SynthDef(\xiiSpectral_MagSmear2x2, { arg inbus=0, outbus=0, stretch=0, shift=0, bufnum=0, dryvol=0, vol=1;
			var in, chain;
			in = InFeedback.ar(inbus, 2);
			chain = FFT(bufnum, in);
			chain = PV_MagShift(chain, stretch, shift); 
			// I need to delay the output of raw signal by the size of FFT buffer
			Out.ar(outbus, (DelayL.ar(in, 0.2, ((2*2048))/44100) * dryvol) + (IFFT(chain).dup * vol));
		}).add;
		
		stretchSpec = ControlSpec.new(0.1, 5, \lin, 0.01, 1); 
		shiftSpec = ControlSpec.new(0.1, 50, \lin, 0.01, 1); 

		if(channels == 2, { 	// stereo
			buffer = Buffer.alloc(s, 2048, 2); // a four second 2 channel Buffer
		}, {
			buffer = Buffer.alloc(s, 2048, 1); // a four second 1 channel Buffer
		});

		params = [ 
		Ê Ê["Stretch", "Shift", "FxLevel", "DryLevel"], 
		Ê Ê[\stretch, \shift, \vol, \dryvol, \bufnum], 
		Ê Ê[stretchSpec, shiftSpec, \amp, \amp], 
		Ê Êif(setting.notNil, {setting[5]}, {[1.0, 0.1, 1.0, 0, buffer.bufnum]})
		]; 
		
		xiigui = if(channels == 2, { 	// stereo
			XiiEffectGUI.new("Spectral MagShift 2x2", \xiiSpectral_MagSmear2x2, params, channels, this, setting); 
			}, {				// mono
			XiiEffectGUI.new("Spectral MagShift 1x1", \xiiSpectral_MagSmear1x1, params, channels, this, setting);
		})
	}
}





// freezes the magnitudes when level > 0.5 
XiiMagFreeze {	
	var <>xiigui;

	*new { arg server, channels, setting = nil;
		^super.new.initMagFreeze(server, channels, setting);
		}
		
	initMagFreeze {arg server, channels, setting;
		var params, s, buffer; 
		s = server ? Server.default;
		
		SynthDef(\xiiSpectral_MagFreeze1x1, { arg inbus=0, outbus=0, freeze=0, bufnum=0, dryvol = 0, vol=1;
			var in, chain;
			in = InFeedback.ar(inbus, 1);
			chain = FFT(bufnum, in);
			chain = PV_MagFreeze(chain, freeze > 0.5 );
			// I need to delay the output of raw signal by the size of FFT buffer
			Out.ar(outbus, (DelayL.ar(in, 0.2, ((2*2048))/44100) * dryvol) + (IFFT(chain) * vol));
		}).add;
		
		
		SynthDef(\xiiSpectral_MagFreeze2x2, { arg inbus=0, outbus=0, freeze=0, bufnum=0, dryvol=0, vol=1;
			var in, chain;
			in = Mix.ar(InFeedback.ar(inbus, 2));
			chain = FFT(bufnum, in);
			chain = PV_MagFreeze(chain, freeze > 0.5); 
			// I need to delay the output of raw signal by the size of FFT buffer
			Out.ar(outbus, (DelayL.ar(in, 0.2, ((2*2048))/44100) * dryvol) + (IFFT(chain).dup * vol));
		}).add;
		
		if(channels == 2, { 	// stereo
			buffer = Buffer.alloc(s, 2048, 2); // a four second 2 channel Buffer
		}, {
			buffer = Buffer.alloc(s, 2048, 1); // a four second 1 channel Buffer
		});

		params = [ 
		Ê Ê["Freeze",  "FxLevel", "DryLevel"], 
		Ê Ê[\freeze, \vol, \dryvol, \bufnum], 
		Ê Ê[\unipolar, \amp, \amp], 
		Ê Êif(setting.notNil, {setting[5]}, {[0.5, 0.1, 0, buffer.bufnum]})
		]; 
		
		xiigui = if(channels == 2, { 	// stereo
			XiiEffectGUI.new("Spectral MagFreeze 2x2", \xiiSpectral_MagFreeze2x2, params, channels, this, setting); 
			}, {				// mono
			XiiEffectGUI.new("Spectral MagFreeze 1x1", \xiiSpectral_MagFreeze1x1, params, channels, this, setting);
		})
	}
}





// makes series of gaps in spectrum 
XiiRectComb {	
	var <>xiigui;

	*new { arg server, channels, setting = nil;
		^super.new.initRectComb(server, channels, setting);
		}
		
	initRectComb {arg server, channels, setting;
		var teethSpec, widthSpec, params, s, buffer; 
		s = server ? Server.default;
		
		SynthDef(\xiiSpectral_RectComb1x1, { arg inbus=0, outbus=0, teeth=0, phase=0, width=0, bufnum=0, dryvol = 0, vol=1;
			var in, chain;
			in = InFeedback.ar(inbus, 1);
			chain = FFT(bufnum, in);
			chain = PV_RectComb(chain, teeth, phase, width);
			// I need to delay the output of raw signal by the size of FFT buffer
			Out.ar(outbus, (DelayL.ar(in, 0.2, ((2*2048))/44100) * dryvol) + (IFFT(chain) * vol));
		}).add;
		
		
		SynthDef(\xiiSpectral_RectComb2x2, { arg inbus=0, outbus=0, teeth=0, phase=0, width=0, bufnum=0, dryvol=0, vol=1;
			var in, chain;
			in = InFeedback.ar(inbus, 2);
			chain = FFT(bufnum, in);
			chain = PV_RectComb(chain, teeth, phase, width); 
			// I need to delay the output of raw signal by the size of FFT buffer
			Out.ar(outbus, (DelayL.ar(in, 0.2, ((2*2048))/44100) * dryvol) + (IFFT(chain).dup * vol));
		}).add;
		
		teethSpec = ControlSpec.new(0.01, 35, \lin, 0.01, 1); 
		//widthSpec = ControlSpec.new(0.1, 50, \lin, 0.01, 1); 

		if(channels == 2, { 	// stereo
			buffer = Buffer.alloc(s, 2048, 2); // a four second 2 channel Buffer
		}, {
			buffer = Buffer.alloc(s, 2048, 1); // a four second 1 channel Buffer
		});

		params = [ 
		Ê Ê["Teeth", "Phase", "Width", "FxLevel", "DryLevel"], 
		Ê Ê[\teeth, \phase, \width, \vol, \dryvol, \bufnum], 
		Ê Ê[teethSpec, \bipolar, \amp, \amp, \amp], 
		Ê Êif(setting.notNil, {setting[5]}, {[5.0, 0.5, 0.2, 1.0, 0, buffer.bufnum]})
		]; 
		
		xiigui = if(channels == 2, { 	// stereo
			XiiEffectGUI.new("Spectral RectComb 2x2", \xiiSpectral_RectComb2x2, params, channels, this, setting); 
			}, {				// mono
			XiiEffectGUI.new("Spectral RectComb 1x1", \xiiSpectral_RectComb1x1, params, channels, this, setting);
		})
	}
}






// randomises the order of bins 
XiiBinScramble {	
	var <>xiigui;

	*new { arg server, channels, setting = nil;
		^super.new.initBinScramble(server, channels, setting);
		}
		
	initBinScramble {arg server, channels, setting;
		var trigSpec, params, s, buffer; 
		s = server ? Server.default;
		
		SynthDef(\xiiSpectral_BinScramble1x1, { arg inbus=0, outbus=0, wipe=0, width=0, trig=0, bufnum=0, dryvol = 0, vol=1;
			var in, chain;
			in = InFeedback.ar(inbus, 1);
			chain = FFT(bufnum, in);
			chain = PV_BinScramble(chain, wipe, width, Impulse.kr(trig));
			// I need to delay the output of raw signal by the size of FFT buffer
			Out.ar(outbus, (DelayL.ar(in, 0.2, ((2*2048))/44100) * dryvol) + (IFFT(chain) * vol));
		}).add;
		
		
		SynthDef(\xiiSpectral_BinScramble2x2, { arg inbus=0, outbus=0, wipe=0, width=0, trig=0, bufnum=0, dryvol=0, vol=1;
			var in, chain;
			in = Mix.ar(InFeedback.ar(inbus, 2));
			chain = FFT(bufnum, in);
			chain = PV_BinScramble(chain, wipe, width, Impulse.kr(trig)); 
			// I need to delay the output of raw signal by the size of FFT buffer
			Out.ar(outbus, (DelayL.ar(in, 0.2, ((2*2048))/44100) * dryvol) + (IFFT(chain).dup * vol));
		}).add;
		
		trigSpec = ControlSpec.new(0, 5, \lin, 0.01, 0); 

		if(channels == 2, { 	// stereo
			buffer = Buffer.alloc(s, 2048, 2); // a four second 2 channel Buffer
		}, {
			buffer = Buffer.alloc(s, 2048, 1); // a four second 1 channel Buffer
		});

		params = [ 
		Ê Ê["Wipe", "Width", "Trig", "FxLevel", "DryLevel"], 
		Ê Ê[\wipe, \width, \trig, \vol, \dryvol, \bufnum], 
		Ê Ê[\unipolar, \unipolar, trigSpec, \amp, \amp], 
		Ê Êif(setting.notNil, {setting[5]}, {[0.3, 1, 0, 1.0, 0, buffer.bufnum]})
		]; 
		
		xiigui = if(channels == 2, { 	// stereo
			XiiEffectGUI.new("Spectral BinScramble 2x2", \xiiSpectral_BinScramble2x2, params, channels, this, setting); 
			}, {				// mono
			XiiEffectGUI.new("Spectral BinScramble 1x1", \xiiSpectral_BinScramble1x1, params, channels, this, setting);
		})
	}
}




// shift and scale the position of the bins 
XiiBinShift {	
	var <>xiigui;

	*new { arg server, channels, setting = nil;
		^super.new.initBinShift(server, channels, setting);
		}
		
	initBinShift {arg server, channels, setting;
		var stretchSpec, shiftSpec, params, s, buffer; 
		s = server ? Server.default;
		
		SynthDef(\xiiSpectral_BinShift1x1, { arg inbus=0, outbus=0, stretch=0, shift=0, bufnum=0, dryvol = 0, vol=1;
			var in, chain;
			in = InFeedback.ar(inbus, 1);
			chain = FFT(bufnum, in);
			chain = PV_BinShift(chain, stretch, shift);
			// I need to delay the output of raw signal by the size of FFT buffer
			Out.ar(outbus, (DelayL.ar(in, 0.2, ((2*2048))/44100) * dryvol) + (IFFT(chain) * vol));
		}).add;
		
		
		SynthDef(\xiiSpectral_BinShift2x2, { arg inbus=0, outbus=0, stretch=0, shift=0, trig=0, bufnum=0, dryvol=0, vol=1;
			var in, chain;
			in = Mix.ar(InFeedback.ar(inbus, 2));
			chain = FFT(bufnum, in);
			chain = PV_BinShift(chain, stretch, shift); 
			// I need to delay the output of raw signal by the size of FFT buffer
			Out.ar(outbus, (DelayL.ar(in, 0.2, ((2*2048))/44100) * dryvol) + (IFFT(chain).dup * vol));
		}).add;
		
		stretchSpec = ControlSpec.new(0.1, 5, \lin, 0.01, 1); 
		shiftSpec = ControlSpec.new(-128, 228, \lin, 1, 1); 

		if(channels == 2, { 	// stereo
			buffer = Buffer.alloc(s, 2048, 2); // a four second 2 channel Buffer
		}, {
			buffer = Buffer.alloc(s, 2048, 1); // a four second 1 channel Buffer
		});

		params = [ 
		Ê Ê["Stretch", "Shift", "FxLevel", "DryLevel"], 
		Ê Ê[\stretch, \shift, \vol, \dryvol, \bufnum], 
		Ê Ê[stretchSpec, shiftSpec, \amp, \amp], 
		Ê Êif(setting.notNil, {setting[5]}, {[1, 1, 1, 0, buffer.bufnum]})
		]; 
		
		xiigui = if(channels == 2, { 	// stereo
			XiiEffectGUI.new("Spectral BinShift 2x2", \xiiSpectral_BinShift2x2, params, channels, this, setting); 
			}, {				// mono
			XiiEffectGUI.new("Spectral BinShift 1x1", \xiiSpectral_BinShift1x1, params, channels, this, setting);
		})
	}
}


