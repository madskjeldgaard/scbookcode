/* IZ 060306

Series of functions to be performed at predetermined count intervals: 

(

a = Pactions([
	2,						// wait 2 counts
	{ "starting ... ".post }, 		// start something
	3,						// wait 3 counts
	{ "set x! - ".post }, 	// change something
	2,						// wait 2 counts
	{ "... stopping".postln }		// stop something
], 2							// repeat the above 2 times
).asStream;

Routine({
	20.do { 
		0.25.wait;
		a.next;
	}
}).play;

)
*/

Pactions : Pattern {
	*new { | countsOrActions, repeats = 1 |
		^Pseq(
			countsOrActions.collect { | ca |
				if (ca isKindOf: Function) {
					Pfuncn(ca, 1);
				}{
					Pcount(ca);
				}
			},
			repeats
		)
	}
}

