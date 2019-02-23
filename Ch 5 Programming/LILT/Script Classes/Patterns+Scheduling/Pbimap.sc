/* (IZ 2005-08-21) {

- Pmap: Map the value of the source pattern from 0-1 to the range provided by spec
- Punmap: Unmap the value of the source pattern from the range provided by spec to 0-1
- Pbimap: Map the value of the source pattern from the range of inSpec to the range of outSpec

} */

Pmap : FilterPattern {
	var <>spec;
	*new { arg pattern, spec;
		^super.new(pattern).spec_(spec);
	}
	storeArgs { ^[pattern, spec] }
	embedInStream { arg event;
		var next;
		var stream = pattern.asStream;
		while({
			(next = stream.next).notNil
		},{
			event = spec.map(next).yield
		});
		^event;
	}
}

Punmap : FilterPattern {
	var <>spec;
	*new { arg pattern, spec;
		^super.new(pattern).spec_(spec);
	}
	storeArgs { ^[pattern, spec] }
	embedInStream { arg event;
		var next;
		var stream = pattern.asStream;
		while({
			(next = stream.next).notNil
		},{
			event = spec.unmap(next).yield
		});
		^event;
	}
}

/* { Map the input from one range to the other, using the ControlSpecs provided in inSpec, outSpec.

inSpec unmaps (!) the incoming value from a given source range to the range 0-1
outSpec maps (!) the mapped value from 0-1 to the target range min-max

p = Pbimap(Pseq((30..90)), ControlSpec(30, 90), ControlSpec(0.3, 0.03)).asStream;
60.do { p.next.postln; };

See also BiMap class.
} */

Pbimap : FilterPattern {
	var <>inSpec,<>outSpec;
	*new { arg pattern, inSpec, outSpec;
		^super.new(pattern).inSpec_(inSpec).outSpec_(outSpec)
	}
	storeArgs { ^[pattern, inSpec, outSpec] }
	embedInStream { arg event;
		var next;
		var stream = pattern.asStream;
		while({
			(next = stream.next).notNil
		},{
			event = outSpec.map(inSpec.unmap(next)).yield
		});
		^event;
	}
}
