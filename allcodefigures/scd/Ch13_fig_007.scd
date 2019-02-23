	// figure 13.7 - Sonification of tag systems
(
// compare two axioms on left and right channels
// ? = 4 (size of alphabet)
// v (deletion number) varies [1..6] with horizontal cursor position
{
	var tag, rules, val;
	rules = [[0, 1, 1], [1, 3, 2, 0], [1, 2], [3, 1, 1]]; // same rule for both
	v = MouseX.kr(1, 6);
	val = dup {
		var axiom = Array.fill(14, { #[0, 1, 2, 3].choose }); axiom.join.postln;
		Duty.ar(1 / SampleRate.ir, 0,  Dtag(7e5, v, axiom, rules), doneAction:2);
	} * 0.1
}.play;
)

