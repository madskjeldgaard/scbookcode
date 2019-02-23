
// Figure 3.6 Using 'messaging style': Score

(
SynthDef("ScoreSine",{ arg freq = 440;
Out.ar(0,
	SinOsc.ar(freq, 0, 0.2) * Line.kr(1, 0, 0.5, doneAction: 2)
)
}).add;
x = [
// args for s_new are synthdef, nodeID, addAction, targetID, synth args ...
[0.0, [ \s_new, \ScoreSine, 1000, 0, 0,  \freq, 1413 ]],
[0.5, [ \s_new, \ScoreSine, 1001, 0, 0,  \freq, 712 ]],
[1.0, [ \s_new, \ScoreSine, 1002, 0, 0,  \freq, 417 ]],
[2.0, [\c_set, 0, 0]] // dummy command to mark end of NRT synthesis time
];
z = Score(x);
)
z.play;


