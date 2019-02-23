/* IZ August-September 2005

Pbracket marks the start and end of a pattern with a function

	Pbracket(beginAction, pattern, endAction):
evaluate beginAction function at the start of the pattern and the endAction at its end. 

Examples: 

a = Pbracket(
	{ |e| [\starting, e].postln; },
	Pbind(\degree, Pwhite(0, 10, 15)),
	{"stopped".postln;}
);
a.play(SystemClock, (dur: 0.1));

// NOTE: When playing a Ppar, the protoEvent provided as arg to .play must contain 
// define values either for delta or for both dur and stretch. 
(
b = Pseq([Ppar([a, Pbindf(a, \octave, 6)]), 
	Pbind(\dur, 1.5, \legato, 3, \degree, Pseq([\rest, Array.rand(8, 0, 10)]))]);
b.play(SystemClock, (dur: 0.15, stretch: 1, legato: 0.01));
)

/////////////
a = Pbracket({ |e| e[\octave] = 7; }, Pbind(\degree, Pwhite(0, 10, 15)), {"ended".postln});
a.play;

*/

Pbracket : FilterPattern {
	var <>begin, <>end;
	*new { arg begin, pattern, end;
		^super.new(pattern).begin_(begin).end_(end)
	}
	storeArgs { ^[pattern, begin, end] }
	embedInStream { arg event;
		var val, inEvent;
		var evtStream = pattern.asStream;
		// copying event prevents changes made by begin func from affecting other streams
		// but does not work with Pbind ?
//		begin.(event = event.copy);
		begin.(event);
		loop {
			inEvent = evtStream.next(event);
			if (inEvent.isNil) {
				end.(event);
				^event;
			};
			event = inEvent.yield;
		};
	}
}

/*

(
Pbracket({|x|x.size.postln}, Pbind(\degree, Pwhite(0, 10, 15)), {|x|x.keys.postln})
	.play(SystemClock, (dur: 0.1));
)

 
(
a = (dur: 0.5, stretch: 0.3);
Ppar([
	Pbracket(
		{|x|x[\legato] = 0.1},
		Pbind(\degree, Pwhite(0, 10, 15)), 
		{|x|x.keys.postln}
	),
	Pbracket(
		{|x|x[\legato] = 0.1},
		Pbind(\degree, Pwhite(0, 10, 15), \dur, 0.25), 
		{|x|x.keys.postln}
	)
]).play(SystemClock, a);
)

}
*/