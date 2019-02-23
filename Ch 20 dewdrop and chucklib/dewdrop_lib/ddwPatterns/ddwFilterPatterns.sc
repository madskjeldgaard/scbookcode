
// I wrote Pinterp before Pseg got added; keeping the stub for backward compatibility
// current embedInStream implementation is incorrect
// because Envs as streams do not terminate anymore

Pinterp : FilterPattern {
	var	<>times, <>curves;
	*new { |pattern, times = 1.0, curves = \lin|
		^Pseg(pattern, times, curves)
	}
}

// pattern should return patterns, each of which should be embedded in the stream
Pembedn : Pn {
	embedInStream { |event|
		var	stream, result;
		stream = pattern.asStream;
		repeats.value.do({
			(result = stream.next(event)).notNil.if({
				event = result.embedInStream(event);
			}, { "Pembedn got nil, returning".warn; ^event });
		});
		^event
	}
}


// this should be used ONLY for event patterns that will be played on a clock
// streams may terminate prematurely if you use this in other contexts
// this is a workaround until I find a better solution

PnNilSafe : Pn {
	embedInStream { arg event;
		var	saveLogicalTime;
		repeats.value.do {
			saveLogicalTime = thisThread.clock.beats;
			event = pattern.embedInStream(event);
			if(thisThread.clock.beats == saveLogicalTime) { ^event }
		};
		^event;
	}
}


// Pstutter, but based on next value of child stream
// n-stream gets passed not the event, but the next value of the child
// child stream still gets passed the event
Psmartstutter : Pstutter {
	embedInStream { arg event;
		var inevent, nn;

		var stream = pattern.asStream;
		var nstream = n.asStream;

		while ({
			(inevent = stream.next(event)).notNil
		},{
			(nn = nstream.next(inevent)).notNil.if({
				nn.abs.do({
					event = inevent.copy.embedInStream(event);
				});
			}, { ^event });
		});
		^event;
	}
}


Pdelta : FilterPattern {
	var	<>cycle;
	*new { |pattern, cycle = 4|
		^super.newCopyArgs(pattern).cycle_(cycle)
	}
	
	embedInStream { |inval|
		var	stream = pattern.asStream,
			lastValue, value;
		(lastValue = stream.next(inval)).isNil.if({ ^inval });
		inf.do({
			(value = stream.next(inval)).isNil.if({ ^inval });
			{ value >= lastValue }.while({
				inval = (value - lastValue).yield;	// these must be numbers obviously
				lastValue = value;
				(value = stream.next(inval)).isNil.if({ ^inval });
			});
			lastValue = lastValue - (lastValue - value).roundUp(cycle);
			inval = (value - lastValue).yield;
			lastValue = value;
		});
		^inval
	}
}


// record scratching goes forward and backward thru the audio stream
// Pscratch does the same for the output values of a pattern
// memory is finite (can only go backward so far)
// recommend to use Pwrand for stepPattern -- weights can give an overall positive direction

Pscratch : FilterPattern {
	var	<>stepPattern, <>memorySize;
	*new { |pattern, stepPattern, memorySize = 100|
		^super.newCopyArgs(pattern).stepPattern_(stepPattern).memorySize_(memorySize)
	}
	
	embedInStream { |inval|
		var	memSize = memorySize,	// protect against the instance variable changing
			memory = Array.newClear(memSize),	// a circular buffer
			origin = 0,
			bottomIndex = 0,	// memory.wrapAt(bottomIndex) will always be the OLDEST element
			outIndex = 0,
			stream = pattern.asStream,
			stepStream = stepPattern.asStream,
			value, step;
		
		while {
			(step = stepStream.next(inval)).notNil
		} {
			(step.isStrictlyPositive or: { value.isNil }).if({
				step = max(step, 1);	// step might be negative or 0 on first iteration
					// have I climbed out of the memory hole?
				(outIndex + step < bottomIndex).if({
						// no, so advance toward the top and return a previous value
					outIndex = outIndex + step;
					inval = memory.wrapAt(outIndex).embedInStream(inval);
				}, {
					(outIndex < (bottomIndex - 1)).if({
							// recover exactly up to bottomIndex
						step = step - bottomIndex + outIndex + 1;
						outIndex = bottomIndex - 1;
					});

						// advance the primary stream and record values in memory
						// output the last value obtained
					step.do({
						(value = stream.next(inval)).isNil.if({
								// always return last value!
							inval = memory.wrapAt(bottomIndex-1).embedInStream(inval);
							^inval
						});
						memory.wrapPut(bottomIndex, value);
						bottomIndex = bottomIndex + 1;
					});
					outIndex = bottomIndex - 1;
					inval = memory.wrapAt(outIndex).embedInStream(inval);
				});
			}, {
					// if negative or 0, decrease outIndex (only as far as legal)
					// and return prior value
				outIndex = max(outIndex + step, bottomIndex - memSize).max(0);
				inval = memory.wrapAt(outIndex).embedInStream(inval);
			});
		};
		^inval
	}
}


// Pconst doesn't really "constrain" -- it FITS the last value to match the desired sum
// This is what I really think "constrain" means... go as far as you can, then stop before hitting the limit

Plimitsum : Pconst {
	embedInStream { arg inval;
		var delta, elapsed = 0.0, nextElapsed, str=pattern.asStream;
		loop ({
			delta = str.next(inval);
			if(delta.isNil) { 
				^inval
			};
			nextElapsed = elapsed + delta;
			if (nextElapsed.round(tolerance) >= sum) {
				^inval
			}{
				elapsed = nextElapsed;
				inval = delta.yield;
			};
		});
	}
}


// Pswitch embeds list items in the stream; Pswitch1 embeds stream values singly
// Pwhile embeds its pattern in the stream; Pwhile1 embeds stream values singly

Pwhile1 : Pwhile {
	embedInStream { |event|
		var	stream = pattern.asStream, next;
		while { (next = stream.next(event)).notNil } {
			if(func.value(event, next)) { event = next.yield } { ^event }
		}
		^event
	}
}
