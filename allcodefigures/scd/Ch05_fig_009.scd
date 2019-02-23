Figure 5.9

( 
// execute this first to boot the server and load the synth definition
Server.default.waitForBoot({ 
	SynthDef("ping", { | freq = 440 |
		Out.ar(0, 
			SinOsc.ar([freq, freq * (4/3)], 0,
				EnvGen.kr(Env.perc(0.05, 0.3, 0.1, -4), doneAction: 2)
			)
		)
	}).add
});
)

(
// execute this next to create the sounds
var countdown = 100;
var note = 50;
var increment_func, decrement_func;
var action;
increment_func = {
	note = note + [2, 5, 7, 12].choose;
	if (note > 100) { action = decrement_func };
};
decrement_func = {
	note = note - [1, 2, 5, 7, 12].choose;
	if (note < 50) { action = increment_func };
};
action = increment_func;
{
	countdown do: { 
		Synth("ping", [\freq, note.midicps]);
		action.value;
		0.1.wait;
	}
}.fork;
)

