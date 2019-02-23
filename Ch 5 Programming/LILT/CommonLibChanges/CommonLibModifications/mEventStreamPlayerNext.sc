/* (IZ 2005-08-20)
Adding notification of dependents when stream ends

Examples:

(
a = Pbind(\degree, Pseq([1,2,3, Pwhite(10, 20, 5)],2), \dur, 0.2).play;
a.addDependant({ | who, what | format("% %", a, what).postln });
)

(
var stream;
stream = Ppar([
	Pbind(\degree, Pseq([4,5,6],4), \dur, 0.5),
	Pbind(\degree, Pseq([1,2,3],2))
], inf).play;
stream.addDependant({ | who, what | format("% %", a, what).postln });
{ stream.stop }.defer(5);
)

*/

+ EventStreamPlayer {
	next { arg inTime;
		var nextTime;
		var outEvent = stream.next(event);
		if (outEvent.isNil) {
			streamHasEnded = stream.notNil;
			stream = nextBeat = nil;
			// IZ: notify dependant objects that stream has ended. 
			this.changed(\stopped);
			^nil
		}{
			if (muteCount > 0) { outEvent.put(\freq, \rest) };
			outEvent.play;
			if ((nextTime = outEvent.delta).isNil) { stream = nil };
			nextBeat = inTime + nextTime;	// inval is current logical beat
			^nextTime
		};
	}
}

/*
+ PauseStream { 

        stop {
                stream = nil;
                isWaiting = false;
              this.changed(\stopped);
        }
}
*/

