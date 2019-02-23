Flanger : Filter {	
	*ar { 
		arg in, rate=0.5, depth=1.0, mul=1.0, add=0.0;
		^this.multiNew('audio', in, rate, depth).madd(mul, add)
	}
	*kr {
		arg in, rate=0.5, depth=1.0, mul=1.0, add=0.0;
		^this.multiNew('control', in, rate, depth).madd(mul, add)
	}
}
