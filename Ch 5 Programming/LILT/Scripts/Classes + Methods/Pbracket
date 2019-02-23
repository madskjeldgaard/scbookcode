/* (IZ 2005-10-20) { 
/*

Pbracket marks the start and end of a pattern with a function

Pbracket(begin, pattern, end):
evaluate begin function at the start of the pattern and the end function at its end. 

Examples: 

} */

(
a = Pbracket(
	{ |e| [\starting, e].postln; },
	Pbind(\degree, Pwhite(0, 10, 15)),
	{"stopped".postln;}
);
a.play(SystemClock, (dur: 0.1));
)

// NOTE: When playing a Ppar, the protoEvent provided as arg to .play must contain 
// define values either for delta or for both dur and stretch. 
( // this example requires the previous example to define the pattern in variable a.
b = Pseq([Ppar([a, Pbindf(a, \octave, 6)]),
	Pbind(\dur, 1.5, \legato, 3, \degree, Pseq([\rest, Array.rand(8, 0, 10)]))]);
b.play(SystemClock, (dur: 0.15, stretch: 1, legato: 0.01));
)

/////////////
a = Pbracket({ |e| e[\octave] = 7; }, Pbind(\degree, Pwhite(0, 10, 15)), {"ended".postln});
a.play;


