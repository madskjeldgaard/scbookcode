/* (IZ 2005-08-22)

Map the input value from one range to another

BiMap(inMin, inMax, inWarp, inStep, outMin, outMax, outWarp, outStep);

Usage: 

b = BiMap(30, 90, \lin, 0, 0.5, 0.05, \exp, 0.05);
(30..90).collect { |i| b.(i) };

*/

BiMap {
	var <>inSpec,<>outSpec;
	*new { | inMin=0, inMax=1, inWarp=\lin, inStep=0, outMin=0, outMax=1, outWarp=\lin, outStep=0 |
		^this.newCopyArgs(ControlSpec(inMin, inMax, inWarp, inStep),
			ControlSpec(outMin, outMax, outWarp, outStep)
		)
	}

	value { | inval |
		^outSpec.map(inSpec.unmap(inval))
	}
}
