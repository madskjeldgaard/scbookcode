
// plug-n-play quantizing algorithms for process onsets

// for help -- see [TimeSpec] help file

NilTimeSpec {   // always schedules for now
	classvar	<default;
		// use default var to avoid creating and destroying objects
		// no parameters so this should be OK
	*initClass { default = this.prNew; }
	*new { ^default }
	*prNew { ^super.new }
	asTimeSpec { ^this }
	asQuant { ^this }	// for compatibility with pattern playing
	applyLatency { ^this }	// can't schedule earlier than now!
		// override schedTime in subclasses for different scheduling results
	nextTimeOnGrid { arg clock; 
		^clock.tryPerform(\beats) ? 0
	}
		// this is used in too many places, can't delete it outright yet
	schedTime { |clock|
		this.deprecated(thisMethod, this.class.findRespondingMethodFor(\nextTimeOnGrid));
		^this.nextTimeOnGrid(clock)
	}
		// for chucklib, normally you shouldn't use this
		// same as nextTimeOnGrid here but others have to take bp's leadTime into account
	bpSchedTime { |bp|
		^this.nextTimeOnGrid(bp.clock)
	}
	quant { ^0 }
	phase { ^0 }
	offset { ^0 }
	offset_ {}
	timingOffset { ^this.offset }
	timingOffset_ { |offset| this.offset_(offset) }
	bindClassName { ^\NilTimeSpec }

	printOn { |stream| this.storeOn(stream) }
}

DelayTimeSpec : NilTimeSpec {
	var	<delay, dStream, <>clock;
	*new { |delay| ^super.prNew.delay_(delay ? 1) }
	delay_ { |dly|
		delay = dly;
		dStream = dly.asPattern.asStream;
	}
	nextTimeOnGrid { arg clock;
		var beats = clock.tryPerform(\beats);
		^beats + dStream.next(beats)
	}
	
	storeArgs { ^[delay] }
}

DelayTimeSpecLeadTime : DelayTimeSpec {
	bpSchedTime { |bp|
		^this.nextTimeOnGrid(bp.clock) - (bp.leadTime ? 0)
	}
}

// schedule for a specific beat number
AbsoluteTimeSpec : NilTimeSpec {
	var	<>quant, <>clock;
	*new { arg quant; ^super.prNew.quant_(quant ? 1).clock_(TempoClock.default) }
	applyLatency { ^this }
	nextTimeOnGrid { |argClock|
		(quant >= (argClock ? clock).beats).if({
			^quant
		}, {
				// invalid after given time has passed
			MethodError("AbsoluteTimeSpec(%) has expired at % beats."
				.format(quant, (argClock ? clock).beats), this).throw;
		});
	}
	storeArgs { ^[quant] }
}

AbsoluteTimeSpecLeadTime : AbsoluteTimeSpec {
	bpSchedTime { |bp|
		^this.nextTimeOnGrid(bp.clock) - (bp.leadTime ? 0)
	}
}


// BasicTimeSpec is standard scheduling model: quant, phase, offset

BasicTimeSpec : AbsoluteTimeSpecLeadTime {
	var	<phase, <offset;
	var	qstream, pstream, ostream;	// so they can change on successive invocations
	*new { arg quant, phase, offset;
		^super.new(quant).phase_(phase).offset_(offset).clock_(TempoClock.default)
	}
	quant_ { |q|
		qstream = q.asPattern.asStream;
		quant = q;
	}
	phase_ { |p|
		pstream = p.asPattern.asStream;
		phase = p;
	}
	offset_ { |o|
		ostream = o.asPattern.asStream;
		offset = o;
	}
	applyLatency { |latency| ^this.copy.offset_(latency) }
	
		// breaking dependency on TempoClock nextTimeOnGrid
		// because I don't like how it handles phase
		// if it's fixed in TempoClock, I'll revert this change
	nextTimeOnGrid { arg argClock;
		var	schedclock = argClock ? clock ? TempoClock.default,
			q = qstream.next(schedclock) ? 0, p = pstream.next(schedclock) ? 0;
		if(q < 0) { q = q.neg * schedclock.beatsPerBar };
		^roundUp(schedclock.beats - schedclock.baseBarBeat, q) + schedclock.baseBarBeat
			+ p - (ostream.next(schedclock) ? 0)
	}
		// BP's leadTime overrides BasicTimeSpec's offset
	bpSchedTime { |bp|
		var	schedclock = bp.clock ? clock ? TempoClock.default,
			q = qstream.next(schedclock) ? 0, p = pstream.next(schedclock) ? 0;
		if(q < 0) { q = q.neg * schedclock.beatsPerBar };
		^roundUp(schedclock.beats - schedclock.baseBarBeat, q) + schedclock.baseBarBeat
			+ p - (bp.leadTime ? 0)
	}
	storeArgs { 
		^if(offset.isNil) {
			if(phase.isNil) {
				[quant]
			} {
				[quant, phase]
			}
		} {
			[quant, phase, offset]
		}
	}
}


// retaining stubs for backward compatibility

QuantOffsetTimeSpec : BasicTimeSpec {
	*new { arg quant, offset;
		this.deprecated(thisMethod, Meta_BasicTimeSpec.findRespondingMethodFor(\new));
		^super.new(quant, offset)
	}
}

QuantOffsetLatencyTimeSpec : QuantOffsetTimeSpec {
	*new { arg quant, offset, latency;
		this.deprecated(thisMethod, Meta_BasicTimeSpec.findRespondingMethodFor(\new));
		^super.new(quant, offset, latency)
	}
}

QuantOffsetLatencyWrapTimeSpec : QuantOffsetLatencyTimeSpec {
}
