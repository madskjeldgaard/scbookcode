/* IZ 041222 

Play a pattern-stream / function / item with a timer, by asking "next" to the stream-object as long as it returns not-nil. Timer can be anything that returns a number when asked ".next.value". 


Metro({|...a| a.postln;}, 2, *(1..3)).play;
Metro({|...a| a.postln;}, 0.25, *(1..10)).play;

a = Metro({|...a| a.postln;}, Pser((1,0.9..0.1),inf), { 0.1.exprand(2) } !! 7, 1, 2, 3);
a.play;

a = Metro({|...a| a.postln;}, 0.1, *(1..50));
a.play;
a.pause;
a.resume;
a.stop;

a = Metro({|...a| a.postln;}, 2, *(0..100));
a.play;
a.pause;
a.resume;
a.stop;

*/


Metro {
	var <receiver;	// function evaluated with each new value of the data stream
	var <timer;		// pattern for making the timerStream
	var <pattern;		// pattern making the data stream
	var <stream;		// the data stream
	var <timerStream;	// stream producing wait times for each repetition
	var <routine;		// routine for iterating over the stream; see makeRoutine
	var <isRunning = false;
	var <isPaused = false;
	var <currentValue;	// last value produced by the stream

	*new { | receiver, timer, pattern ... morePatterns |
		if (morePatterns.size > 0) {
			pattern = Pseq(pattern.asArray.addAll(morePatterns));
		};
		^this.newCopyArgs(receiver, timer ? 0.5, pattern);
	}

	play { this.start; } // synonym for start
	start {
		if (isRunning) { [this, "is already running"].postln; ^this };
		stream = pattern.asStream;
		timerStream = timer.asStream;
		this.makeRoutine;	// remake to avoid double scheduling mistake
		routine.play(SystemClock);
	}

	makeRoutine {
		routine = Routine({

/* Following stops _after_ having waited one more cycle after the last element in 
the stream. To stop at the same time as the last element, use alternative algorithm below */
			isRunning = true;
			while { isRunning and: {(currentValue = stream.next(this)).notNil}} {
				receiver.value(currentValue, this);
				timerStream.next.value(currentValue).wait;
			};
			this.stop; // immediately set isRunning=false and notify. 

/* 
// Alternative algorithm for stopping at the same time as the 
// output of the last element: 
			currentValue = stream.next(this);
			if (currentValue.isNil) {
				this.stop;
			}{
				isRunning = true;
				while { isRunning } {
					receiver.value(currentValue, this);
					// perform stop+notify before waiting one more cycle
					if ((currentValue = stream.next(this)).isNil) {
						this.stop;
					};
					timerStream.next.value(currentValue).wait;
				}
			}
*/
		});
	}

	stop {
		if (isRunning) { // if stopped from routine, do not repeat notification
			isRunning = false;
			[this, "stopped"].postln; 
			// possibly more useful notification of objects to be added here
		}
	}
	pause { 
		if (isRunning) { 
			routine.stop;
			isPaused = true;
		} 
	}
	resume {
		if (isRunning) {
			if (isPaused) {
				isPaused = false;
			/* NO!!!!!!!!!!! No reset! 
			routine.reset.play // this is wrong!
			Resuming/restarting a routine can cause double scheduling */
				this.makeRoutine;
				routine.play;
			}{
				[this, "is not paused. Will not resume"].postln;
			}
		}{
			[this, "cannot resume a stopped Metro. Do aMetro.start first"].postln; 
		}
	}

}
