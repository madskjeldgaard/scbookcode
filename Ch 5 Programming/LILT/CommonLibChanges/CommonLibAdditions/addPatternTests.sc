/* (IZ 2005-09-05)
Utilities for testing patterns during develolpment: 
test(n = 10): Post the output of the first n iterations of the pattern
ptest(key = \degree, dur = 0.1, clock): Play the pattern with Pbind(key, pattern, \dur dur)
*/


+ Pattern {
	test { | repeats = 10 |
		var stream;
		stream = this.asStream;
		{ stream.next.postln; } ! repeats;
	}
	ptest { | key = \degree, dur = 0.1, clock |
		clock = clock ? SystemClock;
		^Pbind(key, this, \dur, dur).play(clock);
	}

}
