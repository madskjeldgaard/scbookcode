/* IZ 050713
Adding synths to FX instances. FX is a utility class in Lilt. */

+ Synth {

	// add synth as fx to an FX group. It reads its input from the in bus
	// and writes it output to the output bus of the FX group
	*fx { | fx,  defName ... args |
		^this.new(defName, [\in, fx.in.index, \out, fx.out] ++ args, fx, \addToTail);
	}

	// add synth as source to an FX group. It writes its output to the in bus
	// of the FX group
	*send { | fx,  defName ... args |
		^this.new(defName, [\out, fx.in.index] ++ args, fx, \addToHead);
	}

}