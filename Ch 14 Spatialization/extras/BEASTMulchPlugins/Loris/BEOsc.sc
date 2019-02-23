
BEOsc : UGen {	
	*ar { 
		arg freq=440.0, phase=0.0, bw, mul=1.0, add=0.0;
		^this.multiNew('audio', freq, phase, bw).madd(mul, add)
	}
	
	*arFromEnvs {|envs, add = 0|
		^BEOsc.ar(envs[0], envs[3], envs[2], envs[1], add)
	
	}

}

//BEOsc {	
//	*ar { 
//		arg freq=440.0, phase=0.0, bw, mul=1.0, add=0.0;
//		var mod;
//		mod = sqrt( 1.0 - bw ) + ( BrownNoise.ar * sqrt( 2.0 * bw ) );
//		^SinOsc.ar(freq, phase, mul * mod);
//	}
//}

//BEOsc2 : UGen {	
//	*ar { 
//		arg freq=440.0, phase=0.0, bw, mul=1.0, add=0.0;
//		var mod;
//		mod = FastSqrt( 1.0 - bw ) + ( LP4PAv.ar(WhiteNoise.ar) * FastSqrt( 2.0 * bw ) );
//		^SinOsc.ar(freq, phase, mul * mod);
//	}
//}

LP4Noise : UGen {
	
	*ar { arg mul = 1.0, add = 0.0;
		// support this idiom from SC2.
		if (mul.isArray, {
			^{ this.multiNew('audio') }.dup(mul.size).madd(mul, add)
		},{
			^this.multiNew('audio').madd(mul, add)
		});
	}
	*kr { arg mul = 1.0, add = 0.0;
		if (mul.isArray, {
			^{ this.multiNew('control') }.dup(mul.size).madd(mul, add)
		},{
			^this.multiNew('control').madd(mul, add)
		});
	}
	
}

LorisMod : UGen {
	
	*ar { arg bw = 0.0, mul = 1.0, add = 0.0;
		// support this idiom from SC2.
		if (mul.isArray, {
			^{ this.multiNew('audio', bw) }.dup(mul.size).madd(mul, add)
		},{
			^this.multiNew('audio', bw).madd(mul, add)
		});
	}
	
}
